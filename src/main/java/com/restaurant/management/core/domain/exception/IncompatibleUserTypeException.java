package com.restaurant.management.core.domain.exception;

/**
 * Lançada ao associar um usuário a um tipo de sistema de outro perfil, como dar o tipo
 * "Cliente" a quem se cadastrou como dono de restaurante. Resulta em HTTP 422.
 * <p>
 * Os dados chegaram certos (o usuário e o tipo existem), mas a combinação quebra uma
 * regra de negócio; por isso 422, e não 400.
 */
public class IncompatibleUserTypeException extends RuntimeException {

    public IncompatibleUserTypeException(String message) {
        super(message);
    }
}
