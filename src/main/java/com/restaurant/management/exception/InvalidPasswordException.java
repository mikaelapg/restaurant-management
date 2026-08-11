package com.restaurant.management.exception;

/**
 * Lançada quando a nova senha não é válida — ex: não corresponde à confirmação,
 * ou a senha atual informada está incorreta.
 * Resulta em resposta HTTP 422 Unprocessable Entity com ProblemDetail.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
