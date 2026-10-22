package com.restaurant.management.core.application.exception;

/**
 * Lançada quando o restaurante já tem um item com o mesmo nome, sem diferenciar
 * maiúsculas de minúsculas. Resulta em HTTP 409.
 */
public class MenuItemAlreadyExistsException extends RuntimeException {

    public MenuItemAlreadyExistsException(String name) {
        super("O cardápio deste restaurante já tem um item com o nome: " + name);
    }
}
