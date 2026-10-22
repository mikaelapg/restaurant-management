package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.SystemUserTypeException;
import com.restaurant.management.core.application.exception.UserTypeInUseException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.usertype.UserType;

/**
 * Remove um tipo de usuário, desde que não seja de sistema e que nenhum usuário esteja
 * associado a ele.
 * <p>
 * Os tipos de sistema ("Dono de Restaurante" e "Cliente") ficam: os cadastros novos
 * nascem com eles e a regra de quem pode ser dono de restaurante depende deles.
 * <p>
 * O banco também recusa excluir um tipo em uso ({@code ON DELETE RESTRICT}); a checagem
 * aqui existe para responder 409 com uma mensagem clara, em vez do erro genérico de
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
        UserType userType = gateway.findById(id)
                .orElseThrow(() -> new UserTypeNotFoundException(id));
        if (userType.isSystemType()) {
            throw new SystemUserTypeException(id, userType.getName());
        }
        if (userGateway.existsByUserTypeId(id)) {
            throw new UserTypeInUseException(id);
        }
        gateway.deleteById(id);
    }
}
