package com.restaurant.management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para o endereço embutido.
 */
@Schema(description = "Endereço do usuário")
public record AddressResponse(

        @Schema(description = "Logradouro", example = "Rua das Flores")
        String street,

        @Schema(description = "Número", example = "123")
        String number,

        @Schema(description = "Complemento", example = "Apto 45")
        String complement,

        @Schema(description = "Cidade", example = "São Paulo")
        String city,

        @Schema(description = "Estado", example = "SP")
        String state,

        @Schema(description = "CEP", example = "01310-100")
        String zipCode
) {
}
