package com.restaurant.management.infrastructure.web.restaurant;

import com.restaurant.management.core.application.restaurant.RestaurantCommand;
import com.restaurant.management.core.domain.restaurant.Restaurant;
import com.restaurant.management.infrastructure.web.shared.AddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo de criação e de atualização de um restaurante. */
public record RestaurantRequest(

        @Schema(description = "Nome do restaurante", example = "Cantina da Nonna")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = Restaurant.NAME_MAX_LENGTH, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @Valid
        @NotNull(message = "O endereço é obrigatório.")
        AddressDto address,

        @Schema(description = "Tipo de cozinha", example = "Italiana")
        @NotBlank(message = "O tipo de cozinha é obrigatório.")
        @Size(max = Restaurant.CUISINE_TYPE_MAX_LENGTH, message = "O tipo de cozinha deve ter no máximo 50 caracteres.")
        String cuisineType,

        @Valid
        @NotNull(message = "O horário de funcionamento é obrigatório.")
        OpeningHoursDto openingHours,

        @Schema(description = "Id do usuário dono do restaurante", example = "1")
        @NotNull(message = "O dono do restaurante é obrigatório.")
        Long ownerId
) {

    /** Converte em comando de aplicação; endereço e horário viram objetos de domínio aqui. */
    public RestaurantCommand toCommand() {
        return new RestaurantCommand(name, address.toDomain(), cuisineType, openingHours.toDomain(), ownerId);
    }
}
