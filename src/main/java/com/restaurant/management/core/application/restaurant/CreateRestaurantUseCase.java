package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.restaurant.Restaurant;

/** Cadastra um restaurante, cujo dono precisa ser um usuário existente do tipo dono de restaurante. */
public class CreateRestaurantUseCase {

    private final RestaurantGateway gateway;
    private final RestaurantOwnerPolicy ownerPolicy;

    public CreateRestaurantUseCase(RestaurantGateway gateway, UserGateway userGateway) {
        this.gateway = gateway;
        this.ownerPolicy = new RestaurantOwnerPolicy(userGateway);
    }

    public Restaurant execute(RestaurantCommand command) {
        Restaurant restaurant = Restaurant.create(command.name(), command.address(), command.cuisineType(),
                command.openingHours(), command.ownerId());
        ownerPolicy.ensureCanOwnRestaurants(restaurant.getOwnerId());
        return gateway.save(restaurant);
    }
}
