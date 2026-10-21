const turnosDisponiveis = [
  { value: "COMERCIAL", periodo: "Comercial", horario: "08:00 às 18:00" },
  { value: "NOTURNO_A", periodo: "Noturno A", horario: "18:00 às 08:00" },
  { value: "NOTURNO_B", periodo: "Noturno B", horario: "18:00 às 08:00" }
];

const departamentosDisponiveis = [
  { value: "Financeiro", setor: "Financeiro" },
  { value: "Comercial", setor: "Comercial" },
  { value: "RH", setor: "RH" },
  { value: "Logística", setor: "Logística" },
  { value: "Cozinha", setor: "Cozinha" }
];

const nomesTurnos = Object.fromEntries(
  turnosDisponiveis.map(({ value, periodo, horario }) => [value, `${periodo} (${horario})`])
);
const nomesDepartamentos = Object.fromEntries(
  departamentosDisponiveis.map(({ value, setor }) => [value, setor])
);

function normalizarDepartamento(valor) {
  return typeof valor === "string" ? valor.toLowerCase().trim() : "";
}

const secoesDashboard = {
  inicio: {
    carregar: () => api.listarUsuarios(),
    nomeItem: (item) => item.nome || `Usuário ${item.id}`,
    campos: [
      ["cpf", "CPF"],
      ["razaoSocialEmpresa", "Empresa"]
    ]
  },
  turnos: {
    carregar: () => turnosDisponiveis,
    nomeItem: (item) => item.periodo || `Turno ${item.id}`,
    campos: [["horario", "Horário"]],
    descricaoRegistros: "opções disponíveis"
  },
  departamentos: {
    carregar: () => departamentosDisponiveis,
    nomeItem: (item) => item.setor || `Departamento ${item.id}`,
    campos: [],
    descricaoRegistros: "opções disponíveis"
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
let departamentoSelecionado = null;
let carregamentoFuncionariosDepartamento = 0;

const dialogoFuncionario = document.querySelector("[data-employee-dialog]");
const formularioFuncionario = document.querySelector("[data-employee-form]");
const statusFormularioFuncionario = document.querySelector("[data-employee-form-status]");
const botaoSalvarFuncionario = document.querySelector("[data-employee-submit]");
let funcionarioEmEdicao = null;

function preencherOpcoes(select, opcoes) {
  opcoes.forEach(({ value, label }) => {
    const opcao = document.createElement("option");
    opcao.value = value;
    opcao.textContent = label;
    select.append(opcao);
  });
}

preencherOpcoes(
  document.querySelector("#funcionario-turno"),
  turnosDisponiveis.map(({ value }) => ({ value, label: nomesTurnos[value] }))
);
preencherOpcoes(
  document.querySelector("#departamento"),
  departamentosDisponiveis.map(({ value, setor }) => ({ value, label: setor }))
);

function mostrarRegistros(secao, registros) {
  const configuracao = secoesDashboard[secao];
  const lista = document.querySelector(`[data-list="${secao}"]`);
  const mensagem = document.querySelector(`[data-status="${secao}"]`);
  lista.replaceChildren();

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
      const botaoDepartamento = document.createElement("button");
      botaoDepartamento.type = "button";
      botaoDepartamento.className = "botao-departamento";
      botaoDepartamento.dataset.departmentValue = registro.value;
      botaoDepartamento.textContent = registro.setor;
      botaoDepartamento.setAttribute(
        "aria-pressed",
        String(departamentoSelecionado === registro.value)
      );
      lista.append(botaoDepartamento);
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
      const nomes = campo === "turno"
        ? nomesTurnos
        : campo === "departamento"
          ? nomesDepartamentos
          : null;
      detalhe.textContent = `${rotulo}: ${nomes?.[registro[campo]] || registro[campo]}`;
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

async function carregarFuncionariosDepartamento(departamento) {
  departamentoSelecionado = departamento;
  const requisicaoAtual = ++carregamentoFuncionariosDepartamento;
  const mensagem = document.querySelector("[data-department-employees-status]");
  const titulo = document.querySelector("[data-selected-department]");
  const lista = document.querySelector('[data-list="department-employees"]');
  const nomeDepartamento = nomesDepartamentos[departamento];

  titulo.textContent = `Funcionários de ${nomeDepartamento}`;
  mensagem.classList.remove("erro");
  mensagem.textContent = "Carregando funcionários...";
  lista.replaceChildren();

  document.querySelectorAll("[data-department-value]").forEach((botao) => {
    botao.setAttribute("aria-pressed", String(botao.dataset.departmentValue === departamento));
    botao.disabled = true;
  });

  try {
    const todosFuncionarios = await api.listarFuncionarios();
    if (requisicaoAtual !== carregamentoFuncionariosDepartamento) {
      return;
    }

    if (!Array.isArray(todosFuncionarios)) {
      throw new Error("A API retornou uma lista de funcionários inválida.");
    }

    const departamentoNormalizado = normalizarDepartamento(departamento);
    const funcionariosDoDepartamento = todosFuncionarios.filter((funcionario) => {
      const departamentoFuncionario = normalizarDepartamento(funcionario.departamento);
      return departamentoFuncionario !== "" && departamentoFuncionario === departamentoNormalizado;
    });

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
      turno.textContent = `Turno: ${nomesTurnos[funcionario.turno] || funcionario.turno || "Não informado"}`;
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

function abrirFormularioFuncionario(funcionario = null) {
  funcionarioEmEdicao = funcionario;
  formularioFuncionario.reset();
  statusFormularioFuncionario.textContent = "";
  statusFormularioFuncionario.classList.remove("erro");

  const editando = funcionario !== null;
  const campoNome = document.querySelector("#funcionario-nome");
  const campoCpf = document.querySelector("#funcionario-cpf");
  document.querySelector("[data-employee-form-title]").textContent =
    editando ? "Editar funcionário" : "Novo funcionário";
  campoNome.value = funcionario?.nome || "";
  campoNome.readOnly = editando;
  campoCpf.value = funcionario?.cpf || "";
  campoCpf.readOnly = editando;
  document.querySelector("#funcionario-telefone").value = funcionario?.telefone || "";
  document.querySelector("#funcionario-endereco").value = funcionario?.endereco || "";
  document.querySelector("#funcionario-funcao").value = funcionario?.funcao || "";
  document.querySelector("#funcionario-turno").value = funcionario?.turno || "";
  document.querySelector("#departamento").value = funcionario?.departamento || "";
  botaoSalvarFuncionario.textContent = editando ? "Salvar alterações" : "Salvar funcionário";

  dialogoFuncionario.showModal();
  document.querySelector(editando ? "#funcionario-telefone" : "#funcionario-nome").focus();
}

async function salvarFuncionario(evento) {
  evento.preventDefault();

  if (!formularioFuncionario.reportValidity()) {
    return;
  }

  const dadosFormulario = new FormData(formularioFuncionario);
  const dataAtual = new Date().toISOString();
  const editando = funcionarioEmEdicao !== null;
  const funcionario = {
    ...(editando ? { id: funcionarioEmEdicao.id } : {}),
    nome: editando ? funcionarioEmEdicao.nome : dadosFormulario.get("nome").trim(),
    cpf: editando ? funcionarioEmEdicao.cpf : dadosFormulario.get("cpf").trim(),
    telefone: dadosFormulario.get("telefone").trim(),
    endereco: dadosFormulario.get("endereco").trim(),
    funcao: dadosFormulario.get("funcao").trim(),
    turno: dadosFormulario.get("turno"),
    departamento: dadosFormulario.get("departamento"),
    dataCadastro: editando ? funcionarioEmEdicao.dataCadastro : dataAtual
  };

  if (editando) {
    funcionario.status = funcionarioEmEdicao.status;
    funcionario.dataAtualizacao = dataAtual;
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
    return;
  }

  if (secoesCarregando.has(secao)) {
    if (atualizar) {
      secoesParaAtualizar.add(secao);
    }
    return;
  }

  if (secoesCarregadas.has(secao) && !atualizar) {
    return;
  }

  const mensagem = document.querySelector(`[data-status="${secao}"]`);
  const botao = document.querySelector(`[data-refresh="${secao}"]`);
  mensagem.classList.remove("erro");
  mensagem.textContent = "Carregando...";
  botao.disabled = true;
  secoesCarregando.add(secao);

  try {
    const registros = await secoesDashboard[secao].carregar();
    mostrarRegistros(secao, registros);
    secoesCarregadas.add(secao);
  } catch (erro) {
    mensagem.classList.add("erro");
    mensagem.textContent = `Não foi possível carregar os dados. ${erro.message}`;
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
  abrirFormularioFuncionario();
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
    abrirFormularioFuncionario(funcionario);
    mensagem.textContent = "Dados do funcionário carregados.";
  } catch (erro) {
    mensagem.classList.add("erro");
    mensagem.textContent = `Não foi possível carregar o funcionário para edição. ${erro.message}`;
  } finally {
    botaoEditar.disabled = false;
  }
});

document.querySelector('[data-list="departamentos"]').addEventListener("click", (evento) => {
  const botaoDepartamento = evento.target.closest("[data-department-value]");
  if (botaoDepartamento) {
    void carregarFuncionariosDepartamento(botaoDepartamento.dataset.departmentValue);
  }
});

formularioFuncionario.addEventListener("submit", salvarFuncionario);

document.querySelectorAll("[data-close-employee-dialog]").forEach((botao) => {
  botao.addEventListener("click", () => dialogoFuncionario.close());
});

window.addEventListener("dashboard:viewchange", (evento) => {
  carregarSecao(evento.detail.view);
});
