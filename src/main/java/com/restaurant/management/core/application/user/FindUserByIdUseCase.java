package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;

/** Busca um usuário pelo id, de qualquer perfil ou de um perfil específico. */
public class FindUserByIdUseCase {

    private final UserGateway gateway;

    public FindUserByIdUseCase(UserGateway gateway) {
        this.gateway = gateway;
    }

    /** Busca o usuário, seja ele cliente ou dono de restaurante. */
    public User execute(Long id) {
        return execute(id, null);
    }

    /**
     * Busca o usuário só se ele for do perfil informado.
     *
     * @param profile perfil exigido, ou {@code null} para aceitar qualquer um
     */
    public User execute(Long id, UserProfile profile) {
        return gateway.findById(id)
                .filter(user -> profile == null || user.getProfile() == profile)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
