# Sistema de Gestão de Restaurantes

API REST para o sistema compartilhado de gestão de restaurantes do Tech Challenge — Fase 1.
Implementa o cadastro e a manutenção dos dois perfis de usuário do domínio, dono de
restaurante e cliente, com validação de credenciais, busca por nome e troca de senha em
endpoint dedicado.

Construída em Java 21 e Spring Boot 3.3, com persistência em PostgreSQL versionada por
Flyway e empacotamento em Docker.

**Repositório:** <https://github.com/mikaelapg/restaurant-management>
O repositório contém o código-fonte, este README, a documentação Swagger/OpenAPI (servida
pela aplicação) e a coleção Postman em JSON. O relatório técnico em PDF é entregue
separadamente, como pede o enunciado.

## Stack

| Componente | Versão | Papel |
|---|---|---|
| Java | 21 (LTS) | Linguagem |
| Spring Boot | 3.3.5 | Framework da aplicação |
| Spring Data JPA | 3.3.x | Persistência e repositórios |
| PostgreSQL | 16 | Banco relacional |
| Flyway | 10.x | Versionamento do schema |
| SpringDoc OpenAPI | 2.6.0 | Especificação OpenAPI e Swagger UI |
| spring-security-crypto | 6.x | Hash BCrypt das senhas |
| Lombok | 1.18.x | Redução de boilerplate |
| JUnit 5 + Mockito | 5.x | Testes unitários |
| Testcontainers | 1.20.x | Testes de integração contra PostgreSQL real |
| JaCoCo | 0.8.x | Cobertura de código |
| Docker Compose | v2 | Orquestração de aplicação e banco |

## Decisões de projeto

**Herança JOINED para os perfis de usuário.** `Customer` e `RestaurantOwner` compartilham
nome, e-mail, login, senha e endereço, mas têm atributos próprios (CPF e data de
nascimento; nome do restaurante e CNPJ). A estratégia `JOINED` mantém os campos comuns em
`users` e os específicos em tabelas filhas, preservando as restrições de integridade em
cada coluna — o que `SINGLE_TABLE` não permitiria sem tornar tudo anulável. O custo é um
`JOIN` por consulta ao subtipo, aceitável no volume deste domínio.

**Versionamento da API pela URI.** A versão faz parte do caminho (`/api/v1/...`) e os
controllers ficam em `controller.v1`. É explícito no endereço, testável direto no navegador
e no Postman, e permite que uma futura `v2` conviva com a `v1` sem quebrar consumidores:
bastaria um pacote `controller.v2` reaproveitando a camada de serviço.

**Erros no padrão RFC 7807.** Todas as respostas de erro são `application/problem+json`,
com `type` identificando a classe do problema de forma estável e legível por máquina. Erros
de validação incluem o mapa `fieldErrors`, apontando cada campo recusado.

**Precedência explícita entre os tratadores de exceção.** Com
`spring.mvc.problemdetails.enabled`, o Spring registra o seu próprio
`ProblemDetailsExceptionHandler` com ordem 0. O `GlobalExceptionHandler` da aplicação
declara `@Order(HIGHEST_PRECEDENCE)` para responder antes dele às exceções padrão do MVC.
O tratamento genérico de `Exception` fica isolado em `UnhandledExceptionHandler`, com a
menor precedência: como `@ExceptionHandler(Exception.class)` casa com qualquer exceção, no
mesmo advice ele capturaria tudo antes do Spring e transformaria em 500 respostas que o
tratamento nativo resolve corretamente, como 415 e 406. A ordem resultante é: tratadores
específicos da aplicação, tratadores padrão do Spring MVC, e por último o 500.

**Troca de senha separada da atualização cadastral.** `PUT` não recebe senha; a troca exige
a senha atual e a confirmação da nova, em `PATCH .../password`. São operações com
pré-condições e riscos distintos, e mantê-las juntas obrigaria o cliente a trafegar a senha
em toda edição de perfil.

**Camada de serviço orientada a SOLID.** Os serviços são expostos por interface
(`CustomerService`, `RestaurantOwnerService`, `UserService`, `AuthService`), e os
controllers e testes dependem da abstração, não da implementação (`…ServiceImpl` —
Inversão de Dependência). `BaseUserService` orquestra o que vale para qualquer usuário —
troca de senha e exclusão —; a regra de unicidade de e-mail e login fica no colaborador
`UserUniquenessValidator` (Responsabilidade Única); e um novo perfil de usuário se
resolve por uma nova subclasse, sem tocar no código existente (Aberto/Fechado). Os
serviços concretos tratam apenas o que é específico do subtipo (CPF, CNPJ, nome do
restaurante).

**Senhas com BCrypt, sem a stack de filtros do Spring Security.** O escopo desta fase é a
validação de credenciais, não a proteção das rotas. Usar apenas `spring-security-crypto`
entrega o hash com salt sem introduzir a cadeia de filtros que ainda não é necessária.

## Como executar

### Docker Compose

