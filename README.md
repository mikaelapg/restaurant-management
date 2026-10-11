# Sistema de Gestão de Restaurantes

API REST para o sistema compartilhado de gestão de restaurantes do Tech Challenge.

- **Fase 1:** cadastro e manutenção dos dois perfis de usuário, dono de restaurante e
  cliente, com validação de credenciais, busca por nome e troca de senha em endpoint dedicado.
- **Fase 2:** cadastro de tipos de usuário e associação dos usuários a um tipo, cadastro de
  restaurantes e cadastro dos itens do cardápio, organizados em Clean Architecture.

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
| JaCoCo | 0.8.x | Cobertura de código, com mínimo de 80% |
| Docker Compose | v2 | Orquestração de aplicação e banco |

## Arquitetura

As funcionalidades da Fase 2 seguem a Clean Architecture, em três camadas. A regra é que
as dependências apontam só para dentro: a infraestrutura conhece a aplicação, que conhece
o domínio, e nunca o contrário.

```
            ┌───────────────────────────────────────────────────────┐
            │ infrastructure  (Spring, JPA, HTTP)                   │
            │   web/          controllers e DTOs                    │
            │   persistence/  entidades JPA, repositórios, gateways │
            │   config/       registra os casos de uso como beans   │
            │   ┌───────────────────────────────────────────────┐   │
            │   │ core/application  (Java puro)                 │   │
            │   │   casos de uso, interfaces de gateway,        │   │
            │   │   comandos, exceções, paginação               │   │
            │   │   ┌───────────────────────────────────────┐   │   │
            │   │   │ core/domain  (Java puro)              │   │   │
            │   │   │   UserType, Restaurant, MenuItem,     │   │   │
            │   │   │   Address, OpeningHours               │   │   │
            │   │   └───────────────────────────────────────┘   │   │
            │   └───────────────────────────────────────────────┘   │
            └───────────────────────────────────────────────────────┘
```

| Camada | Pacote | O que tem | Depende de |
|---|---|---|---|
| Domínio | `core.domain` | Entidades imutáveis que validam as próprias regras (nome obrigatório, preço positivo, horários diferentes) e lançam `DomainValidationException` | nada |
| Aplicação | `core.application` | Um caso de uso por operação (`CreateRestaurantUseCase`, `AssignUserTypeUseCase`…) e as interfaces de gateway que eles usam (`RestaurantGateway`, `UserGateway`…) | domínio |
| Infraestrutura | `infrastructure` | Controllers REST, entidades JPA separadas das de domínio, gateways que implementam as interfaces da aplicação e classes `@Configuration` que montam os casos de uso | aplicação e domínio |

Os casos de uso e as entidades de domínio não têm nenhuma anotação do Spring nem do JPA. Por
isso são testados com JUnit e Mockito puros, sem subir contexto, e trocar o banco mudaria só
os gateways.

**O caminho de uma requisição**, cadastrando um restaurante:

1. `RestaurantController` recebe o `RestaurantRequest`, que o Bean Validation confere
   (campos ausentes viram 400 com `fieldErrors`).
2. O request vira um `RestaurantCommand`; endereço e horário viram os objetos de valor
   `Address` e `OpeningHours`, que validam as regras de domínio.
3. `CreateRestaurantUseCase` cria o `Restaurant`, pergunta ao `UserGateway` se o dono
   existe e manda o `RestaurantGateway` salvar.
4. `RestaurantJpaGateway` converte o domínio em `RestaurantJpaEntity`, salva pelo Spring
   Data e converte o resultado de volta.
5. O controller devolve `201 Created` com o header `Location` e o `RestaurantResponse`.

**Convivência com a Fase 1.** O cadastro de usuários da Fase 1 continua na estrutura em
camadas tradicional (`controller`, `service`, `domain`, `dto`). Os casos de uso novos não
dependem dele: quando precisam saber de usuários, usam a interface `UserGateway`, e o
`UserJpaGateway` faz a ponte com o repositório existente. A migração dos usuários para a
estrutura nova pode ser feita depois sem mexer nos casos de uso.

## Decisões de projeto

### Fase 2

