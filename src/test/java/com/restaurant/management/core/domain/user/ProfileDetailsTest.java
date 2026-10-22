package com.restaurant.management.core.domain.user;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dados do perfil (domínio)")
class ProfileDetailsTest {

    @Test
    @DisplayName("cliente: aceita CPF formatado, troca branco por nulo e é do perfil CUSTOMER")
    void shouldBuildCustomerDetails() {
        CustomerDetails details = new CustomerDetails(" 123.456.789-00 ", LocalDate.of(1990, 1, 1));

        assertThat(details.cpf()).isEqualTo("123.456.789-00");
        assertThat(details.profile()).isEqualTo(UserProfile.CUSTOMER);
        assertThat(new CustomerDetails("  ", null).cpf()).isNull();
    }

    @Test
    @DisplayName("cliente: recusa CPF fora do formato")
    void shouldRejectInvalidCpf() {
        assertThatThrownBy(() -> new CustomerDetails("12345678900", null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    @DisplayName("cliente: recusa data de nascimento hoje ou no futuro")
    void shouldRejectFutureBirthDate() {
        assertThatThrownBy(() -> new CustomerDetails(null, LocalDate.now()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("passado");
    }

    @Test
    @DisplayName("dono: aceita CNPJ formatado, troca branco por nulo e é do perfil RESTAURANT_OWNER")
    void shouldBuildOwnerDetails() {
        RestaurantOwnerDetails details = new RestaurantOwnerDetails(" Grupo ", "12.345.678/0001-90");

        assertThat(details.restaurantName()).isEqualTo("Grupo");
        assertThat(details.profile()).isEqualTo(UserProfile.RESTAURANT_OWNER);
        RestaurantOwnerDetails empty = new RestaurantOwnerDetails(" ", "");
        assertThat(empty.restaurantName()).isNull();
        assertThat(empty.cnpj()).isNull();
    }

    @Test
    @DisplayName("dono: recusa CNPJ fora do formato e nome longo demais")
    void shouldRejectInvalidOwnerDetails() {
        assertThatThrownBy(() -> new RestaurantOwnerDetails(null, "12345678000190"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("CNPJ");
        assertThatThrownBy(() -> new RestaurantOwnerDetails("a".repeat(256), null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("senha: aceita senha forte e recusa as que faltam algum requisito")
    void shouldApplyPasswordPolicy() {
        assertThat(PasswordPolicy.isStrong("Senha@123")).isTrue();
        assertThat(PasswordPolicy.isStrong(null)).isFalse();
        assertThat(PasswordPolicy.isStrong("senha@123")).isFalse();
        assertThat(PasswordPolicy.isStrong("SENHA@123")).isFalse();
        assertThat(PasswordPolicy.isStrong("Senha@abc")).isFalse();
        assertThat(PasswordPolicy.isStrong("Senha123")).isFalse();
        assertThat(PasswordPolicy.isStrong("Se@1")).isFalse();
        assertThat(PasswordPolicy.isStrong("Senha @123")).isFalse();

        PasswordPolicy.validate("Senha@123");
        assertThatThrownBy(() -> PasswordPolicy.validate("fraca"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("8 caracteres");
    }
}
