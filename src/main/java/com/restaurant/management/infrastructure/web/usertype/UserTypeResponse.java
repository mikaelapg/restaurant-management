package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.core.domain.usertype.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

/** O que a API devolve sobre um tipo de usuário. */
public record UserTypeResponse(
        @Schema(description = "Identificador", example = "1")
        Long id,

        @Schema(description = "Nome do tipo de usuário", example = "Dono de Restaurante")
        String name,

        @Schema(description = "Indica se é um tipo de sistema (\"Dono de Restaurante\" ou \"Cliente\"), "
                + "que pode ser renomeado, mas não excluído", example = "true")
        boolean systemType,

        @Schema(description = "Perfil ligado ao tipo de sistema; nulo nos demais", example = "RESTAURANT_OWNER")
        UserProfile profile
) {

    public static UserTypeResponse fromDomain(UserType userType) {
        return new UserTypeResponse(userType.getId(), userType.getName(), userType.isSystemType(),
                userType.getProfile());
    }

    /** Para o tipo associado a um usuário, que pode não existir. */
    public static UserTypeResponse fromNullable(UserType userType) {
        return userType == null ? null : fromDomain(userType);
    }
}
