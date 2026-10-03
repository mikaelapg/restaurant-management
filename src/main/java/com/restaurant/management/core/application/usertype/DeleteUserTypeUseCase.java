package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserTypeInUseException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.application.user.UserGateway;

/**
 * Remove um tipo de usuário, desde que nenhum usuário esteja associado a ele.
 * <p>
 * O banco também recusa a exclusão ({@code ON DELETE RESTRICT}); a checagem aqui
 * existe para responder 409 com uma mensagem clara, em vez do erro genérico de
 * integridade.
 */
public class DeleteUserTypeUseCase {

    private final UserTypeGateway gateway;
    private final UserGateway userGateway;

    public DeleteUserTypeUseCase(UserTypeGateway gateway, UserGateway userGateway) {
        this.gateway = gateway;
        this.userGateway = userGateway;
    }

    public void execute(Long id) {
        if (gateway.findById(id).isEmpty()) {
            throw new UserTypeNotFoundException(id);
        }
        if (userGateway.existsByUserTypeId(id)) {
            throw new UserTypeInUseException(id);
        }
        gateway.deleteById(id);
    }
}
