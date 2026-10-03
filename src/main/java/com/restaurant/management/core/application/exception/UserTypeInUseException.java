package com.restaurant.management.core.application.exception;

/**
 * Lançada ao tentar excluir um tipo de usuário que ainda está associado a algum
 * usuário. Resulta em HTTP 409.
 */
public class UserTypeInUseException extends RuntimeException {

    public UserTypeInUseException(Long id) {
        super("O tipo de usuário " + id + " está associado a usuários e não pode ser excluído.");
    }
}
