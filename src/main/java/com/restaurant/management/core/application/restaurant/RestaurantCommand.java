package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.domain.restaurant.OpeningHours;
import com.restaurant.management.core.domain.shared.Address;

/**
 * Dados de entrada para cadastrar ou atualizar um restaurante.
 * <p>
 * O endereço e o horário já chegam como objetos de domínio, ou seja, já validados.
 */
public record RestaurantCommand(String name, Address address, String cuisineType,
                                OpeningHours openingHours, Long ownerId) {
}
