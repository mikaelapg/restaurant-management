package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.domain.user.User;

/**
 * Atualiza os dados cadastrais de um usuário do perfil indicado pelos dados do perfil.
 * <p>
 * Um id que existe, mas é de outro perfil, responde como não encontrado: em
 * {@code PUT /customers/{id}}, um dono de restaurante não é um cliente.
 */
public class UpdateUserUseCase {

    private final UserGateway gateway;
    private final UserUniquenessPolicy uniquenessPolicy;

    public UpdateUserUseCase(UserGateway gateway) {
        this.gateway = gateway;
        this.uniquenessPolicy = new UserUniquenessPolicy(gateway);
    }

    public User execute(Long id, UpdateUserCommand command) {
        User current = gateway.findById(id)
                .filter(user -> command.details() == null || user.getProfile() == command.details().profile())
                .orElseThrow(() -> new UserNotFoundException(id));

        User updated = current.updateData(command.name(), command.email(), command.login(),
                command.address() != null ? command.address() : current.getAddress(),
                command.details());
        uniquenessPolicy.ensureAvailable(updated);
        return gateway.save(updated);
    }
}
