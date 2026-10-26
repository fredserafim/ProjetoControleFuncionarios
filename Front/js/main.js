const nomesTurnos = new Map();
const nomesDepartamentos = new Map();
const diasSemanaNomes = new Map([
  ["MONDAY", "Segunda-feira"],
  ["TUESDAY", "Terça-feira"],
  ["WEDNESDAY", "Quarta-feira"],
  ["THURSDAY", "Quinta-feira"],
  ["FRIDAY", "Sexta-feira"],
  ["SATURDAY", "Sábado"],
  ["SUNDAY", "Domingo"]
]);

function nomeExibicaoTurno(valor) {
  const chave = String(valor);
  return [...nomesTurnos.entries()].find(
    ([nome]) => nome.toLowerCase() === chave.toLowerCase()
  )?.[1] || valor;
}

function formatarCargaHoraria(minutos) {
  const horas = Math.floor(minutos / 60);
  const minutosRestantes = minutos % 60;
  return minutosRestantes
    ? `${horas}h${String(minutosRestantes).padStart(2, "0")}`
    : `${horas}h`;
}

function formatarDataHoraLocal(valor = new Date()) {
  const data = valor instanceof Date
    ? valor
    : new Date(valor);
  const deslocamento = data.getTimezoneOffset() * 60000;
  return new Date(data.getTime() - deslocamento).toISOString().slice(0, 16);
}

function atualizarCampoVigenciaTurno(preencherData = true) {
  const selectTurno = document.querySelector("#funcionario-turno");
  const campoVigencia = document.querySelector("#funcionario-turno-vigencia");
  const jornadaSelecionada = /^\d+$/.test(selectTurno.value);
  campoVigencia.required = jornadaSelecionada;
  document.querySelector("[data-shift-effective-field]").hidden = !jornadaSelecionada;
  if (jornadaSelecionada && preencherData && !campoVigencia.value) {
    campoVigencia.value = formatarDataHoraLocal();
  }
}

function jornadaConfigurada(turno) {
  return Boolean(turno.tipoEscala && turno.horarioInicio && turno.horarioFim);
}

const secoesDashboard = {
  inicio: {
    carregar: () => api.listarUsuarios(),
    nomeItem: (item) => item.nome || `Usuário ${item.id}`,
    campos: [
      ["cpf", "CPF"],
      ["dataNascimento", "Data de nascimento"],
      ["dataCadastro", "Cadastrado em"],
      ["dataAtualizacao", "Atualizado em"],
      ["empresa_id", "ID da empresa"],
      ["razaoSocialEmpresa", "Empresa"],
      ["status", "Status"]
    ]
  },
  turnos: {
    carregar: () => api.listarTurnos(),
    nomeItem: (item) => item.nome || item.periodo || `Jornada ${item.id}`,
    campos: [
      ["tipoJornada", "Tipo de jornada"],
      ["tipoEscala", "Escala"],
      ["diasSemana", "Dias de trabalho"],
      ["horarioInicio", "Início"],
      ["horarioFim", "Término"],
      ["intervalos", "Intervalos"],
      ["cargaEfetivaMinutos", "Carga efetiva diária"],
      ["cargaSemanalPrevistaMinutos", "Carga semanal prevista"],
      ["cargaDiariaContratualMinutos", "Carga contratual diária"],
      ["cargaSemanalContratualMinutos", "Carga contratual semanal"],
      ["cargaMensalPadraoMinutos", "Carga mensal padrão"],
      ["dataCadastro", "Cadastrado em"],
      ["dataAtualizacao", "Atualizado em"],
      ["status", "Status"]
    ],
    descricaoRegistros: "turno(s) encontrado(s)"
  },
  departamentos: {
    carregar: () => api.listarDepartamentos(),
    nomeItem: (item) => item.departamento || `Departamento ${item.id}`,
    campos: [["status", "Status"]],
    descricaoRegistros: "departamento(s) encontrado(s)"
  },
  funcionarios: {
    carregar: () => api.listarFuncionarios(),
    nomeItem: (item) => item.nome || `Funcionário ${item.id}`,
    campos: [
      ["cpf", "CPF"],
      ["telefone", "Telefone"],
      ["endereco", "Endereço"],
      ["funcao", "Função"],
      ["turno", "Turno"],
      ["departamento", "Departamento"],
      ["dataCadastro", "Cadastrado em"],
      ["dataAtualizacao", "Atualizado em"],
      ["status", "Status"]
    ]
  }
};

const secoesCarregadas = new Set();
const secoesCarregando = new Set();
const secoesParaAtualizar = new Set();
const funcionariosPorId = new Map();
const departamentosPorId = new Map();
let departamentoSelecionado = null;
let carregamentoFuncionariosDepartamento = 0;

const dialogoFuncionario = document.querySelector("[data-employee-dialog]");
const formularioFuncionario = document.querySelector("[data-employee-form]");
const statusFormularioFuncionario = document.querySelector("[data-employee-form-status]");
const botaoSalvarFuncionario = document.querySelector("[data-employee-submit]");
let funcionarioEmEdicao = null;

const dialogoTurno = document.querySelector("[data-shift-dialog]");
const formularioTurno = document.querySelector("[data-shift-form]");
const statusFormularioTurno = document.querySelector("[data-shift-form-status]");
const botaoSalvarTurno = document.querySelector("[data-shift-submit]");
const listaIntervalosTurno = document.querySelector("[data-break-list]");
const turnosPorId = new Map();
let turnoEmEdicao = null;

const dialogoDepartamento = document.querySelector("[data-department-dialog]");
const formularioDepartamento = document.querySelector("[data-department-form]");
const statusFormularioDepartamento = document.querySelector("[data-department-form-status]");
const botaoSalvarDepartamento = document.querySelector("[data-department-submit]");
let departamentoEmEdicao = null;

function preencherOpcoes(select, opcoes) {
  const opcaoInicial = select.options[0];
  select.replaceChildren(opcaoInicial);
  opcoes.forEach(({ value, label }) => {
    const opcao = document.createElement("option");
    opcao.value = value;
    opcao.textContent = label;
    select.append(opcao);
  });
}

