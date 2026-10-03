package com.restaurant.management.service;

import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaEntity;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;

/** Converte o tipo de usuário associado a um usuário no DTO de resposta. */
final class UserTypeMapper {

    private UserTypeMapper() {
    }

    /** Usuário sem tipo associado devolve {@code null}. */
    static UserTypeResponse toResponse(UserTypeJpaEntity userType) {
        return userType == null ? null : new UserTypeResponse(userType.getId(), userType.getName());
    }
}
