package com.restaurant.management.exception;

/**
 * Lançada quando as credenciais informadas (login e/ou senha) são inválidas.
 * Resulta em resposta HTTP 401 Unauthorized com ProblemDetail.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Login ou senha inválidos. Por favor, verifique suas credenciais.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
