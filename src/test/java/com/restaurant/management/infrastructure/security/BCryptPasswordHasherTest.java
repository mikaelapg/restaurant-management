package com.restaurant.management.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BCryptPasswordHasher")
class BCryptPasswordHasherTest {

    /** Custo 4, o mínimo do BCrypt, para o teste não demorar; a aplicação usa 12. */
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(new BCryptPasswordEncoder(4));

    @Test
    @DisplayName("gera hash BCrypt que confere com a senha original e não com outra")
    void shouldHashAndMatch() {
        String hash = hasher.hash("Senha@123");

        assertThat(hash).startsWith("$2a$").isNotEqualTo("Senha@123");
        assertThat(hasher.matches("Senha@123", hash)).isTrue();
        assertThat(hasher.matches("Outra@123", hash)).isFalse();
    }

    @Test
    @DisplayName("senha ou hash nulos nunca conferem")
    void shouldNotMatchNulls() {
        assertThat(hasher.matches(null, "hash")).isFalse();
        assertThat(hasher.matches("Senha@123", null)).isFalse();
    }

    @Test
    @DisplayName("o construtor padrão usa custo 12")
    void shouldUseStrength12ByDefault() {
        assertThat(new BCryptPasswordHasher().hash("Senha@123")).startsWith("$2a$12$");
    }
}
