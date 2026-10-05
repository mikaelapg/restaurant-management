package com.restaurant.management.core.domain.restaurant;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.shared.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Restaurant (entidade de domínio)")
class RestaurantTest {

    static final Address ADDRESS = new Address("Rua Augusta", "1500", null, "São Paulo", "SP", "01305-100");
    static final OpeningHours HOURS = new OpeningHours(LocalTime.of(11, 0), LocalTime.of(23, 0));

    @Test
    @DisplayName("cria sem id e com os textos aparados")
    void shouldCreate() {
        Restaurant restaurant = Restaurant.create("  Cantina  ", ADDRESS, " Italiana ", HOURS, 1L);

        assertThat(restaurant.getId()).isNull();
        assertThat(restaurant.getName()).isEqualTo("Cantina");
        assertThat(restaurant.getCuisineType()).isEqualTo("Italiana");
        assertThat(restaurant.getAddress()).isEqualTo(ADDRESS);
        assertThat(restaurant.getOpeningHours()).isEqualTo(HOURS);
        assertThat(restaurant.getOwnerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("restore exige id")
    void shouldRequireIdOnRestore() {
        assertThatThrownBy(() -> Restaurant.restore(null, "Cantina", ADDRESS, "Italiana", HOURS, 1L))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("update devolve outra instância com o mesmo id e os dados novos")
    void shouldUpdateKeepingId() {
        Restaurant original = Restaurant.restore(5L, "Cantina", ADDRESS, "Italiana", HOURS, 1L);

        Restaurant updated = original.update("Cantina Nova", ADDRESS, "Massas", HOURS, 2L);

        assertThat(updated).isNotSameAs(original).isEqualTo(original);
        assertThat(updated.getId()).isEqualTo(5L);
        assertThat(updated.getName()).isEqualTo("Cantina Nova");
        assertThat(updated.getOwnerId()).isEqualTo(2L);
        assertThat(original.getName()).isEqualTo("Cantina");
    }

    @Test
    @DisplayName("recusa nome vazio ou longo demais")
    void shouldRejectInvalidName() {
        assertThatThrownBy(() -> Restaurant.create(" ", ADDRESS, "Italiana", HOURS, 1L))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("nome");
        assertThatThrownBy(() -> Restaurant.create("a".repeat(101), ADDRESS, "Italiana", HOURS, 1L))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("100");
    }

    @Test
    @DisplayName("recusa tipo de cozinha vazio")
    void shouldRejectBlankCuisine() {
        assertThatThrownBy(() -> Restaurant.create("Cantina", ADDRESS, null, HOURS, 1L))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("tipo de cozinha");
    }

    @Test
    @DisplayName("recusa endereço, horário ou dono ausentes")
    void shouldRejectMissingParts() {
        assertThatThrownBy(() -> Restaurant.create("Cantina", null, "Italiana", HOURS, 1L))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("endereço");
        assertThatThrownBy(() -> Restaurant.create("Cantina", ADDRESS, "Italiana", null, 1L))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("horário");
        assertThatThrownBy(() -> Restaurant.create("Cantina", ADDRESS, "Italiana", HOURS, null))
                .isInstanceOf(DomainValidationException.class).hasMessageContaining("dono");
    }

    @Test
    @DisplayName("igualdade por id; sem id, só é igual a si mesmo")
    void shouldCompareById() {
        Restaurant a = Restaurant.restore(1L, "A", ADDRESS, "X", HOURS, 1L);
        Restaurant b = Restaurant.restore(1L, "B", ADDRESS, "Y", HOURS, 2L);
        Restaurant fresh = Restaurant.create("A", ADDRESS, "X", HOURS, 1L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(fresh).isNotEqualTo(Restaurant.create("A", ADDRESS, "X", HOURS, 1L)).isEqualTo(fresh);
        assertThat(a).isNotEqualTo("A");
    }
}
