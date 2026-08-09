package com.restaurant.management.dto.response;

import com.restaurant.management.domain.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para o endpoint de login.
 */
@Schema(description = "Resultado da validação de login")
public record LoginResponse(

        @Schema(description = "ID do usuário autenticado", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "João Silva")
        String name,

        @Schema(description = "Login do usuário", example = "joao.silva")
        String login,

        @Schema(description = "E-mail do usuário", example = "joao@restaurante.com")
        String email,

        @Schema(description = "Tipo de usuário", example = "RESTAURANT_OWNER")
        UserType userType,

        @Schema(description = "Mensagem de sucesso", example = "Login realizado com sucesso")
        String message
) {
}
