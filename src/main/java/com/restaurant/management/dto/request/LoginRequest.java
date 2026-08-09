package com.restaurant.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para validação de login do usuário.
 * <p>
 * Utilizado no endpoint {@code POST /api/v1/auth/login}.
 */
@Schema(description = "Credenciais para login no sistema")
public record LoginRequest(

        @NotBlank(message = "O login é obrigatório")
        @Schema(description = "Login do usuário", example = "joao.silva")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        @Schema(description = "Senha do usuário", example = "Senha@123")
        String password
) {
}
