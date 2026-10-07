package com.restaurant.management.core.application.menuitem;

import com.restaurant.management.core.application.exception.MenuItemNotFoundException;
import com.restaurant.management.core.domain.menuitem.MenuItem;

/** Busca um item do cardápio de um restaurante. */
public class FindMenuItemByIdUseCase {

    private final MenuItemGateway gateway;

    public FindMenuItemByIdUseCase(MenuItemGateway gateway) {
        this.gateway = gateway;
    }

    public MenuItem execute(Long restaurantId, Long itemId) {
        return gateway.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new MenuItemNotFoundException(restaurantId, itemId));
    }
}