async function carregarOpcoesFuncionario(funcionario = null) {
  const [turnos, departamentos] = await Promise.all([
    api.listarTurnos(),
    api.listarDepartamentos()
  ]);

  if (!Array.isArray(turnos) || !Array.isArray(departamentos)) {
    throw new Error("A API retornou opções inválidas para o formulário.");
  }

  nomesTurnos.clear();
  nomesDepartamentos.clear();

  const opcoesTurno = turnos
    .filter((turno) => turno.status !== "D"
      || String(turno.id) === String(funcionario?.turnoId))
    .filter((turno) => jornadaConfigurada(turno)
      || String(turno.id) === String(funcionario?.turnoId))
    .map((turno) => {
      const valor = jornadaConfigurada(turno)
        ? String(turno.id)
        : `legacy:${turno.nome || turno.periodo || ""}`;
      const nome = turno.nome || turno.periodo || `Jornada ${turno.id}`;
      const horario = turno.horarioInicio ? ` (${turno.horarioInicio.slice(0, 5)})` : "";
      const label = `${nome}${horario}`;
      nomesTurnos.set(String(turno.id), label);
      if (turno.periodo) {
        nomesTurnos.set(turno.periodo, label);
      }
      return { value: valor, label };
    });

  if (funcionario?.turno && funcionario.turnoId == null) {
    opcoesTurno.push({
      value: `legacy:${funcionario.turno}`,
      label: `${funcionario.turno} (vínculo legado)`
    });
  }

  const opcoesDepartamento = departamentos
    .filter((departamento) => departamento.status !== "D"
      || String(departamento.id) === String(funcionario?.departamentoId))
    .map((departamento) => {
    const label = departamento.departamento || `Departamento ${departamento.id}`;
    nomesDepartamentos.set(String(departamento.id), label);
    return { value: String(departamento.id), label };
    });

  const selectTurno = document.querySelector("#funcionario-turno");
  preencherOpcoes(selectTurno, opcoesTurno);
  preencherOpcoes(document.querySelector("#departamento"), opcoesDepartamento);
}

function mostrarRegistros(secao, registros) {
  const configuracao = secoesDashboard[secao];
  const lista = document.querySelector(`[data-list="${secao}"]`);
  const mensagem = document.querySelector(`[data-status="${secao}"]`);
  lista.replaceChildren();

  if (Array.isArray(registros) && secao === "departamentos") {
    nomesDepartamentos.clear();
    departamentosPorId.clear();
    registros.forEach((departamento) => {
      departamentosPorId.set(String(departamento.id), departamento);
      nomesDepartamentos.set(
        String(departamento.id),
        departamento.departamento || `Departamento ${departamento.id}`
      );
    });
  }

  if (Array.isArray(registros) && secao === "turnos") {
    nomesTurnos.clear();
    turnosPorId.clear();
    registros.forEach((turno) => {
      const valor = turno.nome || turno.periodo || `Jornada ${turno.id}`;
      const horario = turno.horarioInicio
        ? ` (${turno.horarioInicio.slice(0, 5)})`
        : "";
      const label = `${valor}${horario}`;
      nomesTurnos.set(String(turno.id), label);
      if (turno.periodo) {
        nomesTurnos.set(turno.periodo, label);
      }
      turnosPorId.set(String(turno.id), turno);
    });
  }

  if (!Array.isArray(registros) || registros.length === 0) {
    if (secao === "funcionarios") {
      funcionariosPorId.clear();
    }
    mensagem.textContent = "Nenhum registro encontrado.";
    return;
  }

  if (secao === "funcionarios") {
    funcionariosPorId.clear();
    registros.forEach((registro) => funcionariosPorId.set(String(registro.id), registro));
  }

  mensagem.textContent = `${registros.length} ${configuracao.descricaoRegistros || "registro(s) encontrado(s)"}.`;

  registros.forEach((registro) => {
    if (secao === "departamentos") {
      const item = document.createElement("article");
      item.className = "item-dashboard";
      const botaoDepartamento = document.createElement("button");
      botaoDepartamento.type = "button";
      botaoDepartamento.className = "botao-departamento";
      botaoDepartamento.dataset.departmentValue = String(registro.id);
      botaoDepartamento.textContent = registro.departamento || `Departamento ${registro.id}`;
      botaoDepartamento.setAttribute(
        "aria-pressed",
        String(String(departamentoSelecionado) === String(registro.id))
      );
      item.append(botaoDepartamento);
      [["status", "Status"], ["dataCadastro", "Cadastrado em"], ["dataAtualizacao", "Atualizado em"]]
        .forEach(([campo, rotulo]) => {
          if (!registro[campo]) {
            return;
          }
          const detalhe = document.createElement("p");
          const valor = campo === "status"
            ? registro.status === "D" ? "Inativo" : "Ativo"
            : registro[campo];
          detalhe.textContent = `${rotulo}: ${valor}`;
          item.append(detalhe);
        });

      const acoes = document.createElement("div");
      acoes.className = "acoes-lista acoes-departamento";

      const botaoEditar = document.createElement("button");
      botaoEditar.type = "button";
      botaoEditar.className = "botao-secundario";
      botaoEditar.dataset.editDepartment = String(registro.id);
      botaoEditar.textContent = "Editar";
      acoes.append(botaoEditar);

      const botaoStatus = document.createElement("button");
      botaoStatus.type = "button";
      botaoStatus.className = registro.status === "D" ? "" : "botao-secundario";
      if (registro.status === "D") {
        botaoStatus.dataset.reactivateDepartment = String(registro.id);
        botaoStatus.textContent = "Reativar";
      } else {
        botaoStatus.dataset.deactivateDepartment = String(registro.id);
        botaoStatus.textContent = "Desativar";
      }
      acoes.append(botaoStatus);
      item.append(acoes);
      lista.append(item);
      return;
    }

    const item = document.createElement("article");
    item.className = "item-dashboard";

    const titulo = document.createElement("h3");
    titulo.textContent = configuracao.nomeItem(registro);
    item.append(titulo);

    configuracao.campos.forEach(([campo, rotulo]) => {
      if (registro[campo] === undefined || registro[campo] === null || registro[campo] === "") {
        return;
      }

      const detalhe = document.createElement("p");
      const nomeTurno = campo === "turno" ? nomeExibicaoTurno(registro[campo]) : null;
      let valor = nomeTurno || registro[campo];
      if (campo === "status") {
        valor = registro.status === "D" ? "Inativo" : "Ativo";
      } else if (campo === "diasSemana" && Array.isArray(registro.diasSemana)) {
        valor = registro.diasSemana.map((dia) => diasSemanaNomes.get(dia) || dia).join(", ");
      } else if (campo === "intervalos" && Array.isArray(registro.intervalos)) {
        valor = registro.intervalos
          .map(({ inicio, fim }) => `${inicio.slice(0, 5)}–${fim.slice(0, 5)}`)
          .join(", ");
      } else if (campo === "horarioInicio" || campo === "horarioFim") {
        valor = registro[campo]?.slice(0, 5);
      } else if (campo.endsWith("Minutos")) {
        valor = formatarCargaHoraria(registro[campo]);
      } else if (campo === "tipoJornada") {
        valor = { DIURNA: "Diurna", NOTURNA: "Noturna", PERSONALIZADA: "Personalizada" }[registro[campo]]
          || registro[campo];
      } else if (campo === "tipoEscala") {
        valor = { SEMANAL_FIXA: "Semanal fixa", "12X36": "12x36", PERSONALIZADA: "Personalizada" }[registro[campo]]
          || registro[campo];
      }
      detalhe.textContent = `${rotulo}: ${valor}`;
      item.append(detalhe);
    });

    if (secao === "turnos") {
      const acoes = document.createElement("div");
      acoes.className = "acoes-lista acoes-turno";

      const botaoEditar = document.createElement("button");
      botaoEditar.type = "button";
      botaoEditar.className = "botao-secundario";
      botaoEditar.dataset.editShift = String(registro.id);
      botaoEditar.textContent = "Editar";
      acoes.append(botaoEditar);

      const botaoStatus = document.createElement("button");
      botaoStatus.type = "button";
      botaoStatus.className = registro.status === "D" ? "" : "botao-secundario";
      if (registro.status === "D") {
        botaoStatus.dataset.reactivateShift = String(registro.id);
        botaoStatus.textContent = "Reativar";
      } else {
        botaoStatus.dataset.deactivateShift = String(registro.id);
        botaoStatus.textContent = "Desativar";
      }
      acoes.append(botaoStatus);
      item.append(acoes);
    }

    if (secao === "funcionarios") {
      const botaoEditar = document.createElement("button");
      botaoEditar.type = "button";
      botaoEditar.className = "botao-secundario botao-editar-funcionario";
      botaoEditar.dataset.editEmployee = String(registro.id);
      botaoEditar.textContent = "Editar";
      botaoEditar.setAttribute("aria-label", `Editar ${configuracao.nomeItem(registro)}`);
      item.append(botaoEditar);
    }

    lista.append(item);
  });
}

