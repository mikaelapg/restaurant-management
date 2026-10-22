package com.restaurant.management.infrastructure.persistence.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositório Spring Data. Só é usado pelo {@link RestaurantJpaGateway}. */
public interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity, Long> {

    boolean existsByOwnerId(Long ownerId);

    /**
     * Busca por trechos de nome e de tipo de cozinha, sem diferenciar maiúsculas.
     * Um parâmetro vazio casa com tudo, o que dispensa uma consulta por combinação de filtros.
     */
    @Query("""
            SELECT r FROM RestaurantJpaEntity r
            WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))
              AND LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :cuisineType, '%'))
            """)
    Page<RestaurantJpaEntity> search(@Param("name") String name,
                                     @Param("cuisineType") String cuisineType,
                                     Pageable pageable);
}
