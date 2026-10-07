package com.restaurant.management.core.application.menuitem;

import com.restaurant.management.core.application.exception.MenuItemAlreadyExistsException;
import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.domain.menuitem.MenuItem;

/** Cadastra um item no cardápio de um restaurante existente, com nome único dentro dele. */
public class CreateMenuItemUseCase {

    private final MenuItemGateway gateway;
    private final RestaurantGateway restaurantGateway;

    public CreateMenuItemUseCase(MenuItemGateway gateway, RestaurantGateway restaurantGateway) {
        this.gateway = gateway;
        this.restaurantGateway = restaurantGateway;
    }

    public MenuItem execute(Long restaurantId, MenuItemCommand command) {
        if (!restaurantGateway.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }

        MenuItem menuItem = MenuItem.create(restaurantId, command.name(), command.description(),
                command.price(), command.dineInOnly(), command.photoPath());

        gateway.findByRestaurantIdAndNameIgnoreCase(restaurantId, menuItem.getName()).ifPresent(existing -> {
            throw new MenuItemAlreadyExistsException(menuItem.getName());
        });

        return gateway.save(menuItem);
    }
}