function abrirFormularioTurno(turno = null) {
  turnoEmEdicao = turno;
  formularioTurno.reset();
  statusFormularioTurno.textContent = "";
  statusFormularioTurno.classList.remove("erro");

  const editando = turno !== null;
  document.querySelector("[data-shift-form-title]").textContent =
    editando ? "Editar jornada" : "Nova jornada";
  document.querySelector("#turno-nome").value = turno?.nome || turno?.periodo || "";
  document.querySelector("#turno-tipo-jornada").value = turno?.tipoJornada || "DIURNA";
  document.querySelector("#turno-tipo-escala").value =
    turno?.tipoEscala || "SEMANAL_FIXA";
  document.querySelector("#turno-inicio").value =
    turno?.horarioInicio?.slice(0, 5) || "";
  document.querySelector("#turno-fim").value = turno?.horarioFim?.slice(0, 5) || "";
  preencherIntervalosTurno(turno
    ? turno.intervalos || []
    : [{ inicio: "12:00", fim: "13:30" }]);
  document.querySelector("#turno-data-referencia").value =
    turno?.dataReferencia || "";
  document.querySelector("#turno-carga-diaria-contratual").value =
    turno?.cargaDiariaContratualMinutos ?? "";
  document.querySelector("#turno-carga-semanal-contratual").value =
    turno?.cargaSemanalContratualMinutos ?? "";
  document.querySelector("#turno-carga-mensal").value =
    turno?.cargaMensalPadraoMinutos ?? "";
  document.querySelectorAll('[name="diasSemana"]').forEach((checkbox) => {
    checkbox.checked = Boolean(turno?.diasSemana?.includes(checkbox.value));
  });
  atualizarCamposEscala(!editando);
  atualizarPreviaCargaJornada();
  botaoSalvarTurno.textContent = editando ? "Salvar alterações" : "Salvar jornada";
  dialogoTurno.showModal();
  document.querySelector("#turno-nome").focus();
}

function criarCampoIntervaloTurno(intervalo = {}) {
  const linha = document.createElement("div");
  linha.className = "intervalo-jornada";
  linha.dataset.breakRow = "";

  [["intervaloInicio", "Início do intervalo"], ["intervaloFim", "Término do intervalo"]]
    .forEach(([nome, rotulo]) => {
      const label = document.createElement("label");
      label.className = "campo-turno";
      label.textContent = rotulo;

      const input = document.createElement("input");
      input.type = "time";
      input.name = nome;
      input.value = intervalo[nome === "intervaloInicio" ? "inicio" : "fim"]?.slice(0, 5) || "";
      label.append(input);
      linha.append(label);
    });

  const remover = document.createElement("button");
  remover.type = "button";
  remover.className = "botao-secundario";
  remover.dataset.removeBreak = "";
  remover.textContent = "Remover";
  remover.setAttribute("aria-label", "Remover intervalo");
  linha.append(remover);
  return linha;
}

