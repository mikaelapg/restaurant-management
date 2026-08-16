package com.restaurant.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuração do encoder de senha BCrypt.
 * <p>
 * Utiliza apenas o módulo de criptografia do Spring Security (spring-security-crypto),
 * sem ativar nenhum filtro de segurança ou autenticação automática.
 */
@Configuration
public class SecurityConfig {

    /**
     * Bean do encoder BCrypt para hashing de senhas.
     * Strength 12 oferece bom equilíbrio entre segurança e performance.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