**Tipo de usuário como cadastro, associado por chave estrangeira.** Com banco relacional, a
associação é uma coluna `users.user_type_id` com FK para `user_types`. Cada usuário tem um
tipo; trocar de tipo é um `PUT /api/v1/users/{id}/user-type`. A migration `V3` cadastra
"Dono de Restaurante" e "Cliente", associa os usuários que já existiam conforme o perfil, e
os cadastros novos nascem com o tipo correspondente. Se esses tipos forem renomeados ou
excluídos, o usuário nasce sem tipo e pode ser associado depois, em vez de o cadastro falhar.

**Tipo de usuário e perfil são coisas diferentes.** O campo `userType`
(`CUSTOMER`/`RESTAURANT_OWNER`) da Fase 1 continua sendo o discriminador da herança: diz em
qual tabela filha estão o CPF ou o CNPJ, e não muda. O `assignedUserType` é o tipo do
cadastro da Fase 2, gerenciado pelo CRUD e trocável.

**Integridade garantida pelo banco e explicada pela aplicação.** O banco recusa excluir um
tipo em uso, um usuário que é dono de restaurante (`ON DELETE RESTRICT`), um preço não
positivo ou horários iguais (`CHECK`). A aplicação confere as mesmas regras antes, para
responder 409 ou 400 com uma mensagem que diz o que fazer, e não o erro genérico de
integridade.

**O dono do restaurante é qualquer usuário existente.** O enunciado pede que o dono seja
"associado a um usuário existente"; o restaurante guarda o `ownerId` e o caso de uso confere
que ele existe (404 se não existir).

**Horário de funcionamento que atravessa a meia-noite.** `OpeningHours` guarda abertura e
fechamento (`HH:mm`). Fechamento antes da abertura é válido e significa que o restaurante
vira a noite (18:00 às 02:00). Abertura igual ao fechamento é recusada, porque não dá para
saber se o restaurante nunca abre ou nunca fecha.

**Itens do cardápio aninhados ao restaurante.** As rotas são
`/restaurants/{restaurantId}/menu-items`: o item não existe fora de um restaurante, e um item
de outro restaurante responde 404 pelo caminho errado. O nome é único dentro do restaurante,
sem diferenciar maiúsculas, e restaurantes diferentes podem ter pratos de mesmo nome.
Excluir o restaurante exclui o cardápio dele (`ON DELETE CASCADE`).

**Preço em `BigDecimal`.** Valores monetários não usam ponto flutuante. O preço é positivo,
tem no máximo duas casas decimais e é guardado em `NUMERIC(10, 2)`.

**Foto como caminho.** Como o enunciado permite, o item guarda só o caminho de onde a foto
estaria (`photoPath`), sem upload de arquivo.

**Paginação sem depender do Spring no núcleo.** Casos de uso e gateways trabalham com
`PageQuery` e `PageResult`, da camada de aplicação; a conversão para `Pageable` e `Page`
acontece só no gateway JPA.

### Fase 1

**Herança JOINED para os perfis de usuário.** `Customer` e `RestaurantOwner` compartilham
nome, e-mail, login, senha e endereço, mas têm atributos próprios (CPF e data de
nascimento; nome do restaurante e CNPJ). A estratégia `JOINED` mantém os campos comuns em
`users` e os específicos em tabelas filhas, preservando as restrições de integridade em
cada coluna — o que `SINGLE_TABLE` não permitiria sem tornar tudo anulável. O custo é um
`JOIN` por consulta ao subtipo, aceitável no volume deste domínio.

**Versionamento da API pela URI.** A versão faz parte do caminho (`/api/v1/...`). É
explícito no endereço, testável direto no navegador e no Postman, e permite que uma futura
`v2` conviva com a `v1` sem quebrar consumidores.

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
resolve por uma nova subclasse, sem tocar no código existente (Aberto/Fechado).

**Senhas com BCrypt, sem a stack de filtros do Spring Security.** O escopo é a validação de
credenciais, não a proteção das rotas. Usar apenas `spring-security-crypto` entrega o hash
com salt sem introduzir a cadeia de filtros que ainda não é necessária.

## Como executar

### Docker Compose

```bash
cp .env.example .env
docker compose up -d --build
docker compose logs -f app
```

O Compose sobe o PostgreSQL 16 e a aplicação. O container da aplicação só inicia após o
healthcheck do banco passar, e o Flyway cria o schema na primeira subida e aplica as
migrations novas nas seguintes.

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