function preencherIntervalosTurno(intervalos) {
  listaIntervalosTurno.replaceChildren(
    ...(intervalos.length ? intervalos : [{}]).map(criarCampoIntervaloTurno)
  );
}

function obterIntervalosTurno() {
  return [...listaIntervalosTurno.querySelectorAll("[data-break-row]")].map((linha) => ({
    inicio: linha.querySelector('[name="intervaloInicio"]').value,
    fim: linha.querySelector('[name="intervaloFim"]').value
  }));
}

function atualizarCamposEscala(preencherPadroes = false) {
  const escala = document.querySelector("#turno-tipo-escala").value;
  const ciclo12x36 = escala === "12X36";
  document.querySelector("[data-weekdays-fieldset]").hidden = ciclo12x36;
  const campoReferencia = document.querySelector("[data-reference-date-field]");
  campoReferencia.hidden = !ciclo12x36;
  document.querySelector("#turno-data-referencia").required = ciclo12x36;

  const ajuda = document.querySelector("[data-shift-schedule-help]");
  ajuda.textContent = ciclo12x36
    ? "O plantão de 12 horas inicia na data de referência e se repete a cada 48 horas, respeitando 36 horas de descanso."
    : "Os dias selecionados representam o dia de início de cada jornada semanal.";

  if (preencherPadroes && ciclo12x36) {
    document.querySelector("#turno-inicio").value = "19:00";
    document.querySelector("#turno-fim").value = "07:00";
    const primeiroIntervalo = listaIntervalosTurno.querySelector("[data-break-row]");
    primeiroIntervalo.querySelector('[name="intervaloInicio"]').value = "23:00";
    primeiroIntervalo.querySelector('[name="intervaloFim"]').value = "00:00";
    document.querySelector("#turno-data-referencia").value =
      formatarDataHoraLocal().slice(0, 10);
  }
  atualizarPreviaCargaJornada();
}

function atualizarPreviaCargaJornada() {
  const inicio = document.querySelector("#turno-inicio").value;
  const fim = document.querySelector("#turno-fim").value;
  const intervalos = obterIntervalosTurno();
  const previa = document.querySelector("[data-workload-preview]");
  if (!inicio || !fim) {
    previa.textContent = "Carga efetiva calculada: informe início e término.";
    return;
  }

  const minutosDoDia = (horario) => {
    const [hora, minuto] = horario.split(":").map(Number);
    return hora * 60 + minuto;
  };
  const inicioMinutos = minutosDoDia(inicio);
  let fimMinutos = minutosDoDia(fim);
  if (fimMinutos <= inicioMinutos) {
    fimMinutos += 24 * 60;
  }
  const intervalosCalculados = [];
  let minutosIntervalo = 0;
  for (const intervalo of intervalos) {
    if (Boolean(intervalo.inicio) !== Boolean(intervalo.fim)) {
      previa.textContent = "Informe início e término de cada intervalo para calcular a carga.";
      return;
    }
    if (!intervalo.inicio) {
      continue;
    }
    let inicioIntervalo = minutosDoDia(intervalo.inicio) - inicioMinutos;
    if (inicioIntervalo < 0) {
      inicioIntervalo += 24 * 60;
    }
    let fimIntervalo = minutosDoDia(intervalo.fim) - inicioMinutos;
    if (fimIntervalo <= inicioIntervalo) {
      fimIntervalo += 24 * 60;
    }
    if (fimIntervalo > fimMinutos) {
      previa.textContent = "Cada intervalo deve estar dentro do período da jornada.";
      return;
    }
    intervalosCalculados.push({ inicio: inicioIntervalo, fim: fimIntervalo });
    minutosIntervalo += fimIntervalo - inicioIntervalo;
  }
  intervalosCalculados.sort((a, b) => a.inicio - b.inicio);
  for (let indice = 1; indice < intervalosCalculados.length; indice += 1) {
    if (intervalosCalculados[indice].inicio < intervalosCalculados[indice - 1].fim) {
      previa.textContent = "Os intervalos da jornada não podem se sobrepor.";
      return;
    }
  }
  const cargaEfetiva = fimMinutos - inicioMinutos - minutosIntervalo;
  if (cargaEfetiva <= 0) {
    previa.textContent = "A carga efetiva deve ser positiva; confira horários e intervalo.";
    return;
  }

  const diasSelecionados = document.querySelectorAll(
    '[name="diasSemana"]:checked'
  ).length;
  const escala = document.querySelector("#turno-tipo-escala").value;
  const semanal = escala === "12X36"
    ? Math.round(cargaEfetiva * 3.5)
    : cargaEfetiva * diasSelecionados;
  previa.textContent = `Carga efetiva por plantão: ${formatarCargaHoraria(cargaEfetiva)}. Carga semanal prevista: ${
    semanal ? formatarCargaHoraria(semanal) : "selecione os dias"
  }. O intervalo não remunerado foi descontado.`;
}

function cargaContratualOpcional(valor) {
  if (!valor) {
    return null;
  }
  const minutos = Number(valor);
  return Number.isSafeInteger(minutos) && minutos >= 0 ? minutos : NaN;
}

