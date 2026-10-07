package com.restaurant.management.core.application.menuitem;

import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.domain.menuitem.MenuItem;

/**
 * Lista o cardápio de um restaurante.
 * <p>
 * Restaurante inexistente é 404, e não uma lista vazia: lista vazia significaria
 * "o restaurante existe e ainda não tem itens".
 */
public class ListMenuItemsUseCase {

    private final MenuItemGateway gateway;
    private final RestaurantGateway restaurantGateway;

    public ListMenuItemsUseCase(MenuItemGateway gateway, RestaurantGateway restaurantGateway) {
        this.gateway = gateway;
        this.restaurantGateway = restaurantGateway;
    }

    public PageResult<MenuItem> execute(Long restaurantId, PageQuery pageQuery) {
        if (!restaurantGateway.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        return gateway.findAllByRestaurantId(restaurantId, pageQuery);
    }
}
