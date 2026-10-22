package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.usertype.UserTypeGateway;
import com.restaurant.management.core.domain.user.PasswordPolicy;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.usertype.UserType;

/**
 * Cadastra um usuário, cliente ou dono de restaurante, conforme os dados do perfil.
 * <p>
 * O usuário já nasce associado ao tipo de sistema do seu perfil: "Cliente" para
 * clientes, "Dono de Restaurante" para donos. Assim um dono recém-cadastrado já pode
 * ser responsável por restaurantes.
 */
public class CreateUserUseCase {

    private final UserGateway gateway;
    private final UserTypeGateway userTypeGateway;
    private final PasswordHasher passwordHasher;
    private final UserUniquenessPolicy uniquenessPolicy;

    public CreateUserUseCase(UserGateway gateway, UserTypeGateway userTypeGateway, PasswordHasher passwordHasher) {
        this.gateway = gateway;
        this.userTypeGateway = userTypeGateway;
        this.passwordHasher = passwordHasher;
        this.uniquenessPolicy = new UserUniquenessPolicy(gateway);
    }

    public User execute(CreateUserCommand command) {
        PasswordPolicy.validate(command.password());
        UserType defaultType = command.details() == null
                ? null
                : userTypeGateway.findByProfile(command.details().profile()).orElse(null);

        User user = User.create(command.name(), command.email(), command.login(),
                passwordHasher.hash(command.password()), command.address(), command.details(), defaultType);
        uniquenessPolicy.ensureAvailable(user);
        return gateway.save(user);
    }
}
