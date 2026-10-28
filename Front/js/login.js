// Liga o formulário de acesso ao endpoint /login e controla o estado da tela.
const formularioLogin = document.querySelector("#login-form");
const mensagemLogin = document.querySelector("#mensagem-login");
const botaoEntrar = document.querySelector("#botao-entrar");

if (new URLSearchParams(window.location.search).has("sessionExpired")) {
  mensagemLogin.classList.add("erro");
  mensagemLogin.textContent = "Sua sessão expirou. Entre novamente.";
}

formularioLogin.addEventListener("submit", async (evento) => {
  // Evita o envio tradicional do navegador para poder aguardar a resposta REST.
  evento.preventDefault();

  mensagemLogin.classList.remove("erro");
  mensagemLogin.textContent = "";
  botaoEntrar.disabled = true;
  botaoEntrar.textContent = "Entrando...";

  const dadosFormulario = new FormData(formularioLogin);
  const credenciais = {
    login: dadosFormulario.get("login").trim(),
    senha: dadosFormulario.get("senha")
  };
  let loginAceito = false;

  try {
    // O token retornado é guardado para que api.js o envie nas próximas chamadas.
    const resultado = await api.login(credenciais);

    if (!resultado?.token) {
      throw new Error("A API não retornou o token de autenticação.");
    }

    localStorage.setItem("token", resultado.token);
    loginAceito = true;
    mensagemLogin.textContent = resultado.mensagem || "Bem-vindo.";
    window.setTimeout(() => {
      window.location.href = "dashboard.html";
    }, 700);
  } catch (erro) {
    mensagemLogin.classList.add("erro");
    mensagemLogin.textContent = erro.message || "Não foi possível entrar. Tente novamente.";
  } finally {
    if (!loginAceito) {
      botaoEntrar.disabled = false;
      botaoEntrar.textContent = "Entrar";
    }
  }
});