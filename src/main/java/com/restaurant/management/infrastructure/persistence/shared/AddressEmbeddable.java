package com.restaurant.management.infrastructure.persistence.shared;

import com.restaurant.management.core.domain.shared.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Endereço embutido nas tabelas da Fase 2, mapeado para o {@link Address} de domínio.
 * <p>
 * As colunas têm os mesmos nomes do endereço dos usuários; o que muda é que aqui os
 * campos obrigatórios são {@code NOT NULL} no banco.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressEmbeddable {

    @Column(name = "street", nullable = false, length = Address.STREET_MAX_LENGTH)
    private String street;

    @Column(name = "number", nullable = false, length = Address.NUMBER_MAX_LENGTH)
    private String number;

    @Column(name = "complement", length = Address.COMPLEMENT_MAX_LENGTH)
    private String complement;

    @Column(name = "city", nullable = false, length = Address.CITY_MAX_LENGTH)
    private String city;

    @Column(name = "state", nullable = false, length = Address.STATE_MAX_LENGTH)
    private String state;

    @Column(name = "zip_code", nullable = false, length = Address.ZIP_CODE_MAX_LENGTH)
    private String zipCode;

    public static AddressEmbeddable fromDomain(Address address) {
        return new AddressEmbeddable(address.street(), address.number(), address.complement(),
                address.city(), address.state(), address.zipCode());
    }

    public Address toDomain() {
        return new Address(street, number, complement, city, state, zipCode);
    }
}