async function salvarTurno(evento) {
  evento.preventDefault();
  if (!formularioTurno.reportValidity() || botaoSalvarTurno.disabled) {
    return;
  }

  const dadosFormulario = new FormData(formularioTurno);
  const nome = dadosFormulario.get("nome").trim();
  const tipoEscala = dadosFormulario.get("tipoEscala");
  const diasSemana = tipoEscala === "12X36"
    ? []
    : dadosFormulario.getAll("diasSemana");
  const intervalos = obterIntervalosTurno();
  const cargaDiariaContratualMinutos =
    cargaContratualOpcional(dadosFormulario.get("cargaDiariaContratualMinutos"));
  const cargaSemanalContratualMinutos =
    cargaContratualOpcional(dadosFormulario.get("cargaSemanalContratualMinutos"));
  const cargaMensalPadraoMinutos =
    cargaContratualOpcional(dadosFormulario.get("cargaMensalPadraoMinutos"));
  const intervalosIncompletos = intervalos.some(
    (intervalo) => Boolean(intervalo.inicio) !== Boolean(intervalo.fim)
  );
  if (!nome || (tipoEscala !== "12X36" && diasSemana.length === 0)
      || intervalosIncompletos
      || [cargaDiariaContratualMinutos, cargaSemanalContratualMinutos,
        cargaMensalPadraoMinutos].some(Number.isNaN)) {
    statusFormularioTurno.classList.add("erro");
    statusFormularioTurno.textContent =
      "Confira o nome, os dias de trabalho, os intervalos e as cargas contratuais.";
    return;
  }

  const editando = turnoEmEdicao !== null;
  const turno = {
    nome,
    periodo: nome,
    tipoJornada: dadosFormulario.get("tipoJornada"),
    tipoEscala,
    diasSemana,
    horarioInicio: dadosFormulario.get("horarioInicio"),
    horarioFim: dadosFormulario.get("horarioFim"),
    intervalos: intervalos.filter((intervalo) => intervalo.inicio && intervalo.fim),
    dataReferencia: dadosFormulario.get("dataReferencia") || null,
    cargaDiariaContratualMinutos,
    cargaSemanalContratualMinutos,
    cargaMensalPadraoMinutos,
    ...(editando ? { status: turnoEmEdicao.status || "A" } : {})
  };
  botaoSalvarTurno.disabled = true;
  statusFormularioTurno.classList.remove("erro");
  statusFormularioTurno.textContent = editando
    ? "Salvando alterações..."
    : "Cadastrando jornada...";

  try {
    if (editando) {
      await api.atualizarTurno(turnoEmEdicao.id, turno);
    } else {
      await api.cadastrarTurno(turno);
    }

    dialogoTurno.close();
    const mensagemSucesso = editando
      ? "Jornada atualizada com sucesso."
      : "Jornada cadastrada com sucesso.";
    const atualizouLista = await carregarSecao("turnos", true);
    const statusLista = document.querySelector('[data-status="turnos"]');
    statusLista.textContent = atualizouLista
      ? mensagemSucesso
      : `${mensagemSucesso} Porém, não foi possível atualizar a listagem. ${statusLista.textContent}`;
  } catch (erro) {
    statusFormularioTurno.classList.add("erro");
    statusFormularioTurno.textContent = `Não foi possível salvar a jornada. ${erro.message}`;
  } finally {
    botaoSalvarTurno.disabled = false;
  }
}

async function alterarStatusTurno(id, status, botao) {
  const reativando = status !== "D";
  const turno = turnosPorId.get(String(id));
  const nome = turno?.periodo || `Turno ${id}`;
  const confirmacao = reativando
    ? `Deseja reativar "${nome}"?`
    : `Deseja desativar "${nome}"? Ele será mantido no banco e poderá ser reativado.`;
  if (!window.confirm(confirmacao)) {
    return;
  }

  const mensagem = document.querySelector('[data-status="turnos"]');
  botao.disabled = true;
  mensagem.classList.remove("erro");
  mensagem.textContent = reativando ? "Reativando turno..." : "Desativando turno...";

  try {
    if (reativando) {
      await api.alterarStatusTurno(id, { status });
    } else {
      await api.excluirTurno(id);
    }

    const atualizouLista = await carregarSecao("turnos", true);
    mensagem.textContent = atualizouLista
      ? `Turno ${reativando ? "reativado" : "desativado"} com sucesso.`
      : `A operação foi concluída, mas a lista não pôde ser atualizada. ${mensagem.textContent}`;
  } catch (erro) {
    mensagem.classList.add("erro");
    mensagem.textContent =
      `Não foi possível ${reativando ? "reativar" : "desativar"} o turno. ${erro.message}`;
  } finally {
    botao.disabled = false;
  }
}

function abrirFormularioDepartamento(departamento = null) {
  departamentoEmEdicao = departamento;
  formularioDepartamento.reset();
  statusFormularioDepartamento.textContent = "";
  statusFormularioDepartamento.classList.remove("erro");

  const editando = departamento !== null;
  document.querySelector("[data-department-form-title]").textContent =
    editando ? "Editar departamento" : "Novo departamento";
  document.querySelector("#departamento-nome").value =
    departamento?.departamento || "";
  botaoSalvarDepartamento.textContent =
    editando ? "Salvar alterações" : "Salvar departamento";
  dialogoDepartamento.showModal();
  document.querySelector("#departamento-nome").focus();
}

async function salvarDepartamento(evento) {
  evento.preventDefault();
  if (!formularioDepartamento.reportValidity() || botaoSalvarDepartamento.disabled) {
    return;
  }

  const departamento = {
    departamento: new FormData(formularioDepartamento).get("departamento").trim()
  };
  if (!departamento.departamento) {
    statusFormularioDepartamento.classList.add("erro");
    statusFormularioDepartamento.textContent = "Informe o nome do departamento.";
    document.querySelector("#departamento-nome").focus();
    return;
  }
  const editando = departamentoEmEdicao !== null;
  if (editando) {
    departamento.status = departamentoEmEdicao.status || "A";
  }

  botaoSalvarDepartamento.disabled = true;
  statusFormularioDepartamento.classList.remove("erro");
  statusFormularioDepartamento.textContent = editando
    ? "Salvando alterações..."
    : "Cadastrando departamento...";

  try {
    if (editando) {
      await api.atualizarDepartamento(departamentoEmEdicao.id, departamento);
    } else {
      await api.cadastrarDepartamento(departamento);
    }

    dialogoDepartamento.close();
    const mensagemSucesso = editando
      ? "Departamento atualizado com sucesso."
      : "Departamento cadastrado com sucesso.";
    const atualizouLista = await carregarSecao("departamentos", true);
    const statusLista = document.querySelector('[data-status="departamentos"]');
    if (atualizouLista) {
      statusLista.textContent = mensagemSucesso;
    } else {
      statusLista.textContent =
        `${mensagemSucesso} Porém, não foi possível atualizar a listagem. ${statusLista.textContent}`;
    }
  } catch (erro) {
    statusFormularioDepartamento.classList.add("erro");
    statusFormularioDepartamento.textContent =
      `Não foi possível salvar o departamento. ${erro.message}`;
  } finally {
    botaoSalvarDepartamento.disabled = false;
  }
}

