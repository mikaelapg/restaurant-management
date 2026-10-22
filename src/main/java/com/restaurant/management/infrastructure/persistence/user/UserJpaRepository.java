package com.restaurant.management.infrastructure.persistence.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório Spring Data sobre a raiz da hierarquia. Como a herança é JOINED, as
 * consultas aqui alcançam clientes e donos. Só é usado pelo {@link UserJpaGateway}.
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

    Optional<UserJpaEntity> findByLoginIgnoreCase(String login);

    Page<UserJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByUserTypeId(Long userTypeId);
}
