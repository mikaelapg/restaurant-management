package com.restaurant.management.core.application.user;

/**
 * Gera e confere hash de senha.
 * <p>
 * Os casos de uso precisam disso, mas o algoritmo (hoje BCrypt) é detalhe de
 * infraestrutura. Nos testes, esta interface é substituída por um mock.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
