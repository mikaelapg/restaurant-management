package com.restaurant.management.infrastructure.web.restaurantowner;

import com.restaurant.management.core.application.user.CreateUserCommand;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import com.restaurant.management.infrastructure.web.shared.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Corpo do cadastro de dono de restaurante. */
@Schema(description = "Dados para cadastro de um dono de restaurante")
public record CreateRestaurantOwnerRequest(
        @Schema(description = "Nome completo", example = "João Silva")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres.")
        String name,

        @Schema(description = "E-mail, único no sistema", example = "joao@restaurante.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Schema(description = "Login, único no sistema", example = "joao.silva")
        @NotBlank(message = "O login é obrigatório.")
        @Size(min = 3, max = 100, message = "O login deve ter entre 3 e 100 caracteres.")
        String login,

        @Schema(description = "Senha: mínimo 8 caracteres, com minúscula, maiúscula, dígito e caractere especial",
                example = "Senha@123")
        @NotBlank(message = "A senha é obrigatória.")
        @StrongPassword
        String password,

        @Schema(description = "Endereço (opcional; se informado, precisa estar completo)")
        @Valid
        AddressDto address,

        @Schema(description = "Nome fantasia ou razão social da empresa do dono (opcional)",
                example = "Grupo Silva Alimentação")
        @Size(max = 255, message = "O nome do restaurante deve ter no máximo 255 caracteres.")
        String restaurantName,

        @Schema(description = "CNPJ (opcional)", example = "12.345.678/0001-90")
        @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", message = "O CNPJ deve seguir o formato 00.000.000/0000-00.")
        String cnpj
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(name, email, login, password, AddressDto.toNullableDomain(address),
                new RestaurantOwnerDetails(restaurantName, cnpj));
    }
}
