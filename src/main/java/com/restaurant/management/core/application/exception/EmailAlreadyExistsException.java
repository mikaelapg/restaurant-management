package com.restaurant.management.core.application.exception;

/** Lançada quando o e-mail já pertence a outro usuário, sem diferenciar maiúsculas. Resulta em HTTP 409. */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Já existe um usuário cadastrado com o e-mail: " + email);
    }
}
