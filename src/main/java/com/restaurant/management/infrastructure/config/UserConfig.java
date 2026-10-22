package com.restaurant.management.infrastructure.config;

import com.restaurant.management.core.application.auth.AuthenticateUserUseCase;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.user.ChangePasswordUseCase;
import com.restaurant.management.core.application.user.CreateUserUseCase;
import com.restaurant.management.core.application.user.DeleteUserUseCase;
import com.restaurant.management.core.application.user.FindUserByIdUseCase;
import com.restaurant.management.core.application.user.ListUsersUseCase;
import com.restaurant.management.core.application.user.PasswordHasher;
import com.restaurant.management.core.application.user.UpdateUserUseCase;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.application.usertype.UserTypeGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os casos de uso de usuários (clientes e donos) e de autenticação como beans do Spring.
 * <p>
 * Os mesmos casos de uso atendem os dois perfis: o perfil vem dos dados do comando ou é
 * passado pelo controller, conforme o endpoint chamado.
 */
@Configuration
public class UserConfig {

    @Bean
    public CreateUserUseCase createUserUseCase(UserGateway gateway, UserTypeGateway userTypeGateway,
                                               PasswordHasher passwordHasher) {
        return new CreateUserUseCase(gateway, userTypeGateway, passwordHasher);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserGateway gateway) {
        return new UpdateUserUseCase(gateway);
    }

    @Bean
    public FindUserByIdUseCase findUserByIdUseCase(UserGateway gateway) {
        return new FindUserByIdUseCase(gateway);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserGateway gateway) {
        return new ListUsersUseCase(gateway);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(UserGateway gateway, PasswordHasher passwordHasher) {
        return new ChangePasswordUseCase(gateway, passwordHasher);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserGateway gateway, RestaurantGateway restaurantGateway) {
        return new DeleteUserUseCase(gateway, restaurantGateway);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(UserGateway gateway, PasswordHasher passwordHasher) {
        return new AuthenticateUserUseCase(gateway, passwordHasher);
    }
}
