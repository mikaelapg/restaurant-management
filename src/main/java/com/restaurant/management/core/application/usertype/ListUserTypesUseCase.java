package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.domain.usertype.UserType;

import java.util.List;

/**
 * Lista todos os tipos de usuário.
 * <p>
 * Sem paginação de propósito: é uma tabela de referência com poucas linhas
 * ("Dono de Restaurante", "Cliente" e alguns outros, no máximo).
 */
public class ListUserTypesUseCase {

    private final UserTypeGateway gateway;

    public ListUserTypesUseCase(UserTypeGateway gateway) {
        this.gateway = gateway;
    }

    public List<UserType> execute() {
        return gateway.findAll();
    }
}
