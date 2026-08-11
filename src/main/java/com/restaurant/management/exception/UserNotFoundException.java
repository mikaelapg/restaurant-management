package com.restaurant.management.exception;

/**
 * Lançada quando um usuário não é encontrado pelo ID informado.
 * Resulta em resposta HTTP 404 Not Found com ProblemDetail.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuário não encontrado com o ID: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
