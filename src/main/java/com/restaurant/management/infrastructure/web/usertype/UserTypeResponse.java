package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.domain.usertype.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

/** O que a API devolve sobre um tipo de usuário. */
public record UserTypeResponse(

        @Schema(description = "Identificador", example = "1")
        Long id,

        @Schema(description = "Nome do tipo de usuário", example = "Dono de Restaurante")
        String name
) {

    public static UserTypeResponse fromDomain(UserType userType) {
        return new UserTypeResponse(userType.getId(), userType.getName());
    }
}
