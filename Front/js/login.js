const formularioLogin = document.querySelector("#login-form");
const mensagemLogin = document.querySelector("#mensagem-login");
const botaoEntrar = document.querySelector("#botao-entrar");

if (new URLSearchParams(window.location.search).has("sessionExpired")) {
  mensagemLogin.classList.add("erro");
  mensagemLogin.textContent = "Sua sessão expirou. Entre novamente.";
}

formularioLogin.addEventListener("submit", async (evento) => {
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