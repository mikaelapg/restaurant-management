package com.restaurant.management.infrastructure.web.user;

import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Dados comuns de um usuário, seja ele cliente ou dono de restaurante. A senha nunca sai na resposta. */
@Schema(description = "Dados consolidados de usuário")
public record UserResponse(
        @Schema(description = "Identificador", example = "1")
        Long id,

        @Schema(description = "Perfil cadastral: CUSTOMER (cliente) ou RESTAURANT_OWNER (dono de restaurante)",
                example = "RESTAURANT_OWNER")
        UserProfile profile,

        @Schema(description = "Nome completo", example = "João Silva")
        String name,

        @Schema(description = "E-mail", example = "joao@restaurante.com")
        String email,

        @Schema(description = "Login", example = "joao.silva")
        String login,

        @Schema(description = "Data e hora da última alteração", example = "2026-10-22T10:00:00")
        LocalDateTime lastModifiedAt,

        @Schema(description = "Endereço; nulo se não informado")
        AddressDto address,

        @Schema(description = "Tipo de usuário associado; nulo se não houver")
        UserTypeResponse userType
) {

    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getProfile(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getLastModifiedAt(),
                AddressDto.fromNullable(user.getAddress()),
                UserTypeResponse.fromNullable(user.getUserType()));
    }
}
