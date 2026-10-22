package com.restaurant.management.infrastructure.web.customer;

import com.restaurant.management.core.application.user.UpdateUserCommand;
import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Corpo da atualização de cliente. Não inclui senha: ela tem endpoint próprio. */
@Schema(description = "Dados para atualização do cliente (exceto senha)")
public record UpdateCustomerRequest(
        @Schema(description = "Nome completo", example = "Maria A. Oliveira")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres.")
        String name,

        @Schema(description = "E-mail", example = "maria.nova@email.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Schema(description = "Login", example = "maria.a.oliveira")
        @NotBlank(message = "O login é obrigatório.")
        @Size(min = 3, max = 100, message = "O login deve ter entre 3 e 100 caracteres.")
        String login,

        @Schema(description = "Endereço; se omitido, o atual é mantido")
        @Valid
        AddressDto address,

        @Schema(description = "CPF", example = "987.654.321-00")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00.")
        String cpf,

        @Schema(description = "Data de nascimento", example = "1990-03-22", type = "string")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dateOfBirth
) {

    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(name, email, login, AddressDto.toNullableDomain(address),
                new CustomerDetails(cpf, dateOfBirth));
    }
}
