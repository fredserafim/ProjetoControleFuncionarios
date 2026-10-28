// O menu usa o fragmento da URL como identificador da seção ativa.
const secoesMenu = new Set(["inicio", "turnos", "departamentos", "funcionarios"]);
const tituloPagina = document.querySelector("#titulo-pagina");
const descricaoPagina = document.querySelector("#descricao-pagina");

function ativarSecao() {
  const hash = window.location.hash.slice(1);
  const secaoAtiva = secoesMenu.has(hash) ? hash : "inicio";

  // Oculta painéis inativos, atualiza acessibilidade/título e avisa main.js.
  document.querySelectorAll("[data-panel]").forEach((painel) => {
    painel.hidden = painel.dataset.panel !== secaoAtiva;
  });

  document.querySelectorAll("[data-menu-item]").forEach((link) => {
    if (link.dataset.menuItem === secaoAtiva) {
      link.setAttribute("aria-current", "page");
      tituloPagina.textContent = link.dataset.title;
      descricaoPagina.textContent = link.dataset.description;
    } else {
      link.removeAttribute("aria-current");
    }
  });

  window.dispatchEvent(new CustomEvent("dashboard:viewchange", {
    detail: { view: secaoAtiva }
  }));
}

window.addEventListener("hashchange", ativarSecao);

if (!secoesMenu.has(window.location.hash.slice(1))) {
  window.history.replaceState(null, "", "#inicio");
}

ativarSecao();