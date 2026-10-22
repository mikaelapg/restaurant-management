package com.restaurant.management.infrastructure.web.auth;

import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/** Resultado de um login aceito. */
@Schema(description = "Resultado da validação de login")
public record LoginResponse(
        @Schema(description = "Identificador do usuário autenticado", example = "1")
        Long id,

        @Schema(description = "Nome", example = "João Silva")
        String name,

        @Schema(description = "Login", example = "joao.silva")
        String login,

        @Schema(description = "E-mail", example = "joao@restaurante.com")
        String email,

        @Schema(description = "Perfil cadastral", example = "RESTAURANT_OWNER")
        UserProfile profile,

        @Schema(description = "Tipo de usuário associado; nulo se não houver")
        UserTypeResponse userType,

        @Schema(description = "Mensagem", example = "Login realizado com sucesso")
        String message
) {

    public static LoginResponse fromDomain(User user) {
        return new LoginResponse(user.getId(), user.getName(), user.getLogin(), user.getEmail(),
                user.getProfile(), UserTypeResponse.fromNullable(user.getUserType()), "Login realizado com sucesso");
    }
}
