package com.restaurant.management.core.application.menuitem;

import com.restaurant.management.core.application.exception.MenuItemAlreadyExistsException;
import com.restaurant.management.core.application.exception.MenuItemNotFoundException;
import com.restaurant.management.core.domain.menuitem.MenuItem;

/**
 * Atualiza um item do cardápio. O nome novo só conflita se pertencer a <em>outro</em>
 * item do mesmo restaurante.
 */
public class UpdateMenuItemUseCase {

    private final MenuItemGateway gateway;

    public UpdateMenuItemUseCase(MenuItemGateway gateway) {
        this.gateway = gateway;
    }

    public MenuItem execute(Long restaurantId, Long itemId, MenuItemCommand command) {
        MenuItem current = gateway.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(restaurantId, itemId));

        MenuItem updated = current.update(command.name(), command.description(), command.price(),
                command.dineInOnly(), command.photoPath());

        gateway.findByRestaurantIdAndNameIgnoreCase(restaurantId, updated.getName())
                .filter(other -> !other.getId().equals(itemId))
                .ifPresent(other -> {
                    throw new MenuItemAlreadyExistsException(updated.getName());
                });

        return gateway.save(updated);
    }
}
