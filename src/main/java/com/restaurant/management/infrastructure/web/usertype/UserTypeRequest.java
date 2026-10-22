package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.domain.usertype.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo de criação e de atualização de um tipo de usuário.
 * <p>
 * As anotações repetem as regras do domínio para que a API responda 400 com
 * {@code fieldErrors} por campo, antes mesmo de chegar ao caso de uso.
 */
public record UserTypeRequest(

        @Schema(description = "Nome do tipo de usuário", example = "Dono de Restaurante")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = UserType.NAME_MAX_LENGTH, message = "O nome deve ter no máximo 50 caracteres.")
        String name
) {
}
