const formularioPonto = document.querySelector("[data-ponto-form]");
const formularioPeriodoPonto = document.querySelector("[data-ponto-periodo-form]");
const formularioResumoPonto = document.querySelector("[data-ponto-resumo-form]");
const statusFuncionarioPonto = document.querySelector("[data-ponto-funcionario]");
const statusRegistroPonto = document.querySelector("[data-ponto-status]");
const statusPeriodoPonto = document.querySelector("[data-ponto-periodo-status]");
const statusResumoPonto = document.querySelector("[data-ponto-resumo-status]");
const botaoConsultarPonto = document.querySelector("[data-ponto-consultar]");
const botoesMarcacaoPonto = [...document.querySelectorAll("[data-ponto-marcar]")];
const camposHorarioPonto = [...document.querySelectorAll("[data-ponto-horario]")];
let funcionarioPonto = null;
let nomeFuncionarioPonto = "";
let requisicaoPonto = 0;

function dataLocalISO(data = new Date()) {
  const deslocamento = data.getTimezoneOffset() * 60000;
  return new Date(data.getTime() - deslocamento).toISOString().slice(0, 10);
}

function configurarDataPonto() {
  const data = document.querySelector("#ponto-data");
  const hoje = dataLocalISO();
  data.value = hoje;

  const mes = document.querySelector("#ponto-mes");
  mes.value = hoje.slice(0, 7);
  document.querySelector("#ponto-periodo-inicio").value = hoje;
  document.querySelector("#ponto-periodo-fim").value = hoje;
}

function limparVisualizacaoPonto() {
  camposHorarioPonto.forEach((campo) => {
    campo.value = "";
  });
  botoesMarcacaoPonto.forEach((botao) => {
    botao.disabled = true;
  });
}

function invalidarConsultaPonto() {
  requisicaoPonto++;
  funcionarioPonto = null;
  nomeFuncionarioPonto = "";
  limparVisualizacaoPonto();
  statusFuncionarioPonto.textContent = "";
  statusRegistroPonto.textContent = "Consulte novamente após alterar CPF ou data.";
  statusPeriodoPonto.textContent = "";
  statusResumoPonto.textContent = "";
  document.querySelector("[data-ponto-periodo-lista]").replaceChildren();
  document.querySelector("[data-ponto-resumo-lista]").replaceChildren();
}

function atualizarVisualizacaoPonto(registro) {
  const valores = {
    entrada: registro.entrada,
    saidaIntervalo: registro.saidaIntervalo,
    retornoIntervalo: registro.retornoIntervalo,
    saidaFinal: registro.saidaFinal
  };
  camposHorarioPonto.forEach((campo) => {
    const valor = valores[campo.dataset.pontoHorario];
    campo.value = valor ? valor.slice(11, 16) : "";
  });
  botoesMarcacaoPonto.forEach((botao) => {
    botao.disabled = true;
  });

  if (registro.status === "PENDENTE") {
    document.querySelector('[data-ponto-marcar="ENTRADA"]').disabled = false;
  } else if (registro.status === "EM_ANDAMENTO") {
    const proxima = !registro.saidaIntervalo
      ? "SAIDA_INTERVALO"
      : !registro.retornoIntervalo
      ? "RETORNO_INTERVALO"
      : !registro.saidaFinal ? "SAIDA_FINAL" : null;
    if (proxima) {
      document.querySelector(`[data-ponto-marcar="${proxima}"]`).disabled = false;
    }
  }

  const minutos = registro.minutosTrabalhados == null
    ? "O total de horas será finalizado após a saída final."
    : `Total trabalhado: ${formatarCargaHoraria(registro.minutosTrabalhados)}.`;
  statusRegistroPonto.classList.remove("erro");
  statusRegistroPonto.textContent =
    `Situação: ${registro.status.replaceAll("_", " ").toLowerCase()}. ${minutos}`;
}

