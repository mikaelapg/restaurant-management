package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.domain.restaurant.Restaurant;

/** Busca um restaurante pelo id. */
public class FindRestaurantByIdUseCase {

    private final RestaurantGateway gateway;

    public FindRestaurantByIdUseCase(RestaurantGateway gateway) {
        this.gateway = gateway;
    }

    public Restaurant execute(Long id) {
        return gateway.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }
}
