package com.restaurant.management.core.application.menuitem;

import java.math.BigDecimal;

/** Dados de entrada para cadastrar ou atualizar um item do cardápio. */
public record MenuItemCommand(String name, String description, BigDecimal price,
                              boolean dineInOnly, String photoPath) {
}