### Tipos de usuário (Fase 2)

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/user-types` | Cadastra; nome único sem diferenciar maiúsculas |
| GET | `/api/v1/user-types` | Lista todos, em ordem alfabética |
| GET | `/api/v1/user-types/{id}` | Busca por id |
| PUT | `/api/v1/user-types/{id}` | Renomeia |
| DELETE | `/api/v1/user-types/{id}` | Exclui; recusado (409) se algum usuário tiver o tipo |
| PUT | `/api/v1/users/{id}/user-type` | Associa o usuário a um tipo: `{"userTypeId": 1}` |

### Restaurantes (Fase 2)

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/restaurants` | Cadastra |
| GET | `/api/v1/restaurants` | Lista paginada, com filtros opcionais `?name=` e `?cuisineType=` |
| GET | `/api/v1/restaurants/{id}` | Busca por id |
| PUT | `/api/v1/restaurants/{id}` | Atualiza todos os dados, inclusive o dono |
| DELETE | `/api/v1/restaurants/{id}` | Exclui, junto com o cardápio |

```json
{
  "name": "Cantina da Nonna",
  "address": {
    "street": "Rua Augusta", "number": "1500", "complement": "Loja 2",
    "city": "São Paulo", "state": "SP", "zipCode": "01305-100"
  },
  "cuisineType": "Italiana",
  "openingHours": { "opensAt": "18:00", "closesAt": "02:00" },
  "ownerId": 1
}
```

### Cardápio (Fase 2)

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/restaurants/{restaurantId}/menu-items` | Cadastra um item |
| GET | `/api/v1/restaurants/{restaurantId}/menu-items` | Lista paginada do cardápio |
| GET | `/api/v1/restaurants/{restaurantId}/menu-items/{itemId}` | Busca um item |
| PUT | `/api/v1/restaurants/{restaurantId}/menu-items/{itemId}` | Atualiza um item |
| DELETE | `/api/v1/restaurants/{restaurantId}/menu-items/{itemId}` | Exclui um item |

```json
{
  "name": "Risoto de cogumelos",
  "description": "Arroz arbóreo, funghi secchi e parmesão",
  "price": 68.90,
  "dineInOnly": false,
  "photoPath": "/images/restaurants/1/risoto.jpg"
}
```

### Autenticação

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/auth/login` | Valida login e senha |

### Clientes

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/customers` | Cadastra; nasce com o tipo "Cliente" |
| GET | `/api/v1/customers` | Lista paginada (`page`, `size`, `sort`), com filtro opcional `?name=` |
| GET | `/api/v1/customers/{id}` | Busca por id |
| PUT | `/api/v1/customers/{id}` | Atualiza dados cadastrais |
| PATCH | `/api/v1/customers/{id}/password` | Troca a senha |
| DELETE | `/api/v1/customers/{id}` | Exclui; recusado (409) se for dono de restaurante |

### Donos de restaurante

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/restaurant-owners` | Cadastra; nasce com o tipo "Dono de Restaurante" |
| GET | `/api/v1/restaurant-owners` | Lista paginada (`page`, `size`, `sort`), com filtro opcional `?name=` |
| GET | `/api/v1/restaurant-owners/{id}` | Busca por id |
| PUT | `/api/v1/restaurant-owners/{id}` | Atualiza dados cadastrais |
| PATCH | `/api/v1/restaurant-owners/{id}/password` | Troca a senha |
| DELETE | `/api/v1/restaurant-owners/{id}` | Exclui; recusado (409) se ainda tiver restaurantes |

