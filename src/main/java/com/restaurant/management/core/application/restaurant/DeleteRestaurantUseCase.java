package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.exception.RestaurantNotFoundException;

/**
 * Remove um restaurante. Os itens do cardápio dele saem junto, pela FK com
 * {@code ON DELETE CASCADE}: um item não faz sentido sem o restaurante.
 */
public class DeleteRestaurantUseCase {

    private final RestaurantGateway gateway;

    public DeleteRestaurantUseCase(RestaurantGateway gateway) {
        this.gateway = gateway;
    }

    public void execute(Long id) {
        if (!gateway.existsById(id)) {
            throw new RestaurantNotFoundException(id);
        }
        gateway.deleteById(id);
    }
}
