package com.restaurant.management.infrastructure.web.menuitem;

import com.restaurant.management.core.domain.menuitem.MenuItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** O que a API devolve sobre um item do cardápio. */
public record MenuItemResponse(

        @Schema(description = "Identificador", example = "1")
        Long id,

        @Schema(description = "Restaurante ao qual o item pertence", example = "1")
        Long restaurantId,

        @Schema(description = "Nome do prato", example = "Risoto de cogumelos")
        String name,

        @Schema(description = "Descrição", example = "Arroz arbóreo, funghi secchi e parmesão")
        String description,

        @Schema(description = "Preço", example = "68.90")
        BigDecimal price,

        @Schema(description = "Apenas para consumo no restaurante", example = "false")
        boolean dineInOnly,

        @Schema(description = "Caminho da foto do prato", example = "/images/restaurants/1/risoto.jpg")
        String photoPath
) {

    public static MenuItemResponse fromDomain(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getRestaurantId(), item.getName(), item.getDescription(),
                item.getPrice(), item.isDineInOnly(), item.getPhotoPath());
    }
}
