package com.restaurant.management.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI / Swagger UI.
 * <p>
 * Swagger UI disponível em: http://localhost:8080/swagger-ui.html
 * API Docs JSON em:          http://localhost:8080/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI restaurantManagementOpenAPI() {
        // Registra explicitamente o Schema do ProblemDetail (RFC 7807) para evitar $ref quebrado
        Schema<?> problemDetailSchema = new ObjectSchema()
                .description("Formato padronizado de erro RFC 7807 (ProblemDetail)")
                .addProperty("type", new StringSchema().example("https://restaurant-api.com/errors/validation-error"))
                .addProperty("title", new StringSchema().example("Erro de Validação"))
                .addProperty("status", new Schema<Integer>().example(400))
                .addProperty("detail", new StringSchema().example("Um ou mais campos da requisição são inválidos."))
                .addProperty("instance", new StringSchema().example("/api/v1/customers"))
                .addProperty("timestamp", new StringSchema().example("2026-08-24T20:00:00Z"))
                .addProperty("fieldErrors", new MapSchema().description("Mapa de campos inválidos e seus respectivos erros"));

        return new OpenAPI()
                .components(new Components().addSchemas("ProblemDetail", problemDetailSchema))
                .info(new Info()
                        .title("Sistema de Gestão de Restaurantes — API")
                        .description("""
                                Backend do sistema compartilhado de gestão de restaurantes.
                                
                                **Versão da API:** v1 (`/api/v1/`)
                                
                                **Tipos de usuário:** cadastro em `/api/v1/user-types`. Os tipos de sistema
                                "Dono de Restaurante" e "Cliente" já vêm cadastrados, e cada usuário nasce
                                com o do seu perfil (`RESTAURANT_OWNER` ou `CUSTOMER`). Só quem tem o tipo
                                "Dono de Restaurante" pode ser responsável por um restaurante.

                                **Restaurantes:** `/api/v1/restaurants`. **Cardápio:** `/api/v1/restaurants/{id}/menu-items`.

                                **Autenticação:** Validação simples via endpoint `/api/v1/auth/login`.
                                
                                **Padrão de erros:** RFC 7807 — ProblemDetail
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@restaurant.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Ambiente Local"),
                        new Server().url("http://restaurant-app:8080").description("Ambiente Docker")
                ));
    }
}
