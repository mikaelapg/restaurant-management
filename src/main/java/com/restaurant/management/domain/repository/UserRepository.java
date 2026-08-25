package com.restaurant.management.domain.repository;

import com.restaurant.management.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório polimórfico sobre a raiz da hierarquia.
 * Como a herança é JOINED, as consultas aqui alcançam clientes e donos de restaurante.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /**
     * Busca pelo login ignorando maiúsculas e minúsculas.
     * O login é tratado como identificador único independente da caixa, do mesmo modo que o e-mail.
     */
    Optional<User> findByLoginIgnoreCase(String login);

    /** Busca por trecho do nome, ignorando maiúsculas e minúsculas. */
    List<User> findByNameContainingIgnoreCase(String name);
}
