# ShiftSync

## Configuração local

O back-end requer o JDK 25 (LTS) e usa PostgreSQL. Configure estas variáveis de ambiente antes de iniciar a API:

- `DB_PASSWORD`: senha do usuário do banco.
- `JWT_SECRET`: segredo usado para assinar e validar os tokens JWT.
- `DB_URL` (opcional): URL JDBC; o padrão aponta para `localhost:5433/shiftsync`.
- `DB_USERNAME` (opcional): usuário do banco; o padrão é `postgres`.
- `JWT_TEMPO_MINUTOS` (opcional): duração do token; o padrão é 30 minutos.
- `JWT_ISSUER` (opcional): emissor do token; o padrão é `shiftsync`.

No PowerShell, defina as variáveis e execute o servidor na mesma janela:

```powershell
$env:DB_PASSWORD = "sua-senha-local"
$env:JWT_SECRET = "seu-segredo-local"
.\mvnw.cmd spring-boot:run
```

O Hibernate permanece configurado com `ddl-auto=update`; a aplicação não recria o banco.
As senhas dos novos usuários são armazenadas com BCrypt. Senhas antigas em texto puro
são convertidas para BCrypt na primeira autenticação válida.

## Swagger e interface

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Abra `Front/html/index.html` com a extensão Live Server do VS Code
  (origem `http://127.0.0.1:5500` ou `http://localhost:5500`).

As rotas protegidas exigem `Authorization: Bearer <token>`. Departamentos e turnos
exibidos no painel e no formulário de funcionários são carregados da API.