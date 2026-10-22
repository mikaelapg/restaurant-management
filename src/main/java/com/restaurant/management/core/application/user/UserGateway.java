package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;

import java.util.Optional;

/**
 * O que os casos de uso precisam de um "lugar onde usuários ficam guardados".
 * <p>
 * Os casos de uso não sabem que por trás há uma herança JPA com uma tabela por perfil;
 * para eles, um usuário é um {@link User} de domínio.
 */
public interface UserGateway {

    /** Salva o usuário e devolve a versão gravada, com id e data da última alteração. */
    User save(User user);

    Optional<User> findById(Long id);

    boolean existsById(Long id);

    /**
     * Página de usuários em ordem alfabética de nome.
     *
     * @param name    trecho do nome, sem diferenciar maiúsculas; vazio não filtra
     * @param profile perfil para filtrar, ou {@code null} para todos
     */
    PageResult<User> findAll(String name, UserProfile profile, PageQuery pageQuery);

    /** Busca pelo e-mail, sem diferenciar maiúsculas. */
    Optional<User> findByEmail(String email);

    /** Busca pelo login, sem diferenciar maiúsculas. */
    Optional<User> findByLogin(String login);

    /** Busca o cliente com o CPF informado. */
    Optional<User> findByCpf(String cpf);

    /** Busca o dono de restaurante com o CNPJ informado. */
    Optional<User> findByCnpj(String cnpj);

    /** Indica se algum usuário está associado ao tipo. */
    boolean existsByUserTypeId(Long userTypeId);

    void deleteById(Long id);
}
