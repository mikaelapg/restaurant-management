package com.restaurant.management.infrastructure.web.restaurantowner;

import com.restaurant.management.core.application.user.UpdateUserCommand;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Corpo da atualização de dono de restaurante. Não inclui senha: ela tem endpoint próprio. */
@Schema(description = "Dados para atualização do dono de restaurante (exceto senha)")
public record UpdateRestaurantOwnerRequest(
        @Schema(description = "Nome completo", example = "João P. Silva")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres.")
        String name,

        @Schema(description = "E-mail", example = "joao.novo@restaurante.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Schema(description = "Login", example = "joao.p.silva")
        @NotBlank(message = "O login é obrigatório.")
        @Size(min = 3, max = 100, message = "O login deve ter entre 3 e 100 caracteres.")
        String login,

        @Schema(description = "Endereço; se omitido, o atual é mantido")
        @Valid
        AddressDto address,

        @Schema(description = "Nome fantasia ou razão social da empresa do dono", example = "Grupo Silva Alimentação")
        @Size(max = 255, message = "O nome do restaurante deve ter no máximo 255 caracteres.")
        String restaurantName,

        @Schema(description = "CNPJ", example = "12.345.678/0001-90")
        @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", message = "O CNPJ deve seguir o formato 00.000.000/0000-00.")
        String cnpj
) {

    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(name, email, login, AddressDto.toNullableDomain(address),
                new RestaurantOwnerDetails(restaurantName, cnpj));
    }
}
