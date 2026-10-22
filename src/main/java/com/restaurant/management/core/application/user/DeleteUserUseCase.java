package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserOwnsRestaurantsException;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.domain.user.UserProfile;

/**
 * Exclui um usuário.
 * <p>
 * Quem é dono de restaurante não pode ser excluído: a FK do banco recusaria de qualquer
 * forma, e a checagem aqui troca o erro genérico de integridade por uma mensagem que diz
 * o que fazer.
 */
public class DeleteUserUseCase {

    private final UserGateway gateway;
    private final RestaurantGateway restaurantGateway;

    public DeleteUserUseCase(UserGateway gateway, RestaurantGateway restaurantGateway) {
        this.gateway = gateway;
        this.restaurantGateway = restaurantGateway;
    }

    /**
     * @param profile perfil exigido do usuário (o do endpoint chamado), ou {@code null} para qualquer um
     */
    public void execute(Long id, UserProfile profile) {
        gateway.findById(id)
                .filter(user -> profile == null || user.getProfile() == profile)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (restaurantGateway.existsByOwnerId(id)) {
            throw new UserOwnsRestaurantsException(id);
        }
        gateway.deleteById(id);
    }
}
