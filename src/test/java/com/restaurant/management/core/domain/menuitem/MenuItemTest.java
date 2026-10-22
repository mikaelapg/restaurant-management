package com.restaurant.management.core.domain.menuitem;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MenuItem (entidade de domínio)")
class MenuItemTest {

    private static MenuItem withPrice(String price) {
        return MenuItem.create(1L, "Risoto", "Arroz arbóreo", new BigDecimal(price), false, null);
    }

    @Test
    @DisplayName("cria sem id, apara os textos e guarda o preço com duas casas")
    void shouldCreate() {
        MenuItem item = MenuItem.create(1L, " Risoto ", " Arroz arbóreo ", new BigDecimal("68.9"), true, " /img/r.jpg ");

        assertThat(item.getId()).isNull();
        assertThat(item.getRestaurantId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Risoto");
        assertThat(item.getDescription()).isEqualTo("Arroz arbóreo");
        assertThat(item.getPrice()).isEqualTo(new BigDecimal("68.90"));
        assertThat(item.isDineInOnly()).isTrue();
        assertThat(item.getPhotoPath()).isEqualTo("/img/r.jpg");
    }

    @Test
    @DisplayName("caminho da foto é opcional; em branco vira nulo")
    void shouldAcceptMissingPhoto() {
        assertThat(MenuItem.create(1L, "R", "D", BigDecimal.TEN, false, "  ").getPhotoPath()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.00"})
    @DisplayName("recusa preço zero ou negativo")
    void shouldRejectNonPositivePrice(String price) {
        assertThatThrownBy(() -> withPrice(price))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    @DisplayName("recusa preço com mais de duas casas decimais, mas aceita zeros à direita")
    void shouldRejectTooManyDecimals() {
        assertThatThrownBy(() -> withPrice("10.999"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("duas casas");
        assertThat(withPrice("10.500").getPrice()).isEqualTo(new BigDecimal("10.50"));
    }

    @Test
    @DisplayName("recusa preço acima do máximo da coluna e preço ausente")
    void shouldRejectTooHighOrMissingPrice() {
        assertThatThrownBy(() -> withPrice("100000000.00"))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> MenuItem.create(1L, "R", "D", null, false, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("recusa nome, descrição ou restaurante ausentes e textos longos demais")
    void shouldRejectInvalidTexts() {
        assertThatThrownBy(() -> MenuItem.create(1L, " ", "D", BigDecimal.ONE, false, null))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("nome");
        assertThatThrownBy(() -> MenuItem.create(1L, "R", null, BigDecimal.ONE, false, null))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("descrição");
        assertThatThrownBy(() -> MenuItem.create(null, "R", "D", BigDecimal.ONE, false, null))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("restaurante");
        assertThatThrownBy(() -> MenuItem.create(1L, "R", "D", BigDecimal.ONE, false, "p".repeat(501)))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("500");
    }

    @Test
    @DisplayName("update mantém id e restaurante; restore exige id")
    void shouldUpdateKeepingIdentity() {
        MenuItem original = MenuItem.restore(3L, 1L, "Risoto", "D", BigDecimal.TEN, false, null);

        MenuItem updated = original.update("Risoto de funghi", "Nova", new BigDecimal("12"), true, "/p.jpg");

        assertThat(updated).isEqualTo(original).hasSameHashCodeAs(original);
        assertThat(updated.getRestaurantId()).isEqualTo(1L);
        assertThat(updated.getName()).isEqualTo("Risoto de funghi");
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("12.00"));
        assertThat(original.getName()).isEqualTo("Risoto");
        assertThatThrownBy(() -> MenuItem.restore(null, 1L, "R", "D", BigDecimal.ONE, false, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("itens sem id só são iguais a si mesmos")
    void shouldNotEqualWithoutId() {
        MenuItem item = withPrice("1");

        assertThat(item).isEqualTo(item).isNotEqualTo(withPrice("1")).isNotEqualTo("Risoto");
    }
}
