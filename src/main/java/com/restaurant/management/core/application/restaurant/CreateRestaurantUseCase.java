package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.restaurant.Restaurant;

/** Cadastra um restaurante, cujo dono precisa ser um usuário existente. */
public class CreateRestaurantUseCase {

    private final RestaurantGateway gateway;
    private final UserGateway userGateway;

    public CreateRestaurantUseCase(RestaurantGateway gateway, UserGateway userGateway) {
        this.gateway = gateway;
        this.userGateway = userGateway;
    }

    public Restaurant execute(RestaurantCommand command) {
        Restaurant restaurant = Restaurant.create(command.name(), command.address(), command.cuisineType(),
                command.openingHours(), command.ownerId());

        if (!userGateway.existsById(restaurant.getOwnerId())) {
            throw new UserNotFoundException(restaurant.getOwnerId());
        }
        return gateway.save(restaurant);
    }
}
