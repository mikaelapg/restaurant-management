package com.restaurant.management.infrastructure.persistence.menuitem;

import com.restaurant.management.core.application.menuitem.MenuItemGateway;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.menuitem.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Implementação do {@link MenuItemGateway} com JPA. */
@Component
public class MenuItemJpaGateway implements MenuItemGateway {

    private static final Sort ORDER = Sort.by("name", "id");

    private final MenuItemJpaRepository repository;

    public MenuItemJpaGateway(MenuItemJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        return repository.save(MenuItemJpaEntity.fromDomain(menuItem)).toDomain();
    }

    @Override
    public Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId) {
        return repository.findByIdAndRestaurantId(id, restaurantId).map(MenuItemJpaEntity::toDomain);
    }

    @Override
    public PageResult<MenuItem> findAllByRestaurantId(Long restaurantId, PageQuery pageQuery) {
        Page<MenuItemJpaEntity> page = repository.findByRestaurantId(restaurantId,
                PageRequest.of(pageQuery.page(), pageQuery.size(), ORDER));
        return new PageResult<>(
                page.getContent().stream().map(MenuItemJpaEntity::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public Optional<MenuItem> findByRestaurantIdAndNameIgnoreCase(Long restaurantId, String name) {
        return repository.findByRestaurantIdAndNameIgnoreCase(restaurantId, name).map(MenuItemJpaEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
