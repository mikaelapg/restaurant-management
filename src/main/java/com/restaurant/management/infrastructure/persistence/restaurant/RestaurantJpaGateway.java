package com.restaurant.management.infrastructure.persistence.restaurant;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.domain.restaurant.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implementação do {@link RestaurantGateway} com JPA. */
@Component
public class RestaurantJpaGateway implements RestaurantGateway {

    /** Ordem estável: por nome e, entre homônimos, por id. */
    private static final Sort ORDER = Sort.by("name", "id");

    private final RestaurantJpaRepository repository;

    public RestaurantJpaGateway(RestaurantJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        return repository.save(RestaurantJpaEntity.fromDomain(restaurant)).toDomain();
    }

    @Override
    public Optional<Restaurant> findById(Long id) {
        return repository.findById(id).map(RestaurantJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByOwnerId(Long ownerId) {
        return repository.existsByOwnerId(ownerId);
    }

    @Override
    public PageResult<Restaurant> findAll(String name, String cuisineType, PageQuery pageQuery) {
        Page<RestaurantJpaEntity> page = repository.search(
                name == null ? "" : name,
                cuisineType == null ? "" : cuisineType,
                PageRequest.of(pageQuery.page(), pageQuery.size(), ORDER));
        return new PageResult<>(
                page.getContent().stream().map(RestaurantJpaEntity::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
