package com.restaurant.management.core.application.exception;

/**
 * Lançada quando o login não existe ou a senha não confere. Resulta em HTTP 401.
 * <p>
 * A mensagem é a mesma nos dois casos de propósito: dizer qual dos dois falhou
 * revelaria quais logins existem.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Login ou senha inválidos. Por favor, verifique suas credenciais.");
    }
}