```bash
cp .env.example .env
docker compose up -d --build
docker compose logs -f app
```

O Compose sobe o PostgreSQL 16 e a aplicação. O container da aplicação só inicia após o
healthcheck do banco passar, e o Flyway cria o schema na primeira subida.

### Local, com o Maven Wrapper

Requer um PostgreSQL acessível em `localhost:5432`. O caminho mais curto é subir apenas o
banco:

```bash
docker compose up -d db

./mvnw spring-boot:run     # Linux e macOS
.\mvnw.cmd spring-boot:run # Windows
```

### Variáveis de ambiente

| Variável | Padrão | Uso |
|---|---|---|
| `POSTGRES_DB` | `restaurant_db` | Nome do banco |
| `POSTGRES_USER` | `restaurant_user` | Usuário do banco |
| `POSTGRES_PASSWORD` | `restaurant_pass` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta exposta pela API |

## Documentação da API

Com a aplicação no ar:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/api-docs |

## Endpoints

### Autenticação

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/auth/login` | Valida login e senha |

### Clientes

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/customers` | Cadastra |
| GET | `/api/v1/customers` | Lista paginada (`page`, `size`, `sort`), com filtro opcional `?name=` |
| GET | `/api/v1/customers/{id}` | Busca por id |
| PUT | `/api/v1/customers/{id}` | Atualiza dados cadastrais |
| PATCH | `/api/v1/customers/{id}/password` | Troca a senha |
| DELETE | `/api/v1/customers/{id}` | Exclui |

### Donos de restaurante

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/restaurant-owners` | Cadastra |
| GET | `/api/v1/restaurant-owners` | Lista paginada (`page`, `size`, `sort`), com filtro opcional `?name=` |
| GET | `/api/v1/restaurant-owners/{id}` | Busca por id |
| PUT | `/api/v1/restaurant-owners/{id}` | Atualiza dados cadastrais |
| PATCH | `/api/v1/restaurant-owners/{id}/password` | Troca a senha |
| DELETE | `/api/v1/restaurant-owners/{id}` | Exclui |

### Usuários

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/v1/users` | Lista paginada de clientes e donos juntos, com filtro `?name=` |
| GET | `/api/v1/users/{id}` | Busca qualquer usuário por id |

A consulta unificada atende à busca por nome sem exigir que o consumidor saiba de antemão
se o usuário é cliente ou dono.

### Paginação

Os três endpoints `GET` de listagem (`/customers`, `/restaurant-owners`, `/users`) são
paginados com o suporte padrão do Spring Data. Parâmetros de consulta:

| Parâmetro | Padrão | Observação |
|---|---|---|
| `page` | `0` | Índice da página, base zero |
| `size` | `20` | Itens por página; máximo `100` |
| `sort` | — | Campo e direção, ex.: `sort=name,asc` (repetível) |

A resposta é um envelope: `content` (a página de itens) mais os metadados `page`, `size`,
`totalElements`, `totalPages` e `last`. O filtro `?name=` é combinável com a paginação.
A busca por `id` não é paginada.

## Regras de validação e unicidade

| Campo | Regra |
|---|---|
| `name` | Obrigatório, de 2 a 255 caracteres |
| `email` | Obrigatório, formato válido, único entre todos os usuários, normalizado para minúsculas |
| `login` | Obrigatório, de 3 a 100 caracteres, único independente de maiúsculas e minúsculas |
| `password` | Obrigatório, mínimo de 8 caracteres com minúscula, maiúscula, dígito e caractere especial (sem espaços); regra em `@StrongPassword`; armazenado como hash BCrypt (custo 12) |
| `cpf` | Opcional; quando informado, formato `000.000.000-00` e único |
| `cnpj` | Opcional; quando informado, formato `00.000.000/0000-00` e único |
| `dateOfBirth` | Opcional; quando informada, deve estar no passado |

**E-mail e login são identificadores únicos e independentes.** O enunciado exige unicidade
apenas do e-mail; o login foi mantido também único, de forma independente, porque é a
credencial consumida pelo serviço de validação de login e precisa identificar um único
usuário sem ambiguidade. O e-mail é o canal de contato/recuperação e pode mudar sem afetar
o acesso; o login é o identificador estável de autenticação. Ambos são únicos no conjunto
de todos os usuários, não por subtipo, e comparados sem diferenciar a caixa.

**CPF e CNPJ são opcionais** porque não estão na lista de campos obrigatórios do enunciado
(nome, e-mail, login, senha, data da última alteração e endereço) — são enriquecimento dos
subtipos. Quando informados, seguem o formato esperado e têm unicidade garantida no banco
e verificada no serviço (resposta `409` apontando o campo).

Na atualização, o endereço ausente no corpo significa manter o atual, não apagá-lo.

## Modelo de erros

```json
{
  "type": "https://restaurant-api.com/errors/login-conflict",
  "title": "Login Já Cadastrado",
  "status": 409,
  "detail": "Já existe um usuário cadastrado com o login: maria.silva",
  "instance": "/api/v1/customers",
  "timestamp": "2026-08-24T21:00:00Z"
}
```

