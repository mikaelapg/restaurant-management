package com.restaurant.management.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO para o endereço embutido nos cadastros de usuário.
 * Todos os campos são opcionais — o usuário pode não ter um endereço cadastrado.
 */
@Builder
@Schema(description = "Endereço do usuário")
public record AddressRequest(

        @Schema(description = "Logradouro (rua, avenida etc.)", example = "Rua das Flores")
        String street,

        @Schema(description = "Número do imóvel", example = "123")
        String number,

        @Schema(description = "Complemento (apto, bloco)", example = "Apto 45")
        String complement,

        @Schema(description = "Cidade", example = "São Paulo")
        String city,

        @Schema(description = "Estado (UF)", example = "SP")
        String state,

        @Schema(description = "CEP (somente números ou com hífen)", example = "01310-100")
        String zipCode
) {
}
