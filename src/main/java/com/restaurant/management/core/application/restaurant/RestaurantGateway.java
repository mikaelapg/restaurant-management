package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.restaurant.Restaurant;

import java.util.Optional;

/** O que os casos de uso precisam de um "lugar onde restaurantes ficam guardados". */
public interface RestaurantGateway {

    Restaurant save(Restaurant restaurant);

    Optional<Restaurant> findById(Long id);

    boolean existsById(Long id);

    /** Indica se o usuário é dono de algum restaurante. */
    boolean existsByOwnerId(Long ownerId);

    /**
     * Página de restaurantes em ordem alfabética de nome.
     *
     * @param name        trecho do nome; vazio ou {@code null} não filtra
     * @param cuisineType trecho do tipo de cozinha; vazio ou {@code null} não filtra
     */
    PageResult<Restaurant> findAll(String name, String cuisineType, PageQuery pageQuery);

    void deleteById(Long id);
}
