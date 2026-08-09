package com.restaurant.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para troca de senha do usuário.
 * <p>
 * Utilizado no endpoint exclusivo {@code PATCH /api/v1/{tipo}/{id}/password}.
 * Requer a senha atual para confirmação antes de aplicar a nova.
 */
@Schema(description = "Dados para troca de senha do usuário")
public record ChangePasswordRequest(

        @NotBlank(message = "A senha atual é obrigatória")
        @Schema(description = "Senha atual do usuário", example = "SenhaAntiga@123")
        String currentPassword,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
        @Schema(description = "Nova senha (mínimo 6 caracteres)", example = "NovaSenha@456")
        String newPassword,

        @NotBlank(message = "A confirmação da nova senha é obrigatória")
        @Schema(description = "Confirmação da nova senha", example = "NovaSenha@456")
        String confirmNewPassword
) {
}
