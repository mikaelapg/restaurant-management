package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.restaurant.Restaurant;

/** Atualiza todos os dados de um restaurante, inclusive o dono, que segue a mesma regra do cadastro. */
public class UpdateRestaurantUseCase {

    private final RestaurantGateway gateway;
    private final RestaurantOwnerPolicy ownerPolicy;

    public UpdateRestaurantUseCase(RestaurantGateway gateway, UserGateway userGateway) {
        this.gateway = gateway;
        this.ownerPolicy = new RestaurantOwnerPolicy(userGateway);
    }

    public Restaurant execute(Long id, RestaurantCommand command) {
        Restaurant current = gateway.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
        Restaurant updated = current.update(command.name(), command.address(), command.cuisineType(),
                command.openingHours(), command.ownerId());
        ownerPolicy.ensureCanOwnRestaurants(updated.getOwnerId());
        return gateway.save(updated);
    }
}
