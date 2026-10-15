const API_BASE_URL = "http://localhost:8080";

function mostrarErroApi(mensagem) {
  const elemento = document.querySelector("#mensagem-login")
    || document.querySelector(".dashboard-panel:not([hidden]) [data-status]");

  if (elemento) {
    elemento.textContent = mensagem;
    elemento.classList.add("erro");
  }
}

async function requisicao(caminho, opcoes = {}) {
  const url = `${API_BASE_URL}${caminho}`;
  const metodo = opcoes.method || "GET";
  let resposta;

  try {
    resposta = await fetch(url, {
      ...opcoes,
      headers: {
        Accept: "application/json",
        ...(opcoes.body ? { "Content-Type": "application/json" } : {}),
        ...opcoes.headers
      }
    });
  } catch (erro) {
    const mensagem = "Não foi possível conectar ao servidor. Verifique se a API está ativa e se o CORS permite este endereço.";
    console.error(`[API] Falha de conexão ou CORS em ${metodo} ${url}.`, erro);
    mostrarErroApi(mensagem);

    const erroConexao = new Error(mensagem);
    erroConexao.name = "ApiConnectionError";
    throw erroConexao;
  }

  const texto = await resposta.text();
  let dados = null;

  if (texto) {
    try {
      dados = JSON.parse(texto);
    } catch {
      dados = texto;
    }
  }

  if (!resposta.ok) {
    const mensagem = typeof dados === "string"
      ? dados
      : dados?.mensagem || dados?.message;
    const mensagemExibida = mensagem || `A API retornou um erro (HTTP ${resposta.status}).`;
    console.error(`[API] Resposta de erro em ${metodo} ${url}: HTTP ${resposta.status}.`, dados);
    mostrarErroApi(mensagemExibida);

    const erroResposta = new Error(mensagemExibida);
    erroResposta.name = "ApiResponseError";
    throw erroResposta;
  }

  return dados;
}

const api = {
  get(caminho) {
    return requisicao(caminho);
  },

  post(caminho, dados) {
    return requisicao(caminho, {
      method: "POST",
      body: JSON.stringify(dados)
    });
  },

  put(caminho, dados) {
    return requisicao(caminho, {
      method: "PUT",
      body: JSON.stringify(dados)
    });
  },

  patch(caminho, dados) {
    return requisicao(caminho, {
      method: "PATCH",
      body: JSON.stringify(dados)
    });
  },

  delete(caminho) {
    return requisicao(caminho, { method: "DELETE" });
  },

  listarUsuarios() {
    return this.get("/usuarios");
  },

  buscarUsuarioPorId(id) {
    return this.get(`/usuarios/${encodeURIComponent(id)}`);
  },

  cadastrarUsuario(usuario) {
    return this.post("/usuarios", usuario);
  },

  atualizarUsuario(id, usuario) {
    return this.put(`/usuarios/${encodeURIComponent(id)}`, usuario);
  },

  alterarStatusUsuario(id, dados) {
    return this.patch(`/usuarios/${encodeURIComponent(id)}/status`, dados);
  },

  listarTurnos() {
    return this.get("/turnos");
  },

  listarFuncionarios() {
    return this.get("/funcionario");
  },

  listarDepartamentos() {
    return this.get("/departamentos");
  },

  listarEmpresas() {
    return this.get("/empresas");
  },

  listarGerentes() {
    return this.get("/gerentes");
  },

  login(dados) {
    return this.post("/login", dados);
  }
};
