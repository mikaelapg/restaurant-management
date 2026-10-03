package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.application.user.UserGateway;

/**
 * Associa um usuário existente a um tipo de usuário existente.
 * <p>
 * Um usuário tem um único tipo; associar de novo troca o tipo anterior.
 */
public class AssignUserTypeUseCase {

    private final UserGateway userGateway;
    private final UserTypeGateway userTypeGateway;

    public AssignUserTypeUseCase(UserGateway userGateway, UserTypeGateway userTypeGateway) {
        this.userGateway = userGateway;
        this.userTypeGateway = userTypeGateway;
    }

    public void execute(Long userId, Long userTypeId) {
        if (!userGateway.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        if (userTypeGateway.findById(userTypeId).isEmpty()) {
            throw new UserTypeNotFoundException(userTypeId);
        }
        userGateway.assignUserType(userId, userTypeId);
    }
}
