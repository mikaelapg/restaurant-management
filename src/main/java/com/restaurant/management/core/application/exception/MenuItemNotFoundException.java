package com.restaurant.management.core.application.exception;

/**
 * Lançada quando o item não existe no cardápio do restaurante informado. Resulta em HTTP 404.
 * <p>
 * Um item que existe, mas em outro restaurante, também cai aqui: pelo caminho
 * {@code /restaurants/{id}/menu-items/{itemId}}, ele não existe.
 */
public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(Long restaurantId, Long itemId) {
        super("Item " + itemId + " não encontrado no cardápio do restaurante " + restaurantId + ".");
    }
}
