package com.restaurant.management.core.application.exception;

/** Lançada quando o login já pertence a outro usuário, sem diferenciar maiúsculas. Resulta em HTTP 409. */
public class LoginAlreadyExistsException extends RuntimeException {

    public LoginAlreadyExistsException(String login) {
        super("Já existe um usuário cadastrado com o login: " + login);
    }
}
