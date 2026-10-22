package com.restaurant.management.core.application.exception;

/**
 * Lançada na troca de senha quando a senha atual está incorreta ou a confirmação não
 * confere com a nova. Resulta em HTTP 422.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
