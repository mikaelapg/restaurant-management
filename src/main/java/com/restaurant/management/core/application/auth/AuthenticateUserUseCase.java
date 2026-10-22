package com.restaurant.management.core.application.auth;

import com.restaurant.management.core.application.exception.InvalidCredentialsException;
import com.restaurant.management.core.application.user.PasswordHasher;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.user.User;

/**
 * Valida login e senha e devolve o usuário autenticado.
 * <p>
 * Não emite token: o enunciado pede só a validação das credenciais. Login inexistente
 * e senha errada dão o mesmo erro, para não revelar quais logins existem.
 */
public class AuthenticateUserUseCase {

    private final UserGateway gateway;
    private final PasswordHasher passwordHasher;

    public AuthenticateUserUseCase(UserGateway gateway, PasswordHasher passwordHasher) {
        this.gateway = gateway;
        this.passwordHasher = passwordHasher;
    }

    public User execute(String login, String password) {
        if (login == null || login.isBlank() || password == null) {
            throw new InvalidCredentialsException();
        }
        return gateway.findByLogin(login.trim())
                .filter(user -> passwordHasher.matches(password, user.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
    }
}
