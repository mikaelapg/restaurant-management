package com.restaurant.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para cadastro de um novo Dono de Restaurante.
 */
@Schema(description = "Dados para cadastro de um dono de restaurante")
public record CreateRestaurantOwnerRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
        @Schema(description = "Nome completo do dono", example = "João Silva")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        @Schema(description = "E-mail único do usuário", example = "joao@restaurante.com")
        String email,

        @NotBlank(message = "O login é obrigatório")
        @Size(min = 3, max = 100, message = "O login deve ter entre 3 e 100 caracteres")
        @Schema(description = "Login de acesso ao sistema", example = "joao.silva")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        @Schema(description = "Senha de acesso (mínimo 6 caracteres)", example = "Senha@123")
        String password,

        @Valid
        @Schema(description = "Endereço do dono do restaurante")
        AddressRequest address,

        @Schema(description = "Nome do restaurante", example = "Restaurante do João")
        String restaurantName,

        @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", message = "O CNPJ deve seguir o formato 00.000.000/0000-00")
        @Schema(description = "CNPJ do restaurante", example = "12.345.678/0001-90")
        String cnpj
) {
}
