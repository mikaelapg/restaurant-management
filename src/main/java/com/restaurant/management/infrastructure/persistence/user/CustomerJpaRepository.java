package com.restaurant.management.infrastructure.persistence.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Consultas restritas aos clientes. Só é usado pelo {@link UserJpaGateway}. */
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {

    Optional<CustomerJpaEntity> findByCpf(String cpf);

    Page<CustomerJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
