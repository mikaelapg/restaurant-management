package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;

/** Lista usuários, paginados, com filtro opcional por trecho do nome e por perfil. */
public class ListUsersUseCase {

    private final UserGateway gateway;

    public ListUsersUseCase(UserGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * @param name    trecho do nome; {@code null} ou vazio não filtra
     * @param profile perfil; {@code null} lista clientes e donos
     */
    public PageResult<User> execute(String name, UserProfile profile, PageQuery pageQuery) {
        return gateway.findAll(name == null ? "" : name.trim(), profile, pageQuery);
    }
}
