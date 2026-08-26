package com.restaurant.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para atualização dos dados de um Dono de Restaurante.
 * <p>
 * Não inclui senha — a troca de senha é feita via endpoint exclusivo
 * {@code PATCH /api/v1/restaurant-owners/{id}/password}.
 */
@Schema(description = "Dados para atualização do dono de restaurante (exceto senha)")
public record UpdateRestaurantOwnerRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
        @Schema(description = "Novo nome completo", example = "João P. Silva")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        @Schema(description = "Novo e-mail", example = "joao.novo@restaurante.com")
        String email,

        @NotBlank(message = "O login é obrigatório")
        @Size(min = 3, max = 100)
        @Schema(description = "Novo login", example = "joao.p.silva")
        String login,

        @Valid
        @Schema(description = "Novo endereço")
        AddressRequest address,

        @Schema(description = "Novo nome do restaurante", example = "Restaurante Premium do João")
        String restaurantName,

        @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", message = "O CNPJ deve seguir o formato 00.000.000/0000-00")
        @Schema(description = "Novo CNPJ", example = "12.345.678/0001-90")
        String cnpj
) {
}
