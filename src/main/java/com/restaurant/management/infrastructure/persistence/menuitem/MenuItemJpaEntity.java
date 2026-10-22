package com.restaurant.management.infrastructure.persistence.menuitem;

import com.restaurant.management.core.domain.menuitem.MenuItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Representação da tabela {@code menu_items} para o JPA. */
@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, length = MenuItem.NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false, length = MenuItem.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "dine_in_only", nullable = false)
    private boolean dineInOnly;

    @Column(name = "photo_path", length = MenuItem.PHOTO_PATH_MAX_LENGTH)
    private String photoPath;

    public static MenuItemJpaEntity fromDomain(MenuItem item) {
        return new MenuItemJpaEntity(item.getId(), item.getRestaurantId(), item.getName(), item.getDescription(),
                item.getPrice(), item.isDineInOnly(), item.getPhotoPath());
    }

    public MenuItem toDomain() {
        return MenuItem.restore(id, restaurantId, name, description, price, dineInOnly, photoPath);
    }
}
