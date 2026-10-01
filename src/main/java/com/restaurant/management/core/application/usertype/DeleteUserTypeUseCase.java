package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserTypeNotFoundException;

/**
 * Remove um tipo de usuário.
 * <p>
 * Ainda falta uma regra aqui, que entra no passo 3, quando o tipo for associado aos
 * usuários: impedir a exclusão de um tipo que algum usuário ainda usa.
 */
public class DeleteUserTypeUseCase {

    private final UserTypeGateway gateway;

    public DeleteUserTypeUseCase(UserTypeGateway gateway) {
        this.gateway = gateway;
    }

    public void execute(Long id) {
        if (gateway.findById(id).isEmpty()) {
            throw new UserTypeNotFoundException(id);
        }
        gateway.deleteById(id);
    }
}
