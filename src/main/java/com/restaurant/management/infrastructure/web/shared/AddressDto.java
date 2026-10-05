package com.restaurant.management.infrastructure.web.shared;

import com.restaurant.management.core.domain.shared.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Endereço nos corpos de entrada e de saída da API da Fase 2.
 * <p>
 * As anotações repetem as regras do {@link Address} de domínio para que a API responda
 * 400 com {@code fieldErrors} apontando cada campo.
 */
@Schema(description = "Endereço")
public record AddressDto(

        @Schema(description = "Logradouro", example = "Rua Augusta")
        @NotBlank(message = "O logradouro é obrigatório.")
        @Size(max = Address.STREET_MAX_LENGTH, message = "O logradouro deve ter no máximo 255 caracteres.")
        String street,

        @Schema(description = "Número", example = "1500")
        @NotBlank(message = "O número é obrigatório.")
        @Size(max = Address.NUMBER_MAX_LENGTH, message = "O número deve ter no máximo 20 caracteres.")
        String number,

        @Schema(description = "Complemento (opcional)", example = "Loja 2")
        @Size(max = Address.COMPLEMENT_MAX_LENGTH, message = "O complemento deve ter no máximo 255 caracteres.")
        String complement,

        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank(message = "A cidade é obrigatória.")
        @Size(max = Address.CITY_MAX_LENGTH, message = "A cidade deve ter no máximo 100 caracteres.")
        String city,

        @Schema(description = "Estado", example = "SP")
        @NotBlank(message = "O estado é obrigatório.")
        @Size(max = Address.STATE_MAX_LENGTH, message = "O estado deve ter no máximo 50 caracteres.")
        String state,

        @Schema(description = "CEP", example = "01305-100")
        @NotBlank(message = "O CEP é obrigatório.")
        @Size(max = Address.ZIP_CODE_MAX_LENGTH, message = "O CEP deve ter no máximo 20 caracteres.")
        String zipCode
) {

    public Address toDomain() {
        return new Address(street, number, complement, city, state, zipCode);
    }

    public static AddressDto fromDomain(Address address) {
        return new AddressDto(address.street(), address.number(), address.complement(),
                address.city(), address.state(), address.zipCode());
    }
}
