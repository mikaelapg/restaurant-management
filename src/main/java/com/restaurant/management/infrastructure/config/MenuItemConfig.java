package com.restaurant.management.infrastructure.config;

import com.restaurant.management.core.application.menuitem.CreateMenuItemUseCase;
import com.restaurant.management.core.application.menuitem.DeleteMenuItemUseCase;
import com.restaurant.management.core.application.menuitem.FindMenuItemByIdUseCase;
import com.restaurant.management.core.application.menuitem.ListMenuItemsUseCase;
import com.restaurant.management.core.application.menuitem.MenuItemGateway;
import com.restaurant.management.core.application.menuitem.UpdateMenuItemUseCase;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra os casos de uso do cardápio como beans do Spring. */
@Configuration
public class MenuItemConfig {

    @Bean
    public CreateMenuItemUseCase createMenuItemUseCase(MenuItemGateway gateway, RestaurantGateway restaurantGateway) {
        return new CreateMenuItemUseCase(gateway, restaurantGateway);
    }

    @Bean
    public FindMenuItemByIdUseCase findMenuItemByIdUseCase(MenuItemGateway gateway) {
        return new FindMenuItemByIdUseCase(gateway);
    }

    @Bean
    public ListMenuItemsUseCase listMenuItemsUseCase(MenuItemGateway gateway, RestaurantGateway restaurantGateway) {
        return new ListMenuItemsUseCase(gateway, restaurantGateway);
    }

    @Bean
    public UpdateMenuItemUseCase updateMenuItemUseCase(MenuItemGateway gateway) {
        return new UpdateMenuItemUseCase(gateway);
    }

    @Bean
    public DeleteMenuItemUseCase deleteMenuItemUseCase(MenuItemGateway gateway) {
        return new DeleteMenuItemUseCase(gateway);
    }
}
