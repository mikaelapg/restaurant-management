package com.restaurant.management.infrastructure.web.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Credenciais para {@code POST /api/v1/auth/login}. */
@Schema(description = "Credenciais de acesso")
public record LoginRequest(
        @Schema(description = "Login", example = "joao.silva")
        @NotBlank(message = "O login é obrigatório.")
        String login,

        @Schema(description = "Senha", example = "Senha@123")
        @NotBlank(message = "A senha é obrigatória.")
        String password
) {
}
