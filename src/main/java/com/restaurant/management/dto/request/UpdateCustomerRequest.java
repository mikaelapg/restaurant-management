package com.restaurant.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para atualização dos dados de um Cliente.
 * <p>
 * Não inclui senha — a troca de senha é feita via endpoint exclusivo
 * {@code PATCH /api/v1/customers/{id}/password}.
 */
@Schema(description = "Dados para atualização do cliente (exceto senha)")
public record UpdateCustomerRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 255)
        @Schema(description = "Novo nome completo", example = "Maria A. Oliveira")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        @Schema(description = "Novo e-mail", example = "maria.nova@email.com")
        String email,

        @NotBlank(message = "O login é obrigatório")
        @Size(min = 3, max = 100)
        @Schema(description = "Novo login", example = "maria.a.oliveira")
        String login,

        @Valid
        @Schema(description = "Novo endereço")
        AddressRequest address,

        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "O CPF deve seguir o formato 000.000.000-00")
        @Schema(description = "Novo CPF", example = "987.654.321-00")
        String cpf,

        @Past(message = "A data de nascimento deve estar no passado")
        @Schema(description = "Nova data de nascimento", example = "1990-03-22", type = "string")
        LocalDate dateOfBirth
) {
}
