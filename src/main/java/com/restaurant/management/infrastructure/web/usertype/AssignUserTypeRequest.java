package com.restaurant.management.infrastructure.web.usertype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Corpo da associação de um usuário a um tipo de usuário. */
public record AssignUserTypeRequest(

        @Schema(description = "Id do tipo de usuário a associar", example = "1")
        @NotNull(message = "O id do tipo de usuário é obrigatório.")
        Long userTypeId
) {
}
