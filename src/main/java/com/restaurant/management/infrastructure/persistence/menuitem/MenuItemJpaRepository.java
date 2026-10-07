package com.restaurant.management.infrastructure.persistence.menuitem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repositório Spring Data. Só é usado pelo {@link MenuItemJpaGateway}. */
public interface MenuItemJpaRepository extends JpaRepository<MenuItemJpaEntity, Long> {

    Optional<MenuItemJpaEntity> findByIdAndRestaurantId(Long id, Long restaurantId);

    Page<MenuItemJpaEntity> findByRestaurantId(Long restaurantId, Pageable pageable);

    Optional<MenuItemJpaEntity> findByRestaurantIdAndNameIgnoreCase(Long restaurantId, String name);
}
