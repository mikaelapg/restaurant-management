package com.restaurant.management.core.application.exception;

/**
 * Lançada ao tirar o tipo de dono de restaurante de um usuário que ainda é responsável
 * por restaurantes. Resulta em HTTP 409.
 * <p>
 * Sem essa regra, o restaurante ficaria com um dono que não pode ser dono.
 */
public class UserTypeChangeNotAllowedException extends RuntimeException {

    public UserTypeChangeNotAllowedException(Long userId) {
        super("O usuário " + userId + " é dono de restaurantes e precisa continuar com o tipo de dono de "
                + "restaurante. Exclua os restaurantes ou transfira-os para outro dono antes.");
    }
}
