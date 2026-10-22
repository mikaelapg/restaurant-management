package com.restaurant.management.core.application.exception;

/**
 * Lançada ao tentar excluir um usuário que é dono de algum restaurante. Resulta em HTTP 409.
 * Os restaurantes precisam ser excluídos ou passados a outro dono antes.
 */
public class UserOwnsRestaurantsException extends RuntimeException {

    public UserOwnsRestaurantsException(Long userId) {
        super("O usuário " + userId + " é dono de restaurantes e não pode ser excluído. "
                + "Exclua os restaurantes ou transfira-os para outro dono antes.");
    }
}
