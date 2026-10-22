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
    const token = localStorage.getItem("token");
    const cabecalhos = new Headers(opcoes.headers || {});
    cabecalhos.set("Accept", "application/json");

    if (opcoes.body) {
      cabecalhos.set("Content-Type", "application/json");
    }
    if (token && caminho !== "/login") {
      cabecalhos.set("Authorization", `Bearer ${token}`);
    }

    resposta = await fetch(url, { ...opcoes, headers: cabecalhos });
  } catch (erro) {
    const mensagem =
      "Não foi possível conectar ao servidor. Verifique se a API está ativa e se o CORS permite este endereço.";
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
      : dados?.mensagem || dados?.message || dados?.detail;
    const mensagemExibida =
      mensagem || ({
        400: "Os dados enviados são inválidos. Confira os campos e tente novamente.",
        401: caminho === "/login"
          ? "Login ou senha inválidos."
          : "Sua sessão expirou. Entre novamente.",
        403: "Você não tem permissão para realizar esta operação.",
        404: "O registro solicitado não foi encontrado.",
        409: "Este registro já existe ou conflita com outro cadastro.",
        500: "O servidor encontrou um erro. Tente novamente mais tarde."
      })[resposta.status]
        || `A API retornou um erro (HTTP ${resposta.status}).`;

    console.error(
      `[API] Resposta de erro em ${metodo} ${url}: HTTP ${resposta.status}.`
    );

    if (resposta.status === 401 && caminho !== "/login") {
      localStorage.removeItem("token");
      window.location.replace("index.html?sessionExpired=1");
    }

    mostrarErroApi(mensagemExibida);

    const erroResposta = new Error(mensagemExibida);
    erroResposta.name = "ApiResponseError";
    erroResposta.status = resposta.status;
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

  buscarFuncionarioPorId(id) {
    return this.get(`/funcionario/${encodeURIComponent(id)}`);
  },

  cadastrarFuncionario(funcionario) {
    return this.post("/funcionario", funcionario);
  },

  atualizarFuncionario(id, funcionario) {
    return this.put(`/funcionario/${encodeURIComponent(id)}`, funcionario);
  },

  listarDepartamentos() {
    return this.get("/departamentos");
  },

  buscarDepartamentoPorId(id) {
    return this.get(`/departamentos/${encodeURIComponent(id)}`);
  },

  cadastrarDepartamento(departamento) {
    return this.post("/departamentos", departamento);
  },

  atualizarDepartamento(id, departamento) {
    return this.put(`/departamentos/${encodeURIComponent(id)}`, departamento);
  },

  alterarStatusDepartamento(id, dados) {
    return this.patch(`/departamentos/${encodeURIComponent(id)}/status`, dados);
  },

  excluirDepartamento(id) {
    return this.delete(`/departamentos/${encodeURIComponent(id)}`);
  },

  listarFuncionariosPorDepartamento(id) {
    return this.get(`/funcionario/${encodeURIComponent(id)}/departamento`);
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
