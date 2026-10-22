package com.restaurant.management.infrastructure.web.customer;

import com.restaurant.management.core.application.user.CreateUserCommand;
import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import com.restaurant.management.infrastructure.web.shared.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Corpo do cadastro de cliente. */
@Schema(description = "Dados para cadastro de um cliente")
public record CreateCustomerRequest(
        @Schema(description = "Nome completo", example = "Maria Oliveira")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres.")
        String name,

        @Schema(description = "E-mail, único no sistema", example = "maria@email.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Schema(description = "Login, único no sistema", example = "maria.oliveira")
        @NotBlank(message = "O login é obrigatório.")
        @Size(min = 3, max = 100, message = "O login deve ter entre 3 e 100 caracteres.")
        String login,

        @Schema(description = "Senha: mínimo 8 caracteres, com minúscula, maiúscula, dígito e caractere especial",
                example = "Senha@456")
        @NotBlank(message = "A senha é obrigatória.")
        @StrongPassword
        String password,

        @Schema(description = "Endereço (opcional; se informado, precisa estar completo)")
        @Valid
        AddressDto address,

        @Schema(description = "CPF (opcional)", example = "123.456.789-00")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00.")
        String cpf,

        @Schema(description = "Data de nascimento (opcional)", example = "1995-07-15", type = "string")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dateOfBirth
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(name, email, login, password, AddressDto.toNullableDomain(address),
                new CustomerDetails(cpf, dateOfBirth));
    }
}
