package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.exception.InvalidRestaurantOwnerException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.user.User;

/**
 * Confere se um usuário pode ser responsável por um restaurante: ele precisa existir
 * (404) e ter o tipo de usuário dos donos (422).
 * <p>
 * É o que distingue, na prática, um "Dono de Restaurante" de um "Cliente": um cliente
 * não pode ser cadastrado como dono de restaurante.
 */
public class RestaurantOwnerPolicy {

    private final UserGateway userGateway;

    public RestaurantOwnerPolicy(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public void ensureCanOwnRestaurants(Long ownerId) {
        User owner = userGateway.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));
        if (!owner.canOwnRestaurants()) {
            throw new InvalidRestaurantOwnerException(ownerId,
                    owner.getUserType() == null ? null : owner.getUserType().getName());
        }
    }
}
