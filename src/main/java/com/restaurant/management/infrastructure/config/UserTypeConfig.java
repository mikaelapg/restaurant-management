package com.restaurant.management.infrastructure.config;

import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.application.usertype.AssignUserTypeUseCase;
import com.restaurant.management.core.application.usertype.CreateUserTypeUseCase;
import com.restaurant.management.core.application.usertype.DeleteUserTypeUseCase;
import com.restaurant.management.core.application.usertype.FindUserTypeByIdUseCase;
import com.restaurant.management.core.application.usertype.ListUserTypesUseCase;
import com.restaurant.management.core.application.usertype.UpdateUserTypeUseCase;
import com.restaurant.management.core.application.usertype.UserTypeGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os casos de uso de tipo de usuário como beans do Spring.
 * <p>
 * Como os casos de uso não têm {@code @Service} (para a camada de aplicação não
 * depender do Spring), alguém precisa criá-los. Este é o único lugar que conhece as
 * duas pontas: pede ao Spring a implementação do gateway e a entrega a cada caso de uso.
 */
@Configuration
public class UserTypeConfig {

    @Bean
    public CreateUserTypeUseCase createUserTypeUseCase(UserTypeGateway gateway) {
        return new CreateUserTypeUseCase(gateway);
    }

    @Bean
    public FindUserTypeByIdUseCase findUserTypeByIdUseCase(UserTypeGateway gateway) {
        return new FindUserTypeByIdUseCase(gateway);
    }

    @Bean
    public ListUserTypesUseCase listUserTypesUseCase(UserTypeGateway gateway) {
        return new ListUserTypesUseCase(gateway);
    }

    @Bean
    public UpdateUserTypeUseCase updateUserTypeUseCase(UserTypeGateway gateway) {
        return new UpdateUserTypeUseCase(gateway);
    }

    @Bean
    public DeleteUserTypeUseCase deleteUserTypeUseCase(UserTypeGateway gateway, UserGateway userGateway) {
        return new DeleteUserTypeUseCase(gateway, userGateway);
    }

    @Bean
    public AssignUserTypeUseCase assignUserTypeUseCase(UserGateway userGateway, UserTypeGateway userTypeGateway,
                                                       RestaurantGateway restaurantGateway) {
        return new AssignUserTypeUseCase(userGateway, userTypeGateway, restaurantGateway);
    }
}
