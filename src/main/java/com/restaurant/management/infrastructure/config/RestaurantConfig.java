package com.restaurant.management.infrastructure.config;

import com.restaurant.management.core.application.restaurant.CreateRestaurantUseCase;
import com.restaurant.management.core.application.restaurant.DeleteRestaurantUseCase;
import com.restaurant.management.core.application.restaurant.FindRestaurantByIdUseCase;
import com.restaurant.management.core.application.restaurant.ListRestaurantsUseCase;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.restaurant.UpdateRestaurantUseCase;
import com.restaurant.management.core.application.user.UserGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra os casos de uso de restaurante como beans do Spring. */
@Configuration
public class RestaurantConfig {

    @Bean
    public CreateRestaurantUseCase createRestaurantUseCase(RestaurantGateway gateway, UserGateway userGateway) {
        return new CreateRestaurantUseCase(gateway, userGateway);
    }

    @Bean
    public FindRestaurantByIdUseCase findRestaurantByIdUseCase(RestaurantGateway gateway) {
        return new FindRestaurantByIdUseCase(gateway);
    }

    @Bean
    public ListRestaurantsUseCase listRestaurantsUseCase(RestaurantGateway gateway) {
        return new ListRestaurantsUseCase(gateway);
    }

    @Bean
    public UpdateRestaurantUseCase updateRestaurantUseCase(RestaurantGateway gateway, UserGateway userGateway) {
        return new UpdateRestaurantUseCase(gateway, userGateway);
    }

    @Bean
    public DeleteRestaurantUseCase deleteRestaurantUseCase(RestaurantGateway gateway) {
        return new DeleteRestaurantUseCase(gateway);
    }
}