### Usuários

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/v1/users` | Lista paginada de clientes e donos juntos, com filtro `?name=` |
| GET | `/api/v1/users/{id}` | Busca qualquer usuário por id |

A consulta unificada atende à busca por nome sem exigir que o consumidor saiba de antemão
se o usuário é cliente ou dono. As respostas de usuário trazem `assignedUserType`
(`{"id": 2, "name": "Cliente"}`), o tipo associado.

### Paginação

As listagens de usuários (`/customers`, `/restaurant-owners`, `/users`) usam o suporte padrão
do Spring Data; as da Fase 2 (`/restaurants`, `.../menu-items`) recebem só página e tamanho e
devolvem em ordem alfabética de nome.

| Parâmetro | Padrão | Observação |
|---|---|---|
| `page` | `0` | Índice da página, base zero |
| `size` | `20` | Itens por página; máximo `100` |
| `sort` | — | Só nas listagens de usuários. Campo e direção, ex.: `sort=name,asc` |

A resposta é um envelope: `content` (a página de itens) mais os metadados `page`, `size`,
`totalElements`, `totalPages` e `last`. Os filtros são combináveis com a paginação. A busca
por `id` não é paginada. A lista de tipos de usuário não é paginada: é uma tabela de
referência com poucas linhas.

## Regras de validação e unicidade

### Usuários

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
usuário sem ambiguidade. Ambos são únicos no conjunto de todos os usuários, não por
subtipo, e comparados sem diferenciar a caixa.

**CPF e CNPJ são opcionais** porque não estão na lista de campos obrigatórios do enunciado
— são enriquecimento dos subtipos. Quando informados, seguem o formato esperado e têm
unicidade garantida no banco e verificada no serviço (resposta `409` apontando o campo).

Na atualização, o endereço ausente no corpo significa manter o atual, não apagá-lo.

### Tipos de usuário, restaurantes e cardápio

| Recurso | Campo | Regra |
|---|---|---|
| Tipo de usuário | `name` | Obrigatório, até 50 caracteres, único sem diferenciar maiúsculas |
| Restaurante | `name` | Obrigatório, até 100 caracteres |
| Restaurante | `address` | Obrigatório; `street`, `number`, `city`, `state` e `zipCode` obrigatórios, `complement` opcional |
| Restaurante | `cuisineType` | Obrigatório, até 50 caracteres |
| Restaurante | `openingHours` | `opensAt` e `closesAt` obrigatórios (`HH:mm`) e diferentes; fechamento antes da abertura significa que vira a noite |
| Restaurante | `ownerId` | Obrigatório; precisa ser um usuário existente |
| Item do cardápio | `name` | Obrigatório, até 100 caracteres, único no restaurante sem diferenciar maiúsculas |
| Item do cardápio | `description` | Obrigatória, até 500 caracteres |
| Item do cardápio | `price` | Obrigatório, maior que zero, até duas casas decimais |
| Item do cardápio | `dineInOnly` | Obrigatório; `true` quando o item só pode ser pedido para consumo no restaurante |
| Item do cardápio | `photoPath` | Opcional, até 500 caracteres |

As regras valem duas vezes: no DTO, para a API responder 400 com `fieldErrors` por campo, e
na entidade de domínio, que se protege sozinha mesmo quando usada fora da camada web.

## Modelo de erros

```json
{
  "type": "https://restaurant-api.com/errors/user-type-in-use",
  "title": "Tipo de Usuário em Uso",
  "status": 409,
  "detail": "O tipo de usuário 3 está associado a usuários e não pode ser excluído.",
  "instance": "/api/v1/user-types/3",
  "timestamp": "2026-10-11T21:00:00Z"
}
```

| Status | `type` | Situação |
|---|---|---|
| 400 | `validation-error` | Bean Validation recusou campos; inclui `fieldErrors` |
| 400 | `domain-validation` | Uma regra de entidade de domínio foi violada, como horários de abertura e fechamento iguais |
| 400 | `malformed-json` | Corpo ausente, JSON malformado ou valor em formato inválido (ex.: horário `25:00`) |
| 400 | `invalid-parameter` | Parâmetro de caminho ou query com tipo incompatível |
| 401 | `invalid-credentials` | Login inexistente ou senha incorreta |
| 404 | `user-not-found` | Nenhum usuário com o id informado, inclusive o dono de um restaurante |
| 404 | `user-type-not-found` | Nenhum tipo de usuário com o id informado |
| 404 | `restaurant-not-found` | Nenhum restaurante com o id informado |
| 404 | `menu-item-not-found` | O item não existe no cardápio do restaurante informado |
| 404 | `resource-not-found` | Rota inexistente |
| 405 | `method-not-allowed` | Método HTTP não suportado pelo endpoint |
| 409 | `email-conflict` | E-mail já cadastrado |
| 409 | `login-conflict` | Login já cadastrado |
| 409 | `cpf-conflict` | CPF já cadastrado |
| 409 | `cnpj-conflict` | CNPJ já cadastrado |
| 409 | `user-type-conflict` | Já existe um tipo de usuário com o nome |
| 409 | `user-type-in-use` | Exclusão de tipo de usuário associado a usuários |
| 409 | `user-owns-restaurants` | Exclusão de usuário que é dono de restaurantes |
| 409 | `menu-item-conflict` | O restaurante já tem um item com o nome |
| 409 | `data-integrity-conflict` | Violação de restrição do banco não coberta acima |
| 422 | `invalid-password` | Senha atual incorreta, ou confirmação divergente |
| 500 | `internal-error` | Falha não prevista |

A distinção entre 401 e 422 é intencional: 401 responde a quem não provou identidade no
login; 422 responde a um pedido bem formado cuja pré-condição de negócio falhou, como
trocar a senha informando a senha atual errada.

## Banco de dados

O schema é criado e evoluído pelo Flyway, em `src/main/resources/db/migration`:

| Migration | O que faz |
|---|---|
| `V1__create_tables.sql` | Usuários: `users`, `customers`, `restaurant_owners` |
| `V2__create_user_types.sql` | Tabela `user_types`, com índice único sobre `LOWER(name)` |
| `V3__associate_users_with_user_types.sql` | Coluna `users.user_type_id`, os tipos "Dono de Restaurante" e "Cliente" e a associação dos usuários existentes |
| `V4__create_restaurants.sql` | Tabela `restaurants` |
| `V5__create_menu_items.sql` | Tabela `menu_items` |

| Tabela | Colunas |
|---|---|
| `users` | `id`, `user_type` (discriminador), `user_type_id` (FK para `user_types`), `name`, `email` (único), `login` (único), `password`, `last_modified_at`, e o endereço embutido: `street`, `number`, `complement`, `city`, `state`, `zip_code` |
| `customers` | `id` (FK para `users`), `cpf` (único), `date_of_birth` |
| `restaurant_owners` | `id` (FK para `users`), `restaurant_name`, `cnpj` (único) |
| `user_types` | `id`, `name` (único sem diferenciar maiúsculas) |
| `restaurants` | `id`, `name`, `cuisine_type`, `opens_at`, `closes_at`, `owner_id` (FK para `users`), e o endereço embutido |
| `menu_items` | `id`, `restaurant_id` (FK para `restaurants`), `name` (único por restaurante sem diferenciar maiúsculas), `description`, `price` (`NUMERIC(10, 2)`), `dine_in_only`, `photo_path` |

| Chave estrangeira | Ao excluir o referenciado |
|---|---|
| `customers.id`, `restaurant_owners.id` → `users` | `CASCADE`: os dados do subtipo saem junto |
| `users.user_type_id` → `user_types` | `RESTRICT`: tipo em uso não pode ser excluído |
| `restaurants.owner_id` → `users` | `RESTRICT`: dono de restaurante não pode ser excluído |
| `menu_items.restaurant_id` → `restaurants` | `CASCADE`: o cardápio sai junto com o restaurante |

Restrições `CHECK` impõem no banco que `opens_at <> closes_at` e que `price > 0`. Os índices
sobre `LOWER(name)` atendem às buscas por nome sem diferenciar maiúsculas.
`last_modified_at` é mantido pelo Hibernate via `@UpdateTimestamp`.

O modelo de domínio está em [`docs/domain-model.mmd`](docs/domain-model.mmd) (Mermaid) e
[`docs/domain-model.puml`](docs/domain-model.puml) (PlantUML).

## Testes

```bash
./mvnw test      # 207 testes unitários (JUnit 5 + Mockito) e relatório do JaCoCo
./mvnw verify    # + 20 testes de integração com Testcontainers (exige Docker) e checagem de cobertura
```

**Testes unitários**, sem banco e sem subir a aplicação:

- Domínio: `UserTypeTest`, `RestaurantTest`, `OpeningHoursTest`, `AddressTest`,
  `MenuItemTest` — as regras de cada entidade e objeto de valor
- Casos de uso, com os gateways mockados: `UserTypeUseCasesTest`,
  `RestaurantUseCasesTest`, `MenuItemUseCasesTest`, `PaginationTest`
- Gateways JPA, com os repositórios mockados: `JpaGatewaysTest` — conversão entre domínio
  e JPA, ordenação e paginação
- Camada web, com `@WebMvcTest`: `UserTypeControllerTest`,
  `UserTypeAssignmentControllerTest`, `RestaurantControllerTest`, `MenuItemControllerTest`,
  e os da Fase 1 (`AuthControllerTest`, `CustomerControllerTest`,
  `RestaurantOwnerControllerTest`, `UserControllerTest`) — rotas, status, JSON e corpo de
  erro RFC 7807
- Serviços da Fase 1: `AuthServiceTest`, `CustomerServiceTest`,
  `RestaurantOwnerServiceTest`, `UserServiceTest`
- Tratamento de erros: `GlobalExceptionHandlerTest`, `UnhandledExceptionHandlerTest`

**Testes de integração** (sufixo `IT`, rodam em `mvn verify` pelo maven-failsafe-plugin):
sobem um PostgreSQL 16 real com Testcontainers e exercitam a aplicação de ponta a ponta,
do HTTP ao banco.

| Classe | O que prova |
|---|---|
| `SchemaConstraintsIT` | Todas as migrations aplicam e as constraints de unicidade dos usuários valem no banco |
| `UserTypeApiIT` | CRUD de tipos, unicidade sem diferenciar maiúsculas, associação e tipo em uso |
| `RestaurantApiIT` | CRUD de restaurantes, filtros e paginação, dono inexistente, dono que não pode ser excluído e `CHECK` de horário |
| `MenuItemApiIT` | CRUD do cardápio, unicidade por restaurante, isolamento entre restaurantes, cascata e constraints de preço |

Sem Docker disponível, as classes de integração são ignoradas sem quebrar o build.

**Cobertura** pelo JaCoCo (relatório em `target/site/jacoco/index.html` após `mvn test`):
~89% das linhas e ~90% das instruções na suíte unitária. O `mvn verify` falha se a
cobertura de linhas ficar abaixo de 80%.

## Coleção Postman

```bash
newman run postman/restaurant-management.postman_collection.json
```

As pastas estão numeradas na ordem de execução, porque cada uma depende do que a anterior
cadastrou:

1. Cadastro de usuários
2. Validação de login
3. Busca de usuários
4. Atualização de dados
5. Troca de senha
6. Tipos de usuário: CRUD, associação e tipo em uso
7. Restaurantes: CRUD, filtros e dono que não pode ser excluído
8. Cardápio: CRUD, nome repetido, preço inválido e exclusão em cascata
9. Exclusão de usuários

Os ids são capturados dos próprios cadastros, e cada execução gera e-mails, logins, CPF,
CNPJ e nomes distintos — a coleção pode rodar várias vezes no mesmo banco sem esbarrar nas
restrições de unicidade. São 67 requisições e 147 asserções, cobrindo os cenários de
sucesso e de erro.

## Estrutura

```
src/main/java/com/restaurant/management
├── core                         Fase 2 — Java puro, sem Spring nem JPA
│   ├── domain
│   │   ├── exception            DomainValidationException
│   │   ├── shared               Address (objeto de valor)
│   │   ├── usertype             UserType
│   │   ├── restaurant           Restaurant, OpeningHours
│   │   └── menuitem             MenuItem
│   └── application
│       ├── exception            Exceções dos casos de uso (não encontrado, conflito)
│       ├── pagination           PageQuery, PageResult
│       ├── user                 UserGateway
│       ├── usertype             Casos de uso e UserTypeGateway
│       ├── restaurant           Casos de uso, RestaurantGateway e RestaurantCommand
│       └── menuitem             Casos de uso, MenuItemGateway e MenuItemCommand
├── infrastructure               Fase 2 — adaptadores
│   ├── config                   Registra os casos de uso como beans
│   ├── persistence              Entidades JPA, repositórios Spring Data e gateways
│   └── web                      Controllers REST e DTOs
├── config                       Configuração do OpenAPI e exemplos de payload
├── controller/v1                Fase 1 — controllers REST de usuários e login
├── domain                       Fase 1 — entidades JPA de usuário e repositórios
├── dto                          Fase 1 — payloads de entrada e saída, PageResponse
├── exception                    Exceções da Fase 1 e tratadores RFC 7807
└── service                      Fase 1 — serviços de usuário
```
