package com.restaurant.management.infrastructure.security;

import com.restaurant.management.core.application.user.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * {@link PasswordHasher} com BCrypt.
 * <p>
 * Usa só o módulo de criptografia do Spring Security ({@code spring-security-crypto}),
 * sem filtros nem autenticação automática. Custo 12: cada hash leva algumas centenas de
 * milissegundos, o que encarece ataques de força bruta sem pesar no uso normal.
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int STRENGTH = 12;

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasher() {
        this(new BCryptPasswordEncoder(STRENGTH));
    }

    BCryptPasswordHasher(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return rawPassword != null && passwordHash != null && encoder.matches(rawPassword, passwordHash);
    }
}
