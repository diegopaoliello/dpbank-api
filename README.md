# DP Bank API

API REST para processamento de débitos, créditos e consulta de saldos de contas correntes. Este repositório documenta os requisitos funcionais, padrões de arquitetura e práticas de observabilidade aplicadas ao DP Bank.

## Sumário
- [Contexto e funcionalidades](#contexto-e-funcionalidades)
- [Stack técnica](#stack-técnica)
- [Pré-requisitos](#pré-requisitos)
- [Guia rápido](#guia-rápido)
- [Migrations e dados iniciais](#migrations-e-dados-iniciais)
- [Documentação da API](#documentação-da-api)
- [Contratos da API](#contratos-da-api)
- [Internacionalização](#internacionalização)
- [Ferramentas auxiliares](#ferramentas-auxiliares)
- [Testes e qualidade](#testes-e-qualidade)

## Contexto e funcionalidades
- Processamento de lançamentos em lote (débito/crédito) sobre uma conta correntista única, com bloqueio pessimista para evitar condições de corrida.
- Consulta do saldo consolidado após cada batch de transações.
- Comunicação com mensagens padronizadas e traduzidas via `Accept-Language` (`pt`, `en`, `es`).
- Observabilidade exposta por Actuator e documentação compartilhada via coleção Postman.

## Stack técnica
| Categoria | Ferramentas |
| --- | --- |
| Linguagem / Runtime | **Java 21**, Maven 3.9+
| Framework principal | **Spring Boot 3.5.9** (Web, Validation, Data JPA, Actuator)
| Persistência | **JPA/Hibernate** + **Liquibase** (versionamento do schema) + **H2** em memória para execução local; **Testcontainers + PostgreSQL** para integrações
| Documentação | **Coleção Postman** (`docs/dpbank-postman-collection.json`) importável em qualquer ambiente
| Internacionalização | **Spring i18n** com bundles `messages.properties` (pt, en, es)
| Testes | **JUnit 5**, Spring Boot Test, **Testcontainers** para bancos descartáveis
| Observabilidade | **Spring Boot Actuator**, logs estruturados via SLF4J/Logback

## Pré-requisitos
1. **Java 21** instalado (`java -version`).
2. **Maven 3.9+** (pode usar o wrapper `./mvnw`).
3. **Docker** em execução para suportar Testcontainers durante `mvn test`/`mvn verify`.

## Guia rápido
### Build
```bash
./mvnw clean install
```
Gera `target/dpbank-api-0.0.1-SNAPSHOT.jar` e executa todo o pipeline de testes (incluindo integrações com Testcontainers).
- Precisa apenas do artefato? Rode `./mvnw clean install -DskipTests`, lembrando que isso ignora a validação com Testcontainers e deve ser usado apenas quando Docker não estiver disponível.

### Executar o JAR
```bash
java -jar target/dpbank-api-0.0.1-SNAPSHOT.jar
```
- Porta padrão: `8080`
- Perfil default utiliza **H2 em memória**; todo dado é recriado em cada inicialização pelo Liquibase.

### Health check
```bash
curl -s http://localhost:8080/actuator/health | jq
```

## Migrations e dados iniciais
- Liquibase aplica os scripts em `src/main/resources/db/changelog`.
- `001-initial-schema.yaml`: cria as tabelas `tb_account` e `tb_transaction` com constraints.
- `002-seed-default-account.yaml`: registra a conta base do ambiente local.

| Propriedade | Valor |
| --- | --- |
| `account_id` | `2d2f3d02-3ec6-4e5b-8d2a-5a497c2a5db7` |
| `account_number` | `1234567890` |
| Saldo inicial | `1000.00` |

## Documentação da API
### Springdoc / OpenAPI
- UI interativa (Swagger): `http://localhost:8080/docs`
- Contrato JSON/YAML: `http://localhost:8080/docs/api`
- Disponível enquanto a API estiver em execução

### Coleção Postman
- Arquivo versionado: [`docs/dpbank-postman-collection.json`](docs/dpbank-postman-collection.json) já inclui variáveis `baseUrl` e `accountId`.

## Contratos da API
### Endpoints principais
| Método | Caminho | Descrição |
| --- | --- | --- |
| `POST` | `/accounts/{id}/transactions` | Processa uma lista de lançamentos (débito/crédito). Retorna saldo atualizado.
| `GET` | `/accounts/{id}/balance` | Consulta o saldo consolidado da conta.

### Payload de exemplo
```json
[
  {
    "amount": 150.0,
    "type": "DEBIT",
    "description": "Pagamento boleto"
  },
  {
    "amount": 250.0,
    "type": "CREDIT",
    "description": "Estorno"
  }
]
```
- Campo `type` aceita `DEBIT` ou `CREDIT` (case-sensitive).
- Regras de validação:
  - Lista não pode ser vazia (`transactions.list.required`).
  - Débitos são recusados se o saldo não for suficiente (`422 Unprocessable Entity`).
  - Erros de payload retornam `400 Bad Request` com `ProblemDetail` padronizado.

## Internacionalização
- Definições em `src/main/resources/i18n/messages*.properties`.
- Idioma é resolvido pelo header `Accept-Language`. Idiomas suportados: `pt` (default), `en`, `es`.
- Exemplo:
```bash
curl -s -H "Accept-Language: es" \
  http://localhost:8080/accounts/00000000-0000-0000-0000-000000000000/balance
```
Resposta (`404`): `{"title":"Cuenta no encontrada", ...}`.

## Ferramentas auxiliares
### H2 Console (somente local)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:dpbankdb`
- Driver: `org.h2.Driver`
- Usuário: `sa`
- Senha: *(em branco)*

### Actuator
- Endpoints expostos: `GET /actuator`, `GET /actuator/health`, `GET /actuator/info` (outros seguem configuração default do Spring Boot 3).
- Útil para automações de monitoração ou smoke tests em pipelines.

## Testes e qualidade
| Comando | Descrição |
| --- | --- |
| `./mvnw test` | Executa testes unitários + integrações leves.
| `./mvnw verify` | Executa toda a suíte com Testcontainers e validação das migrations.
| `./mvnw spring-boot:run` | Alternativa durante o desenvolvimento (hot reload via DevTools).

- ⚠️ **Docker obrigatório para testes**: sem o engine ativo, o Testcontainers não consegue subir o PostgreSQL descartável e `./mvnw clean install`/`verify` falharão. Se realmente precisar rodar sem Docker, utilize `-DskipTests` (ou configure um profile alternativo), ciente de que os testes de integração ficarão pendentes.