package com.restaurant.management.core.application.menuitem;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.menuitem.MenuItem;

import java.util.Optional;

/** O que os casos de uso precisam de um "lugar onde itens do cardápio ficam guardados". */
public interface MenuItemGateway {

    MenuItem save(MenuItem menuItem);

    /** Busca o item só se ele pertencer ao restaurante informado. */
    Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);

    /** Itens do restaurante em ordem alfabética de nome. */
    PageResult<MenuItem> findAllByRestaurantId(Long restaurantId, PageQuery pageQuery);

    /** Busca, dentro do restaurante, um item com o nome informado, sem diferenciar maiúsculas. */
    Optional<MenuItem> findByRestaurantIdAndNameIgnoreCase(Long restaurantId, String name);

    void deleteById(Long id);
}