async function alterarStatusDepartamento(id, status, botao) {
  const reativando = status !== "D";
  const departamento = departamentosPorId.get(String(id));
  const nome = departamento?.departamento || `Departamento ${id}`;
  const confirmacao = reativando
    ? `Deseja reativar "${nome}"?`
    : `Deseja desativar "${nome}"? Ele será mantido no banco e poderá ser reativado.`;
  if (!window.confirm(confirmacao)) {
    return;
  }

  const mensagem = document.querySelector('[data-status="departamentos"]');
  botao.disabled = true;
  mensagem.classList.remove("erro");
  mensagem.textContent = reativando
    ? "Reativando departamento..."
    : "Desativando departamento...";

  try {
    if (reativando) {
      await api.alterarStatusDepartamento(id, { status });
    } else {
      await api.excluirDepartamento(id);
    }

    if (!reativando && String(departamentoSelecionado) === String(id)) {
      departamentoSelecionado = null;
      document.querySelector("[data-selected-department]").textContent =
        "Funcionários do departamento";
      document.querySelector("[data-department-employees-status]").textContent =
        "Selecione um departamento para consultar os funcionários.";
      document.querySelector('[data-list="department-employees"]').replaceChildren();
    }

    const atualizouLista = await carregarSecao("departamentos", true);
    if (atualizouLista) {
      mensagem.textContent = reativando
        ? "Departamento reativado com sucesso."
        : "Departamento desativado com sucesso.";
    } else {
      mensagem.textContent =
        `A operação foi concluída, mas a lista não pôde ser atualizada. ${mensagem.textContent}`;
    }
  } catch (erro) {
    mensagem.classList.add("erro");
    mensagem.textContent =
      `Não foi possível ${reativando ? "reativar" : "desativar"} o departamento. ${erro.message}`;
  } finally {
    botao.disabled = false;
  }
}

async function carregarFuncionariosDepartamento(departamento) {
  departamentoSelecionado = departamento;
  const requisicaoAtual = ++carregamentoFuncionariosDepartamento;
  const mensagem = document.querySelector("[data-department-employees-status]");
  const titulo = document.querySelector("[data-selected-department]");
  const lista = document.querySelector('[data-list="department-employees"]');
  const nomeDepartamento = nomesDepartamentos.get(String(departamento))
    || `Departamento ${departamento}`;

  titulo.textContent = `Funcionários de ${nomeDepartamento}`;
  mensagem.classList.remove("erro");
  mensagem.textContent = "Carregando funcionários...";
  lista.replaceChildren();
  lista.setAttribute("aria-busy", "true");

  document.querySelectorAll("[data-department-value]").forEach((botao) => {
    botao.setAttribute("aria-pressed", String(botao.dataset.departmentValue === departamento));
  });

  try {
    const resposta =
      await api.listarFuncionariosPorDepartamento(departamento);
    if (requisicaoAtual !== carregamentoFuncionariosDepartamento) {
      return;
    }

    if (!Array.isArray(resposta)) {
      throw new Error("A API retornou uma lista de funcionários inválida.");
    }

    const funcionariosDoDepartamento = resposta.filter(
      (funcionario) => String(funcionario.departamentoId) === String(departamento)
    );

    if (funcionariosDoDepartamento.length === 0) {
      mensagem.textContent = "Nenhum funcionário cadastrado neste departamento.";
      return;
    }

    mensagem.textContent = `${funcionariosDoDepartamento.length} funcionário(s) encontrado(s).`;
    funcionariosDoDepartamento.forEach((funcionario) => {
      const item = document.createElement("article");
      item.className = "item-dashboard";

      const nome = document.createElement("h3");
      nome.textContent = funcionario.nome || "Nome não informado";
      item.append(nome);

      const turno = document.createElement("p");
      turno.textContent = `Turno: ${funcionario.turno
        ? nomeExibicaoTurno(funcionario.turno)
        : "Não informado"}`;
      item.append(turno);

      const telefone = document.createElement("p");
      telefone.textContent = `Telefone: ${funcionario.telefone || "Não informado"}`;
      item.append(telefone);

      const funcao = document.createElement("p");
      funcao.textContent = `Função: ${funcionario.funcao || "Não informada"}`;
      item.append(funcao);

      lista.append(item);
    });
  } catch (erro) {
    if (requisicaoAtual !== carregamentoFuncionariosDepartamento) {
      return;
    }

    mensagem.classList.add("erro");
    mensagem.textContent = `Não foi possível carregar os funcionários. ${erro.message}`;
  } finally {
    if (requisicaoAtual === carregamentoFuncionariosDepartamento) {
      lista.removeAttribute("aria-busy");
    }
  }
}

