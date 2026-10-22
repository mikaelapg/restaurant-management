package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.restaurant.Restaurant;

/**
 * Lista restaurantes, paginados e com filtros opcionais por nome e tipo de cozinha.
 * <p>
 * O filtro por tipo de cozinha atende ao objetivo do sistema: o cliente escolhe o
 * restaurante pela comida oferecida.
 */
public class ListRestaurantsUseCase {

    private final RestaurantGateway gateway;

    public ListRestaurantsUseCase(RestaurantGateway gateway) {
        this.gateway = gateway;
    }

    public PageResult<Restaurant> execute(String name, String cuisineType, PageQuery pageQuery) {
        return gateway.findAll(trimToEmpty(name), trimToEmpty(cuisineType), pageQuery);
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
