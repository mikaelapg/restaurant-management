package com.restaurant.management.infrastructure.web.menuitem;

import com.restaurant.management.core.application.menuitem.MenuItemCommand;
import com.restaurant.management.core.domain.menuitem.MenuItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Corpo de criação e de atualização de um item do cardápio. */
public record MenuItemRequest(

        @Schema(description = "Nome do prato", example = "Risoto de cogumelos")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = MenuItem.NAME_MAX_LENGTH, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @Schema(description = "Descrição", example = "Arroz arbóreo, funghi secchi e parmesão")
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = MenuItem.DESCRIPTION_MAX_LENGTH, message = "A descrição deve ter no máximo 500 caracteres.")
        String description,

        @Schema(description = "Preço, com até duas casas decimais", example = "68.90")
        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter até 8 dígitos inteiros e 2 decimais.")
        BigDecimal price,

        @Schema(description = "Se verdadeiro, o item só pode ser pedido para consumo no restaurante", example = "false")
        @NotNull(message = "Informe se o item é apenas para consumo no local.")
        Boolean dineInOnly,

        @Schema(description = "Caminho onde a foto do prato está armazenada (opcional)",
                example = "/images/restaurants/1/risoto.jpg")
        @Size(max = MenuItem.PHOTO_PATH_MAX_LENGTH, message = "O caminho da foto deve ter no máximo 500 caracteres.")
        String photoPath
) {

    public MenuItemCommand toCommand() {
        return new MenuItemCommand(name, description, price, dineInOnly, photoPath);
    }
}
