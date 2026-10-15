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
    carregar: () => api.listarTurnos(),
    nomeItem: (item) => item.periodo || `Turno ${item.id}`,
    campos: [
      ["horario", "Horário"],
      ["horaExtra", "Hora extra"],
      ["status", "Status"]
    ]
  },
  departamentos: {
    carregar: () => api.listarDepartamentos(),
    nomeItem: (item) => item.setor || `Departamento ${item.id}`,
    campos: [["status", "Status"]]
  },
  funcionarios: {
    carregar: () => api.listarFuncionarios(),
    nomeItem: (item) => item.nome || `Funcionário ${item.id}`,
    campos: [
      ["funcao", "Função"],
      ["status", "Status"]
    ]
  }
};

const secoesCarregadas = new Set();
const secoesCarregando = new Set();

function mostrarRegistros(secao, registros) {
  const configuracao = secoesDashboard[secao];
  const lista = document.querySelector(`[data-list="${secao}"]`);
  const mensagem = document.querySelector(`[data-status="${secao}"]`);
  lista.replaceChildren();

  if (!Array.isArray(registros) || registros.length === 0) {
    mensagem.textContent = "Nenhum registro encontrado.";
    return;
  }

  mensagem.textContent = `${registros.length} registro(s) encontrado(s).`;

  registros.forEach((registro) => {
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
      detalhe.textContent = `${rotulo}: ${registro[campo]}`;
      item.append(detalhe);
    });

    lista.append(item);
  });
}

async function carregarSecao(secao, atualizar = false) {
  if (!secoesDashboard[secao] || secoesCarregando.has(secao)) {
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
  }
}

document.querySelectorAll("[data-refresh]").forEach((botao) => {
  botao.addEventListener("click", () => carregarSecao(botao.dataset.refresh, true));
});

window.addEventListener("dashboard:viewchange", (evento) => {
  carregarSecao(evento.detail.view);
});
