const formularioLogin = document.querySelector("#login-form");
const mensagemLogin = document.querySelector("#mensagem-login");
const botaoEntrar = document.querySelector("#botao-entrar");

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

    if (resultado.mensagem !== "Bem vindo") {
      const mensagem = resultado.mensagem || "Login ou senha inválidos.";
      console.warn("[Login] A API recusou a autenticação:", mensagem);
      throw new Error(mensagem);
    }

    loginAceito = true;
    mensagemLogin.textContent = resultado.mensagem;
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