async function abrirFormularioFuncionario(funcionario = null) {
  funcionarioEmEdicao = funcionario;
  formularioFuncionario.reset();
  statusFormularioFuncionario.textContent = "";
  statusFormularioFuncionario.classList.remove("erro");

  const editando = funcionario !== null;
  const campoNome = document.querySelector("#funcionario-nome");
  const campoCpf = document.querySelector("#funcionario-cpf");
  document.querySelector("[data-employee-form-title]").textContent =
    editando ? "Editar funcionário" : "Novo funcionário";
  botaoSalvarFuncionario.textContent = editando ? "Salvar alterações" : "Salvar funcionário";
  botaoSalvarFuncionario.disabled = true;
  dialogoFuncionario.showModal();
  statusFormularioFuncionario.textContent = "Carregando opções do formulário...";

  try {
    await carregarOpcoesFuncionario(funcionario);
    campoNome.value = funcionario?.nome || "";
    campoNome.readOnly = false;
    campoCpf.value = funcionario?.cpf || "";
    campoCpf.readOnly = false;
    document.querySelector("#funcionario-telefone").value = funcionario?.telefone || "";
    document.querySelector("#funcionario-endereco").value = funcionario?.endereco || "";
    document.querySelector("#funcionario-funcao").value = funcionario?.funcao || "";
    const selectTurno = document.querySelector("#funcionario-turno");
    selectTurno.value = funcionario?.turnoId != null
      ? String(funcionario.turnoId)
      : funcionario?.turno ? `legacy:${funcionario.turno}` : "";
    document.querySelector("#funcionario-turno-vigencia").value =
      funcionario?.turnoVigenciaInicio
        ? funcionario.turnoVigenciaInicio.slice(0, 16)
        : formatarDataHoraLocal();
    atualizarCampoVigenciaTurno(false);
    const selectDepartamento = document.querySelector("#departamento");
    const departamentoAtual = [...selectDepartamento.options].find((opcao) =>
      opcao.value === String(funcionario?.departamentoId)
      || (funcionario?.departamento && opcao.textContent.toLowerCase()
        === funcionario.departamento.toLowerCase())
    );
    selectDepartamento.value = departamentoAtual?.value || "";
    statusFormularioFuncionario.textContent = "";
    document.querySelector("#funcionario-nome").focus();
  } catch (erro) {
    statusFormularioFuncionario.classList.add("erro");
    statusFormularioFuncionario.textContent =
      `Não foi possível carregar departamentos e turnos. ${erro.message}`;
  } finally {
    botaoSalvarFuncionario.disabled = false;
  }
}

async function salvarFuncionario(evento) {
  evento.preventDefault();

  if (!formularioFuncionario.reportValidity()) {
    return;
  }

  const dadosFormulario = new FormData(formularioFuncionario);
  const editando = funcionarioEmEdicao !== null;
  const turnoSelecionado = String(dadosFormulario.get("turno") || "");
  const turnoId = /^\d+$/.test(turnoSelecionado) ? Number(turnoSelecionado) : null;
  const turnoLegado = turnoSelecionado.startsWith("legacy:")
    ? turnoSelecionado.slice("legacy:".length)
    : turnoSelecionado;
  const funcionario = {
    ...(editando ? { id: funcionarioEmEdicao.id } : {}),
    nome: dadosFormulario.get("nome").trim(),
    cpf: dadosFormulario.get("cpf").trim(),
    telefone: dadosFormulario.get("telefone").trim(),
    endereco: dadosFormulario.get("endereco").trim(),
    funcao: dadosFormulario.get("funcao").trim(),
    turno: turnoLegado,
    turnoId,
    turnoVigenciaInicio: turnoId
      ? dadosFormulario.get("turnoVigenciaInicio")
      : null,
    departamentoId: Number(dadosFormulario.get("departamento"))
  };

  if (!Number.isInteger(funcionario.departamentoId) || funcionario.departamentoId <= 0) {
    statusFormularioFuncionario.classList.add("erro");
    statusFormularioFuncionario.textContent = "Selecione um departamento válido.";
    return;
  }

  botaoSalvarFuncionario.disabled = true;
  statusFormularioFuncionario.classList.remove("erro");
  statusFormularioFuncionario.textContent = funcionarioEmEdicao
    ? "Salvando alterações..."
    : "Cadastrando funcionário...";

  try {
    if (editando) {
      await api.atualizarFuncionario(funcionarioEmEdicao.id, funcionario);
    } else {
      await api.cadastrarFuncionario(funcionario);
    }

    dialogoFuncionario.close();
    await carregarSecao("funcionarios", true);
    const mensagemLista = document.querySelector('[data-status="funcionarios"]');
    if (!mensagemLista.classList.contains("erro")) {
      mensagemLista.textContent = editando
        ? "Funcionário atualizado com sucesso."
        : "Funcionário cadastrado com sucesso.";
    }
    if (departamentoSelecionado) {
      await carregarFuncionariosDepartamento(departamentoSelecionado);
    }
  } catch (erro) {
    statusFormularioFuncionario.classList.add("erro");
    statusFormularioFuncionario.textContent =
      `Não foi possível salvar o funcionário. ${erro.message}`;
  } finally {
    botaoSalvarFuncionario.disabled = false;
  }
}

async function carregarSecao(secao, atualizar = false) {
  if (!secoesDashboard[secao]) {
    return false;
  }

  if (secoesCarregando.has(secao)) {
    if (atualizar) {
      secoesParaAtualizar.add(secao);
    }
    return false;
  }

  if (secoesCarregadas.has(secao) && !atualizar) {
    return true;
  }

  const mensagem = document.querySelector(`[data-status="${secao}"]`);
  const botao = document.querySelector(`[data-refresh="${secao}"]`);
  mensagem.classList.remove("erro");
  mensagem.textContent = "Carregando...";
  botao.disabled = true;
  secoesCarregando.add(secao);

  try {
    const registros = await secoesDashboard[secao].carregar();
    if (!Array.isArray(registros)) {
      throw new Error("A API retornou uma lista inválida.");
    }
    mostrarRegistros(secao, registros);
    secoesCarregadas.add(secao);
    return true;
  } catch (erro) {
    mensagem.classList.add("erro");
    mensagem.textContent = `Não foi possível carregar os dados. ${erro.message}`;
    return false;
  } finally {
    secoesCarregando.delete(secao);
    botao.disabled = false;
    if (secoesParaAtualizar.delete(secao)) {
      void carregarSecao(secao, true);
    }
  }
}

document.querySelectorAll("[data-refresh]").forEach((botao) => {
  botao.addEventListener("click", () => carregarSecao(botao.dataset.refresh, true));
});

document.querySelector("[data-add-employee]").addEventListener("click", () => {
  void abrirFormularioFuncionario();
});

document.querySelector("[data-add-shift]").addEventListener("click", () => {
  abrirFormularioTurno();
});

document.querySelector("#turno-tipo-escala").addEventListener("change", () => {
  atualizarCamposEscala(!turnoEmEdicao);
});

