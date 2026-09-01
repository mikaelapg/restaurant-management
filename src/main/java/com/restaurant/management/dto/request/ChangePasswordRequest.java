package com.restaurant.management.dto.request;

import com.restaurant.management.dto.request.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

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
        @StrongPassword
        @Schema(description = "Nova senha: mínimo 8 caracteres, com minúscula, maiúscula, dígito e caractere especial", example = "NovaSenha@456")
        String newPassword,

        @NotBlank(message = "A confirmação da nova senha é obrigatória")
        @Schema(description = "Confirmação da nova senha", example = "NovaSenha@456")
        String confirmNewPassword
) {
}
