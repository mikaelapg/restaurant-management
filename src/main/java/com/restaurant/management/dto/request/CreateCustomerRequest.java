package com.restaurant.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para cadastro de um novo Cliente.
 */
@Schema(description = "Dados para cadastro de um cliente")
public record CreateCustomerRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
        @Schema(description = "Nome completo do cliente", example = "Maria Oliveira")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        @Schema(description = "E-mail único do usuário", example = "maria@email.com")
        String email,

        @NotBlank(message = "O login é obrigatório")
        @Size(min = 3, max = 100, message = "O login deve ter entre 3 e 100 caracteres")
        @Schema(description = "Login de acesso ao sistema", example = "maria.oliveira")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        @Schema(description = "Senha de acesso (mínimo 6 caracteres)", example = "Senha@456")
        String password,

        @Valid
        @Schema(description = "Endereço do cliente")
        AddressRequest address,

        @Schema(description = "CPF do cliente", example = "123.456.789-00")
        String cpf,

        @Schema(description = "Data de nascimento (YYYY-MM-DD)", example = "1995-07-15", type = "string")
        LocalDate dateOfBirth
) {
}
