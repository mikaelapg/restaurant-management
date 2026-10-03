package com.restaurant.management.exception;

/**
 * Lançada quando um usuário não é encontrado pelo ID informado.
 * Resulta em resposta HTTP 404 Not Found com ProblemDetail.
 * <p>
 * Mantida para o código da Fase 1; herda da exceção da camada de aplicação para que
 * um único tratador responda às duas.
 */
public class UserNotFoundException extends com.restaurant.management.core.application.exception.UserNotFoundException {

    public UserNotFoundException(Long id) {
        super(id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