[
  "#turno-inicio",
  "#turno-fim"
].forEach((seletor) => {
  document.querySelector(seletor).addEventListener("input", atualizarPreviaCargaJornada);
});

listaIntervalosTurno.addEventListener("input", atualizarPreviaCargaJornada);
listaIntervalosTurno.addEventListener("click", (evento) => {
  const botaoRemover = evento.target.closest("[data-remove-break]");
  if (!botaoRemover) {
    return;
  }

  const linha = botaoRemover.closest("[data-break-row]");
  if (listaIntervalosTurno.querySelectorAll("[data-break-row]").length === 1) {
    linha.querySelectorAll("input").forEach((input) => {
      input.value = "";
    });
  } else {
    linha.remove();
  }
  atualizarPreviaCargaJornada();
});

document.querySelector("[data-add-break]").addEventListener("click", () => {
  const novoIntervalo = criarCampoIntervaloTurno();
  listaIntervalosTurno.append(novoIntervalo);
  novoIntervalo.querySelector("input").focus();
});

document.querySelectorAll('[name="diasSemana"]').forEach((checkbox) => {
  checkbox.addEventListener("change", atualizarPreviaCargaJornada);
});

document.querySelector("#funcionario-turno").addEventListener("change", () => {
  atualizarCampoVigenciaTurno(true);
});

document.querySelector("[data-add-department]").addEventListener("click", () => {
  abrirFormularioDepartamento();
});

document.querySelector('[data-list="turnos"]').addEventListener("click", async (evento) => {
  const botaoEditar = evento.target.closest("[data-edit-shift]");
  if (botaoEditar) {
    const id = botaoEditar.dataset.editShift;
    const mensagem = document.querySelector('[data-status="turnos"]');
    botaoEditar.disabled = true;
    mensagem.classList.remove("erro");
    mensagem.textContent = "Carregando os dados do turno...";

    try {
      const turno = await api.buscarTurnoPorId(id);
      abrirFormularioTurno(turno);
      mensagem.textContent = "Dados do turno carregados.";
    } catch (erro) {
      mensagem.classList.add("erro");
      mensagem.textContent = `Não foi possível carregar o turno para edição. ${erro.message}`;
    } finally {
      botaoEditar.disabled = false;
    }
    return;
  }

  const botaoDesativar = evento.target.closest("[data-deactivate-shift]");
  if (botaoDesativar) {
    await alterarStatusTurno(botaoDesativar.dataset.deactivateShift, "D", botaoDesativar);
    return;
  }

  const botaoReativar = evento.target.closest("[data-reactivate-shift]");
  if (botaoReativar) {
    await alterarStatusTurno(botaoReativar.dataset.reactivateShift, "A", botaoReativar);
  }
});

document.querySelector('[data-list="funcionarios"]').addEventListener("click", async (evento) => {
  const botaoEditar = evento.target.closest("[data-edit-employee]");
  if (!botaoEditar) {
    return;
  }

  const id = botaoEditar.dataset.editEmployee;
  const mensagem = document.querySelector('[data-status="funcionarios"]');
  botaoEditar.disabled = true;
  mensagem.classList.remove("erro");
  mensagem.textContent = "Carregando os dados do funcionário...";

  try {
    const funcionario = await api.buscarFuncionarioPorId(id);
    await abrirFormularioFuncionario(funcionario);
    mensagem.textContent = "Dados do funcionário carregados.";
  } catch (erro) {
    mensagem.classList.add("erro");
    mensagem.textContent = `Não foi possível carregar o funcionário para edição. ${erro.message}`;
  } finally {
    botaoEditar.disabled = false;
  }
});

document.querySelector('[data-list="departamentos"]').addEventListener("click", async (evento) => {
  const botaoEditar = evento.target.closest("[data-edit-department]");
  if (botaoEditar) {
    const id = botaoEditar.dataset.editDepartment;
    const mensagem = document.querySelector('[data-status="departamentos"]');
    botaoEditar.disabled = true;
    mensagem.classList.remove("erro");
    mensagem.textContent = "Carregando os dados do departamento...";

    try {
      const departamento = await api.buscarDepartamentoPorId(id);
      abrirFormularioDepartamento(departamento);
      mensagem.textContent = "Dados do departamento carregados.";
    } catch (erro) {
      mensagem.classList.add("erro");
      mensagem.textContent =
        `Não foi possível carregar o departamento para edição. ${erro.message}`;
    } finally {
      botaoEditar.disabled = false;
    }
    return;
  }

  const botaoDesativar = evento.target.closest("[data-deactivate-department]");
  if (botaoDesativar) {
    await alterarStatusDepartamento(
      botaoDesativar.dataset.deactivateDepartment,
      "D",
      botaoDesativar
    );
    return;
  }

  const botaoReativar = evento.target.closest("[data-reactivate-department]");
  if (botaoReativar) {
    await alterarStatusDepartamento(
      botaoReativar.dataset.reactivateDepartment,
      "A",
      botaoReativar
    );
    return;
  }

  const botaoDepartamento = evento.target.closest("[data-department-value]");
  if (botaoDepartamento) {
    void carregarFuncionariosDepartamento(botaoDepartamento.dataset.departmentValue);
  }
});

formularioFuncionario.addEventListener("submit", salvarFuncionario);
formularioTurno.addEventListener("submit", salvarTurno);
formularioDepartamento.addEventListener("submit", salvarDepartamento);

document.querySelectorAll("[data-close-employee-dialog]").forEach((botao) => {
  botao.addEventListener("click", () => dialogoFuncionario.close());
});

document.querySelectorAll("[data-close-shift-dialog]").forEach((botao) => {
  botao.addEventListener("click", () => dialogoTurno.close());
});

document.querySelectorAll("[data-close-department-dialog]").forEach((botao) => {
  botao.addEventListener("click", () => dialogoDepartamento.close());
});

document.querySelector("[data-logout]").addEventListener("click", () => {
  localStorage.removeItem("token");
});

window.addEventListener("dashboard:viewchange", (evento) => {
  carregarSecao(evento.detail.view);
});
