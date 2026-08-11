package com.restaurant.management.exception;

public class LoginAlreadyExistsException extends RuntimeException {

    public LoginAlreadyExistsException(String login) {
        super(String.format("Já existe um usuário cadastrado com o login: %s", login));
    }

    public LoginAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
