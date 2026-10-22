package com.restaurant.management.core.domain.shared;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Address (objeto de valor)")
class AddressTest {

    private static Address withStreet(String street) {
        return new Address(street, "100", null, "São Paulo", "SP", "01000-000");
    }

    @Test
    @DisplayName("remove espaços das pontas e transforma complemento em branco em nulo")
    void shouldNormalize() {
        Address address = new Address(" Rua A ", " 10 ", "   ", " São Paulo ", " SP ", " 01000-000 ");

        assertThat(address.street()).isEqualTo("Rua A");
        assertThat(address.number()).isEqualTo("10");
        assertThat(address.complement()).isNull();
        assertThat(address.city()).isEqualTo("São Paulo");
        assertThat(address.state()).isEqualTo("SP");
        assertThat(address.zipCode()).isEqualTo("01000-000");
    }

    @Test
    @DisplayName("mantém o complemento informado")
    void shouldKeepComplement() {
        assertThat(new Address("Rua A", "10", " Loja 2 ", "SP", "SP", "1").complement()).isEqualTo("Loja 2");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("recusa campo obrigatório ausente")
    void shouldRejectMissingRequiredField(String street) {
        assertThatThrownBy(() -> withStreet(street))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("logradouro");
    }

    @Test
    @DisplayName("recusa campo acima do tamanho máximo")
    void shouldRejectTooLong() {
        assertThatThrownBy(() -> withStreet("a".repeat(Address.STREET_MAX_LENGTH + 1)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("255");
    }

    @Test
    @DisplayName("recusa complemento acima do tamanho máximo")
    void shouldRejectTooLongComplement() {
        assertThatThrownBy(() -> new Address("Rua", "1", "c".repeat(256), "SP", "SP", "1"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("complemento");
    }

    @Test
    @DisplayName("endereços com os mesmos campos são iguais")
    void shouldCompareByValue() {
        assertThat(withStreet("Rua A")).isEqualTo(withStreet("Rua A")).isNotEqualTo(withStreet("Rua B"));
    }
}
