# ShiftSync

Aplicação web para apoiar o controle de funcionários, jornadas de trabalho e
registros de ponto. O projeto é composto por uma API REST em Java/Spring Boot e
uma interface estática em HTML, CSS e JavaScript.

## Funcionalidades principais

- Autenticação de usuários por CPF e senha, com emissão de token JWT.
- Cadastro e consulta de empresas, usuários, gerentes, departamentos e funcionários.
- Validação de CPF no cadastro e na edição de funcionários.
- Cadastro e consulta de turnos, com jornadas, escalas e ocorrências planejadas.
- Atribuição de jornadas a funcionários com vigência e histórico.
- Registro e consulta de marcações de ponto, incluindo período e resumo mensal.
- Apuração de horas extras e acompanhamento de jornadas realizadas ou pendentes.
- Consulta de endereço por CEP.
- Documentação interativa da API com Swagger UI.
- Senhas novas armazenadas com BCrypt. Senhas legadas em texto puro são
  convertidas para BCrypt após uma autenticação válida.

## Requisitos

- JDK 17 ou superior.
- PostgreSQL.
- Navegador moderno.
- Para abrir a interface localmente, VS Code com a extensão Live Server (ou
  outro servidor estático configurado na porta 5500).

O Maven Wrapper (`mvnw.cmd`) está incluído; não é necessário instalar Maven
separadamente.

## Configuração do banco e da API

A configuração padrão da aplicação aponta para:

- URL: `jdbc:postgresql://localhost:5433/shiftsync`
- Usuário: `postgres`
- Banco de dados: `shiftsync`
- API: `http://localhost:8080`

Crie o banco `shiftsync` no PostgreSQL e confirme que o servidor está acessível
na porta configurada. Se sua instalação usar outra porta, sobrescreva a URL
conforme o exemplo abaixo. O Hibernate está configurado com `ddl-auto=update` e
atualiza as tabelas ao iniciar; mantenha backups do banco e não use essa
configuração como estratégia de migração em produção.

No PowerShell, defina as configurações na mesma janela em que iniciará a API.
Use uma senha de banco e um segredo JWT próprios para o ambiente:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "sua-senha-local-do-postgresql"
$env:SPRING_SECRET = "gere-um-segredo-forte-e-privado"

# Opcionais: use se os valores padrão não corresponderem ao seu ambiente.
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5433/shiftsync"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_TEMPO = "120"
$env:SPRING_EMISSOR = "shiftsync"

.\mvnw.cmd spring-boot:run
```

As variáveis `SPRING_*` sobrescrevem as propriedades correspondentes do Spring
Boot em `src/main/resources/application.properties`. Não compartilhe nem
versione senhas ou segredos reais; o segredo JWT deve ser longo, aleatório e
diferente em cada ambiente. A duração padrão configurada para os tokens é de
120 minutos.

Para executar os testes:

```powershell
.\mvnw.cmd test
```

## Interface e documentação da API

Com a API em execução, abra `Front/html/index.html` usando o Live Server do VS
Code. A interface chama a API em `http://localhost:8080`; o CORS está limitado
às origens `http://127.0.0.1:5500` e `http://localhost:5500`.

O usuário precisa possuir uma conta ativa para entrar. O projeto não inclui
usuário padrão nem carga inicial de dados; a conta deve ser provisionada antes
do primeiro acesso. As rotas da API, exceto login, documentação e preflight
CORS, exigem um token JWT.

- Login: `POST http://localhost:8080/login`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Principais grupos de rotas

- `/usuarios`, `/empresas` e `/gerentes`: contas e cadastros organizacionais.
- `/departamentos`: consulta, cadastro e manutenção de departamentos.
- `/funcionario`: cadastro, consulta, atualização e gerenciamento de funcionários.
- `/turnos`: consulta e manutenção de turnos, jornadas e ocorrências.
- `/ponto`: consulta e registro de marcações e resumos de ponto.
