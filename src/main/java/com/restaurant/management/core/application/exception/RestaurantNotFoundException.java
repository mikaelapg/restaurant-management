package com.restaurant.management.core.application.exception;

/** Lançada quando nenhum restaurante tem o id informado. Resulta em HTTP 404. */
public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(Long id) {
        super("Restaurante não encontrado com o ID: " + id);
    }
}