async function consultarPontoAtual(evento) {
  evento.preventDefault();
  const dados = new FormData(formularioPonto);
  const cpf = String(dados.get("cpf") || "").trim();
  const data = String(dados.get("data") || "");
  const requisicaoAtual = ++requisicaoPonto;
  funcionarioPonto = null;
  nomeFuncionarioPonto = "";
  limparVisualizacaoPonto();
  statusFuncionarioPonto.classList.remove("erro");
  statusRegistroPonto.classList.remove("erro");
  statusFuncionarioPonto.textContent = "Validando funcionário...";
  statusRegistroPonto.textContent = "Consultando jornada...";
  botaoConsultarPonto.disabled = true;

  try {
    const funcionario = await api.buscarFuncionarioPonto(cpf);
    if (requisicaoAtual !== requisicaoPonto) {
      return;
    }
    if (!funcionario?.id || !funcionario?.nome) {
      throw new Error("A API não retornou a identificação do funcionário.");
    }
    funcionarioPonto = { cpf, data };
    nomeFuncionarioPonto = funcionario.nome;
    statusFuncionarioPonto.textContent = `Funcionário identificado: ${funcionario.nome}.`;
    const registro = await api.consultarPonto(cpf, data);
    if (requisicaoAtual !== requisicaoPonto) {
      return;
    }
    atualizarVisualizacaoPonto(registro);
  } catch (erro) {
    if (requisicaoAtual !== requisicaoPonto) {
      return;
    }
    statusFuncionarioPonto.classList.toggle("erro", !funcionarioPonto);
    statusFuncionarioPonto.textContent = funcionarioPonto
      ? `Funcionário identificado: ${nomeFuncionarioPonto}.`
      : erro.message.includes("Funcionário não encontrado")
      ? "Funcionário não encontrado."
      : erro.message;
    statusRegistroPonto.classList.add("erro");
    statusRegistroPonto.textContent = erro.status === 404 && erro.message.includes("jornada")
      ? "Não há jornada prevista para esta data."
      : `Não foi possível consultar o registro de ponto. ${erro.message}`;
  } finally {
    if (requisicaoAtual === requisicaoPonto) {
      botaoConsultarPonto.disabled = false;
    }
  }
}

async function registrarPonto(botao) {
  if (!funcionarioPonto) {
    statusRegistroPonto.classList.add("erro");
    statusRegistroPonto.textContent = "Consulte o funcionário e a jornada antes de registrar.";
    return;
  }

  botao.disabled = true;
  statusRegistroPonto.classList.remove("erro");
  statusRegistroPonto.textContent = "Registrando marcação...";
  try {
    const registro = await api.registrarMarcacaoPonto({
      ...funcionarioPonto,
      marcacao: botao.dataset.pontoMarcar
    });
    atualizarVisualizacaoPonto(registro);
  } catch (erro) {
    statusRegistroPonto.classList.add("erro");
    statusRegistroPonto.textContent = `Não foi possível registrar. ${erro.message}`;
    botao.disabled = false;
  }
}

function renderizarResumoPonto(resumo) {
  const campos = [
    ["Horas trabalhadas", formatarCargaHoraria(resumo.minutosTrabalhados)],
    ["Horas previstas", formatarCargaHoraria(resumo.minutosPrevistos)],
    ["Excedente sobre o previsto", resumo.minutosExcedentesPrevistos == null
      ? "Indisponível" : formatarCargaHoraria(resumo.minutosExcedentesPrevistos)],
    ["Horas extras pela regra mensal existente", resumo.minutosHorasExtrasApuradas == null
      ? "Apuração pendente" : formatarCargaHoraria(resumo.minutosHorasExtrasApuradas)],
    ["Faltas após o fim da jornada", resumo.quantidadeFaltas],
    ["Pontos pendentes", resumo.quantidadePendencias],
    ["Registros incompletos", resumo.registrosIncompletos],
    ["Atrasos", `${resumo.quantidadeAtrasos} (${formatarCargaHoraria(resumo.minutosAtraso)})`],
    ["Saídas antecipadas", `${resumo.quantidadeSaidasAntecipadas} (${formatarCargaHoraria(resumo.minutosSaidasAntecipadas)})`],
    ["Minutos de intervalo excedentes", resumo.minutosIntervalosExcedentes],
    ["Minutos de intervalo insuficientes", resumo.minutosIntervalosInsuficientes]
  ];
  const lista = document.querySelector("[data-ponto-resumo-lista]");
  lista.replaceChildren();
  campos.forEach(([rotulo, valor]) => {
    const termo = document.createElement("dt");
    termo.textContent = rotulo;
    const descricao = document.createElement("dd");
    descricao.textContent = valor;
    lista.append(termo, descricao);
  });
}

