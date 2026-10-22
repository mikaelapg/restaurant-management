package com.restaurant.management.core.application.exception;

/**
 * Lançada quando o usuário indicado como dono de um restaurante não tem o tipo de
 * usuário dos donos ("Dono de Restaurante"). Resulta em HTTP 422.
 * <p>
 * O usuário existe, então não é 404; o pedido está bem formado, então não é 400. O que
 * falha é a regra de negócio: só dono de restaurante pode ser responsável por um.
 */
public class InvalidRestaurantOwnerException extends RuntimeException {

    public InvalidRestaurantOwnerException(Long userId, String currentTypeName) {
        super("O usuário " + userId + " não pode ser dono de restaurante: é preciso ter o tipo de usuário "
                + "de dono de restaurante, e o tipo atual dele é "
                + (currentTypeName == null ? "nenhum" : "'" + currentTypeName + "'") + ".");
    }
}
