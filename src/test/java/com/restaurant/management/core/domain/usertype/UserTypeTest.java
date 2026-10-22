package com.restaurant.management.core.domain.usertype;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.user.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Teste de domínio: sem Spring, sem mock, sem banco. Só a classe e as regras dela.
 * É o tipo de teste mais rápido e mais barato do projeto.
 */
@DisplayName("UserType (domínio)")
class UserTypeTest {

    @Test
    @DisplayName("create gera um tipo sem id e com o nome sem espaços nas pontas")
    void shouldCreateWithTrimmedName() {
        UserType userType = UserType.create("  Cliente  ");

        assertThat(userType.getId()).isNull();
        assertThat(userType.getName()).isEqualTo("Cliente");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("create recusa nome nulo, vazio ou só com espaços")
    void shouldRejectBlankName(String name) {
        assertThatThrownBy(() -> UserType.create(name))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("obrigatório");
    }

    @Test
    @DisplayName("create aceita exatamente 50 caracteres e recusa 51")
    void shouldEnforceMaxLength() {
        assertThat(UserType.create("a".repeat(50)).getName()).hasSize(50);

        assertThatThrownBy(() -> UserType.create("a".repeat(51)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("50");
    }

    @Test
    @DisplayName("restore exige id")
    void shouldRequireIdOnRestore() {
        assertThatThrownBy(() -> UserType.restore(null, "Cliente"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("rename devolve outra instância com o mesmo id e não altera a original")
    void shouldRenameImmutably() {
        UserType original = UserType.restore(1L, "Cliente");

        UserType renamed = original.rename("Consumidor");

        assertThat(renamed.getId()).isEqualTo(1L);
        assertThat(renamed.getName()).isEqualTo("Consumidor");
        assertThat(original.getName()).isEqualTo("Cliente");
    }

    @Test
    @DisplayName("rename também valida o nome novo")
    void shouldValidateOnRename() {
        UserType original = UserType.restore(1L, "Cliente");

        assertThatThrownBy(() -> original.rename(" "))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("dois tipos são iguais quando têm o mesmo id; sem id, só a própria instância")
    void shouldCompareById() {
        assertThat(UserType.restore(1L, "Cliente")).isEqualTo(UserType.restore(1L, "Outro nome"));
        assertThat(UserType.restore(1L, "Cliente")).isNotEqualTo(UserType.restore(2L, "Cliente"));
        assertThat(UserType.create("Cliente")).isNotEqualTo(UserType.create("Cliente"));
        assertThat(UserType.restore(1L, "Cliente")).hasSameHashCodeAs(UserType.restore(2L, "Outro"));
    }

    @Test
    @DisplayName("tipos criados pela API não são de sistema; os restaurados com perfil são, e renomear mantém o perfil")
    void shouldTrackSystemTypes() {
        assertThat(UserType.create("Gerente").isSystemType()).isFalse();
        assertThat(UserType.create("Gerente").getProfile()).isNull();

        UserType owner = UserType.restore(1L, "Dono de Restaurante", UserProfile.RESTAURANT_OWNER);
        assertThat(owner.isSystemType()).isTrue();
        assertThat(owner.rename("Proprietário").getProfile()).isEqualTo(UserProfile.RESTAURANT_OWNER);
    }
}