| Status | `type` | Situação |
|---|---|---|
| 400 | `validation-error` | Bean Validation recusou campos; inclui `fieldErrors` |
| 400 | `malformed-json` | Corpo ausente ou JSON malformado |
| 400 | `invalid-parameter` | Parâmetro de caminho ou query com tipo incompatível |
| 401 | `invalid-credentials` | Login inexistente ou senha incorreta |
| 404 | `user-not-found` | Nenhum usuário com o id informado |
| 404 | `resource-not-found` | Rota inexistente |
| 405 | `method-not-allowed` | Método HTTP não suportado pelo endpoint |
| 409 | `email-conflict` | E-mail já cadastrado |
| 409 | `login-conflict` | Login já cadastrado |
| 409 | `cpf-conflict` | CPF já cadastrado |
| 409 | `cnpj-conflict` | CNPJ já cadastrado |
| 409 | `data-integrity-conflict` | Violação de restrição do banco não coberta acima |
| 422 | `invalid-password` | Senha atual incorreta, ou confirmação divergente |
| 500 | `internal-error` | Falha não prevista |

A distinção entre 401 e 422 é intencional: 401 responde a quem não provou identidade no
login; 422 responde a um pedido bem formado cuja pré-condição de negócio falhou, como
trocar a senha informando a senha atual errada.

## Banco de dados

O schema é criado pelo Flyway em `src/main/resources/db/migration/V1__create_tables.sql`.

| Tabela | Colunas |
|---|---|
| `users` | `id`, `user_type`, `name`, `email` (único), `login` (único), `password`, `last_modified_at`, e o endereço embutido: `street`, `number`, `complement`, `city`, `state`, `zip_code` |
| `customers` | `id` (FK para `users`), `cpf` (único), `date_of_birth` |
| `restaurant_owners` | `id` (FK para `users`), `restaurant_name`, `cnpj` (único) |

As tabelas filhas referenciam `users` por chave estrangeira com `ON DELETE CASCADE`. O
índice `idx_users_name` é criado sobre `LOWER(name)` para atender à busca por nome sem
diferenciar maiúsculas de minúsculas.

`last_modified_at` é mantido pelo Hibernate via `@UpdateTimestamp`.

## Testes

```bash
./mvnw test      # 70 testes unitários (JUnit 5 + Mockito)
./mvnw verify    # + testes de integração com Testcontainers (exige Docker)
```

**70 testes unitários** cobrindo:

- Serviços, com Mockito: `AuthServiceTest`, `CustomerServiceTest`,
  `RestaurantOwnerServiceTest`, `UserServiceTest` — regras de negócio, unicidade delegada a
  `UserUniquenessValidator` e paginação
- Camada web, com `@WebMvcTest`: `AuthControllerTest`, `CustomerControllerTest`,
  `RestaurantOwnerControllerTest`, `UserControllerTest` — rotas, status, envelope de página
  e corpo de erro RFC 7807
- Tratamento de erros: `GlobalExceptionHandlerTest`, `UnhandledExceptionHandlerTest`

**Teste de integração** (`SchemaConstraintsIT`, sufixo `IT` → roda em `mvn verify` pelo
maven-failsafe-plugin): sobe um PostgreSQL real com Testcontainers e valida que a migration
`V1` do Flyway cria o schema e que as constraints de unicidade (`email`, `login`, `cpf`,
`cnpj`) são impostas pelo banco. Sem Docker disponível, a classe é ignorada sem quebrar o
build.

**Cobertura** pelo JaCoCo (relatório em `target/site/jacoco/index.html` após `mvn test`):
~83% das instruções e ~81% das linhas na suíte unitária.

## Coleção Postman

```bash
newman run postman/restaurant-management.postman_collection.json
```

As pastas estão numeradas na ordem de execução, porque cada uma depende do que a anterior
cadastrou. Os ids são capturados dos próprios cadastros, e cada execução gera e-mails,
logins, CPF e CNPJ distintos — a coleção pode rodar várias vezes no mesmo banco sem
esbarrar nas restrições de unicidade.

Cobre cadastro válido e recusado, validação de login, busca por nome nos três endpoints,
atualização, troca de senha e exclusão, com os respectivos cenários de erro.

## Estrutura

```
src/main/java/com/restaurant/management
├── config          Configuração do OpenAPI e exemplos de payload
├── controller/v1   Controllers REST da versão 1
├── domain
│   ├── enums       UserType
│   ├── model       Entidades JPA e o embeddable Address
│   └── repository  Repositórios Spring Data
├── dto
│   ├── request            Payloads de entrada com Bean Validation
│   │   └── validation     @StrongPassword e seu validador
│   └── response           Payloads de saída (sem senha) e PageResponse
├── exception       Exceções de negócio e tratadores RFC 7807
└── service         Interfaces de serviço, implementações (…ServiceImpl),
                    BaseUserService e UserUniquenessValidator
```
