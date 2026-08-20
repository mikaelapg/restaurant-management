package com.restaurant.management.config;

/**
 * Payloads de exemplo usados na documentação OpenAPI.
 * <p>
 * Ficam centralizados aqui para que os mesmos corpos de erro apareçam
 * iguais em todos os endpoints — as respostas de erro seguem a RFC 7807.
 */
public final class OpenApiExamples {

    private OpenApiExamples() {
    }

    public static final String CUSTOMER_CREATED = """
            {
              "id": 2,
              "userType": "CUSTOMER",
              "name": "Maria Oliveira",
              "email": "maria@email.com",
              "login": "maria.oliveira",
              "lastModifiedAt": "2025-08-24T21:00:00",
              "address": {
                "street": "Rua das Flores",
                "number": "123",
                "complement": "Apto 45",
                "city": "São Paulo",
                "state": "SP",
                "zipCode": "01001-000"
              },
              "cpf": "123.456.789-00",
              "dateOfBirth": "1995-07-15"
            }""";

    public static final String OWNER_CREATED = """
            {
              "id": 1,
              "userType": "RESTAURANT_OWNER",
              "name": "João Silva",
              "email": "joao@restaurante.com",
              "login": "joao.silva",
              "lastModifiedAt": "2025-08-24T21:00:00",
              "address": {
                "street": "Av. Paulista",
                "number": "1000",
                "complement": "Conjunto 50",
                "city": "São Paulo",
                "state": "SP",
                "zipCode": "01310-100"
              },
              "restaurantName": "Restaurante do João",
              "cnpj": "12.345.678/0001-90"
            }""";

    public static final String USER_LIST = """
            [
              {
                "id": 1,
                "userType": "RESTAURANT_OWNER",
                "name": "João Silva",
                "email": "joao@restaurante.com",
                "login": "joao.silva",
                "lastModifiedAt": "2025-08-24T21:00:00",
                "address": {
                  "street": "Av. Paulista",
                  "number": "1000",
                  "complement": null,
                  "city": "São Paulo",
                  "state": "SP",
                  "zipCode": "01310-100"
                }
              },
              {
                "id": 2,
                "userType": "CUSTOMER",
                "name": "Maria Silva",
                "email": "maria@email.com",
                "login": "maria.silva",
                "lastModifiedAt": "2025-08-24T21:05:00",
                "address": null
              }
            ]""";

    public static final String LOGIN_SUCCESS = """
            {
              "id": 1,
              "name": "João Silva",
              "login": "joao.silva",
              "email": "joao@restaurante.com",
              "userType": "RESTAURANT_OWNER",
              "message": "Login realizado com sucesso"
            }""";

    public static final String VALIDATION_ERROR = """
            {
              "type": "https://restaurant-api.com/errors/validation-error",
              "title": "Erro de Validação",
              "status": 400,
              "detail": "Um ou mais campos da requisição são inválidos.",
              "instance": "/api/v1/customers",
              "timestamp": "2025-08-24T21:00:00Z",
              "fieldErrors": {
                "email": "O e-mail deve ser válido",
                "password": "A senha deve ter no mínimo 6 caracteres"
              }
            }""";

    public static final String INVALID_CREDENTIALS = """
            {
              "type": "https://restaurant-api.com/errors/invalid-credentials",
              "title": "Credenciais Inválidas",
              "status": 401,
              "detail": "Login ou senha inválidos. Por favor, verifique suas credenciais.",
              "instance": "/api/v1/auth/login",
              "timestamp": "2025-08-24T21:00:00Z"
            }""";

    public static final String USER_NOT_FOUND = """
            {
              "type": "https://restaurant-api.com/errors/user-not-found",
              "title": "Usuário Não Encontrado",
              "status": 404,
              "detail": "Usuário não encontrado com o ID: 99999",
              "instance": "/api/v1/customers/99999",
              "timestamp": "2025-08-24T21:00:00Z"
            }""";

    public static final String EMAIL_CONFLICT = """
            {
              "type": "https://restaurant-api.com/errors/email-conflict",
              "title": "E-mail Já Cadastrado",
              "status": 409,
              "detail": "Já existe um usuário cadastrado com o e-mail: maria@email.com",
              "instance": "/api/v1/customers",
              "timestamp": "2025-08-24T21:00:00Z"
            }""";

    public static final String INVALID_PASSWORD = """
            {
              "type": "https://restaurant-api.com/errors/invalid-password",
              "title": "Senha Inválida",
              "status": 422,
              "detail": "A senha atual informada está incorreta.",
              "instance": "/api/v1/customers/2/password",
              "timestamp": "2025-08-24T21:00:00Z"
            }""";
}
