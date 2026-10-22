package com.restaurant.management.core.application.menuitem;

import com.restaurant.management.core.application.exception.MenuItemNotFoundException;

/** Remove um item do cardápio de um restaurante. */
public class DeleteMenuItemUseCase {

    private final MenuItemGateway gateway;

    public DeleteMenuItemUseCase(MenuItemGateway gateway) {
        this.gateway = gateway;
    }

    public void execute(Long restaurantId, Long itemId) {
        if (gateway.findByIdAndRestaurantId(itemId, restaurantId).isEmpty()) {
            throw new MenuItemNotFoundException(restaurantId, itemId);
        }
        gateway.deleteById(itemId);
    }
}
