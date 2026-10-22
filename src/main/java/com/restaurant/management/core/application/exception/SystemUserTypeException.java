package com.restaurant.management.core.application.exception;

/**
 * Lançada ao tentar excluir um tipo de sistema ("Dono de Restaurante" ou "Cliente").
 * Resulta em HTTP 409.
 * <p>
 * Os cadastros novos recebem esses tipos e a regra de quem pode ser dono de restaurante
 * depende deles. Renomear é permitido; excluir, não.
 */
public class SystemUserTypeException extends RuntimeException {

    public SystemUserTypeException(Long id, String name) {
        super("O tipo de usuário " + id + " ('" + name + "') é um tipo de sistema e não pode ser excluído.");
    }
}
