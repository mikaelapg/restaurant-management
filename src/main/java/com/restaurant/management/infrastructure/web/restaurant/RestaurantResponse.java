package com.restaurant.management.infrastructure.web.restaurant;

import com.restaurant.management.core.domain.restaurant.Restaurant;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;

/** O que a API devolve sobre um restaurante. */
public record RestaurantResponse(

        @Schema(description = "Identificador", example = "1")
        Long id,

        @Schema(description = "Nome", example = "Cantina da Nonna")
        String name,

        AddressDto address,

        @Schema(description = "Tipo de cozinha", example = "Italiana")
        String cuisineType,

        OpeningHoursDto openingHours,

        @Schema(description = "Id do usuário dono do restaurante", example = "1")
        Long ownerId
) {

    public static RestaurantResponse fromDomain(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                AddressDto.fromDomain(restaurant.getAddress()),
                restaurant.getCuisineType(),
                OpeningHoursDto.fromDomain(restaurant.getOpeningHours()),
                restaurant.getOwnerId());
    }
}
