package com.restaurant.management.infrastructure.persistence.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Consultas restritas aos donos de restaurante. Só é usado pelo {@link UserJpaGateway}. */
public interface RestaurantOwnerJpaRepository extends JpaRepository<RestaurantOwnerJpaEntity, Long> {

    Optional<RestaurantOwnerJpaEntity> findByCnpj(String cnpj);

    Page<RestaurantOwnerJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
