package com.restaurant.management.exception;

/**
 * Lançada quando o e-mail informado já está cadastrado para outro usuário.
 * Resulta em resposta HTTP 409 Conflict com ProblemDetail.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Já existe um usuário cadastrado com o e-mail: " + email);
    }
}
