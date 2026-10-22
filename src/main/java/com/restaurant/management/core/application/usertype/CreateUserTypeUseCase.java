package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserTypeAlreadyExistsException;
import com.restaurant.management.core.domain.usertype.UserType;

/**
 * Cadastra um tipo de usuário com nome único.
 * <p>
 * Repare que não há {@code @Service} aqui: os casos de uso não dependem do Spring.
 * Quem cria e injeta esta classe é o {@code UserTypeConfig}, na infraestrutura.
 */
public class CreateUserTypeUseCase {

    private final UserTypeGateway gateway;

    public CreateUserTypeUseCase(UserTypeGateway gateway) {
        this.gateway = gateway;
    }

    public UserType execute(String name) {
        UserType userType = UserType.create(name);

        gateway.findByNameIgnoreCase(userType.getName()).ifPresent(existing -> {
            throw new UserTypeAlreadyExistsException(userType.getName());
        });

        return gateway.save(userType);
    }
}
