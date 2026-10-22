package com.restaurant.management.infrastructure.persistence.user;

import com.restaurant.management.core.domain.shared.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Endereço embutido na tabela {@code users}.
 * <p>
 * Diferente do endereço dos restaurantes, aqui as colunas aceitam nulo: o endereço do
 * usuário é opcional. Quando existe, chega completo, porque passou pelo {@link Address}
 * de domínio.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressEmbeddable {

    @Column(name = "street")
    private String street;

    @Column(name = "number")
    private String number;

    @Column(name = "complement")
    private String complement;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    /** Endereço nulo vira embutido nulo, e todas as colunas ficam nulas. */
    public static UserAddressEmbeddable fromDomain(Address address) {
        if (address == null) {
            return null;
        }
        return new UserAddressEmbeddable(address.street(), address.number(), address.complement(),
                address.city(), address.state(), address.zipCode());
    }

    public Address toDomain() {
        return new Address(street, number, complement, city, state, zipCode);
    }
}
