package com.restaurant.management.core.application.exception;

/** Lançada quando nenhum usuário tem o id informado. Resulta em HTTP 404. */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuário não encontrado com o ID: " + id);
    }
}
