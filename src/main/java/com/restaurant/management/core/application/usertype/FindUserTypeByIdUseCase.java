package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.domain.usertype.UserType;

/** Busca um tipo de usuário pelo id. */
public class FindUserTypeByIdUseCase {

    private final UserTypeGateway gateway;

    public FindUserTypeByIdUseCase(UserTypeGateway gateway) {
        this.gateway = gateway;
    }

    public UserType execute(Long id) {
        return gateway.findById(id)
                .orElseThrow(() -> new UserTypeNotFoundException(id));
    }
}
