const nomesTurnos = new Map();
const nomesDepartamentos = new Map();

function nomeExibicaoTurno(valor) {
  const chave = String(valor);
  return [...nomesTurnos.entries()].find(
    ([nome]) => nome.toLowerCase() === chave.toLowerCase()
  )?.[1] || valor;
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
    nomeItem: (item) => item.periodo || `Turno ${item.id}`,
    campos: [
      ["horario", "Horário"],
      ["horaExtra", "Horas extras"],
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

  const turnoAtual = String(funcionario?.turno || "").toLowerCase();
  const opcoesTurno = turnos
    .filter((turno) => turno.status !== "D"
      || String(turno.periodo || "").toLowerCase() === turnoAtual)
    .map((turno) => {
    const valor = turno.periodo || String(turno.id);
    const horario = turno.horario ? ` (${turno.horario})` : "";
    const label = `${turno.periodo || `Turno ${turno.id}`}${horario}`;
    nomesTurnos.set(valor, label);
    return { value: valor, label };
  });

  const opcoesDepartamento = departamentos
    .filter((departamento) => departamento.status !== "D"
      || String(departamento.id) === String(funcionario?.departamentoId))
    .map((departamento) => {
    const label = departamento.departamento || `Departamento ${departamento.id}`;
    nomesDepartamentos.set(String(departamento.id), label);
    return { value: String(departamento.id), label };
    });

  preencherOpcoes(document.querySelector("#funcionario-turno"), opcoesTurno);
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
    registros.forEach((turno) => {
      const valor = turno.periodo || String(turno.id);
      const horario = turno.horario ? ` (${turno.horario})` : "";
      nomesTurnos.set(valor, `${turno.periodo || `Turno ${turno.id}`}${horario}`);
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
      detalhe.textContent = `${rotulo}: ${nomeTurno || registro[campo]}`;
      item.append(detalhe);
    });

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

  document.querySelectorAll("[data-department-value]").forEach((botao) => {
    botao.setAttribute("aria-pressed", String(botao.dataset.departmentValue === departamento));
    botao.disabled = true;
  });

  try {
    const funcionariosDoDepartamento =
      await api.listarFuncionariosPorDepartamento(departamento);
    if (requisicaoAtual !== carregamentoFuncionariosDepartamento) {
      return;
    }

    if (!Array.isArray(funcionariosDoDepartamento)) {
      throw new Error("A API retornou uma lista de funcionários inválida.");
    }

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
      document.querySelectorAll("[data-department-value]").forEach((botao) => {
        botao.disabled = false;
      });
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
    campoNome.readOnly = editando;
    campoCpf.value = funcionario?.cpf || "";
    campoCpf.readOnly = editando;
    document.querySelector("#funcionario-telefone").value = funcionario?.telefone || "";
    document.querySelector("#funcionario-endereco").value = funcionario?.endereco || "";
    document.querySelector("#funcionario-funcao").value = funcionario?.funcao || "";
    const selectTurno = document.querySelector("#funcionario-turno");
    const valorTurno = funcionario?.turno == null ? "" : String(funcionario.turno);
    const opcaoTurnoAtual = [...selectTurno.options].find(
      (opcao) => opcao.value.toLowerCase() === valorTurno.toLowerCase()
    );
    if (valorTurno && !opcaoTurnoAtual) {
      const opcaoLegada = document.createElement("option");
      opcaoLegada.value = valorTurno;
      opcaoLegada.textContent = `${valorTurno} (valor atual)`;
      selectTurno.append(opcaoLegada);
      selectTurno.value = valorTurno;
    } else {
      selectTurno.value = opcaoTurnoAtual?.value || "";
    }
    const selectDepartamento = document.querySelector("#departamento");
    const departamentoAtual = [...selectDepartamento.options].find((opcao) =>
      opcao.value === String(funcionario?.departamentoId)
      || (funcionario?.departamento && opcao.textContent.toLowerCase()
        === funcionario.departamento.toLowerCase())
    );
    selectDepartamento.value = departamentoAtual?.value || "";
    statusFormularioFuncionario.textContent = "";
    document.querySelector(editando ? "#funcionario-telefone" : "#funcionario-nome").focus();
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
  const funcionario = {
    ...(editando ? { id: funcionarioEmEdicao.id } : {}),
    nome: editando ? funcionarioEmEdicao.nome : dadosFormulario.get("nome").trim(),
    cpf: editando ? funcionarioEmEdicao.cpf : dadosFormulario.get("cpf").trim(),
    telefone: dadosFormulario.get("telefone").trim(),
    endereco: dadosFormulario.get("endereco").trim(),
    funcao: dadosFormulario.get("funcao").trim(),
    turno: dadosFormulario.get("turno"),
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

document.querySelector("[data-add-department]").addEventListener("click", () => {
  abrirFormularioDepartamento();
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
formularioDepartamento.addEventListener("submit", salvarDepartamento);

document.querySelectorAll("[data-close-employee-dialog]").forEach((botao) => {
  botao.addEventListener("click", () => dialogoFuncionario.close());
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
