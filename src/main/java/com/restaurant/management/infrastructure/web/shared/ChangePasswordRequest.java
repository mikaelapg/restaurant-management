package com.restaurant.management.infrastructure.web.shared;

import com.restaurant.management.infrastructure.web.shared.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Corpo da troca de senha, usado por clientes e donos em {@code PATCH /{id}/password}. */
@Schema(description = "Dados para troca de senha")
public record ChangePasswordRequest(
        @Schema(description = "Senha atual", example = "Senha@123")
        @NotBlank(message = "A senha atual é obrigatória.")
        String currentPassword,

        @Schema(description = "Nova senha: mínimo 8 caracteres, com minúscula, maiúscula, dígito e caractere especial",
                example = "NovaSenha@456")
        @NotBlank(message = "A nova senha é obrigatória.")
        @StrongPassword
        String newPassword,

        @Schema(description = "Confirmação da nova senha", example = "NovaSenha@456")
        @NotBlank(message = "A confirmação da nova senha é obrigatória.")
        String confirmNewPassword
) {
}
