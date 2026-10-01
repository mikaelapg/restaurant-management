package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.domain.usertype.UserType;

import java.util.List;
import java.util.Optional;

/**
 * O que os casos de uso precisam de um "lugar onde tipos de usuário ficam guardados".
 * <p>
 * É uma interface de propósito: a camada de aplicação diz <em>o que</em> precisa e a
 * infraestrutura decide <em>como</em> (hoje JPA + PostgreSQL). Nos testes dos casos de
 * uso, ela é substituída por um mock, sem banco nenhum.
 */
public interface UserTypeGateway {

    UserType save(UserType userType);

    Optional<UserType> findById(Long id);

    /** Todos os tipos, em ordem alfabética de nome. */
    List<UserType> findAll();

    /** Busca ignorando maiúsculas e minúsculas: "cliente" e "Cliente" são o mesmo nome. */
    Optional<UserType> findByNameIgnoreCase(String name);

    void deleteById(Long id);
}
