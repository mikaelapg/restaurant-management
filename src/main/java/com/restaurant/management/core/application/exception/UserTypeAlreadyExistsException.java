package com.restaurant.management.core.application.exception;

/**
 * Lançada quando já existe um tipo de usuário com o mesmo nome, sem diferenciar
 * maiúsculas de minúsculas. Resulta em HTTP 409.
 */
public class UserTypeAlreadyExistsException extends RuntimeException {

    public UserTypeAlreadyExistsException(String name) {
        super("Já existe um tipo de usuário com o nome: " + name);
    }
}