function renderizarPeriodoPonto(registros) {
  const lista = document.querySelector("[data-ponto-periodo-lista]");
  lista.replaceChildren();
  if (!Array.isArray(registros)) {
    throw new Error("A API retornou uma lista de registros inválida.");
  }
  if (registros.length === 0) {
    statusPeriodoPonto.textContent = "Nenhuma jornada prevista para o período.";
    return;
  }
  registros.forEach((registro) => {
    const item = document.createElement("article");
    item.className = "usuario";
    const titulo = document.createElement("h4");
    titulo.textContent = `${registro.data} — ${registro.status.replaceAll("_", " ")}`;
    item.append(titulo);
    const horarios = document.createElement("p");
    horarios.textContent = [
      `Entrada: ${registro.entrada?.slice(11, 16) || "—"}`,
      `Intervalo: ${registro.saidaIntervalo?.slice(11, 16) || "—"}–${registro.retornoIntervalo?.slice(11, 16) || "—"}`,
      `Saída: ${registro.saidaFinal?.slice(11, 16) || "—"}`,
      registro.minutosTrabalhados == null
        ? "Horas: em apuração"
        : `Horas: ${formatarCargaHoraria(registro.minutosTrabalhados)}`
    ].join(" | ");
    item.append(horarios);
    lista.append(item);
  });
  statusPeriodoPonto.textContent = `${registros.length} jornada(s) encontrada(s).`;
}

formularioPonto.addEventListener("submit", consultarPontoAtual);
formularioPonto.addEventListener("input", invalidarConsultaPonto);
formularioPonto.addEventListener("change", invalidarConsultaPonto);
botoesMarcacaoPonto.forEach((botao) => {
  botao.addEventListener("click", () => registrarPonto(botao));
});

formularioPeriodoPonto.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  const cpf = String(new FormData(formularioPonto).get("cpf") || "").trim();
  const dados = new FormData(formularioPeriodoPonto);
  statusPeriodoPonto.classList.remove("erro");
  statusPeriodoPonto.textContent = "Consultando registros...";
  try {
    await api.buscarFuncionarioPonto(cpf);
    const registros = await api.consultarPeriodoPonto(
      cpf,
      String(dados.get("inicio") || ""),
      String(dados.get("fim") || "")
    );
    renderizarPeriodoPonto(registros);
  } catch (erro) {
    statusPeriodoPonto.classList.add("erro");
    statusPeriodoPonto.textContent = `Não foi possível consultar o período. ${erro.message}`;
  }
});

formularioResumoPonto.addEventListener("submit", async (evento) => {
  evento.preventDefault();
  const cpf = String(new FormData(formularioPonto).get("cpf") || "").trim();
  const mes = String(new FormData(formularioResumoPonto).get("mes") || "");
  statusResumoPonto.classList.remove("erro");
  statusResumoPonto.textContent = "Consultando resumo...";
  try {
    await api.buscarFuncionarioPonto(cpf);
    const resumo = await api.consultarResumoMensalPonto(cpf, mes);
    renderizarResumoPonto(resumo);
    statusResumoPonto.textContent = `Resumo de ${resumo.mesReferencia} para ${resumo.nomeFuncionario}.`;
  } catch (erro) {
    statusResumoPonto.classList.add("erro");
    statusResumoPonto.textContent = `Não foi possível consultar o resumo. ${erro.message}`;
  }
});

configurarDataPonto();
