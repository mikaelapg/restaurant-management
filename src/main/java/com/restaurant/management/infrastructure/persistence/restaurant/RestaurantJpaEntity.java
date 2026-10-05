package com.restaurant.management.infrastructure.persistence.restaurant;

import com.restaurant.management.core.domain.restaurant.OpeningHours;
import com.restaurant.management.core.domain.restaurant.Restaurant;
import com.restaurant.management.infrastructure.persistence.shared.AddressEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Representação da tabela {@code restaurants} para o JPA.
 * <p>
 * O dono é mapeado como id ({@code owner_id}), e não como relacionamento com a entidade
 * de usuário: o restaurante só precisa da referência, e a FK no banco garante que ela
 * aponta para um usuário que existe.
 */
@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Restaurant.NAME_MAX_LENGTH)
    private String name;

    @Embedded
    private AddressEmbeddable address;

    @Column(name = "cuisine_type", nullable = false, length = Restaurant.CUISINE_TYPE_MAX_LENGTH)
    private String cuisineType;

    @Column(name = "opens_at", nullable = false)
    private LocalTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private LocalTime closesAt;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    public static RestaurantJpaEntity fromDomain(Restaurant restaurant) {
        return new RestaurantJpaEntity(
                restaurant.getId(),
                restaurant.getName(),
                AddressEmbeddable.fromDomain(restaurant.getAddress()),
                restaurant.getCuisineType(),
                restaurant.getOpeningHours().opensAt(),
                restaurant.getOpeningHours().closesAt(),
                restaurant.getOwnerId());
    }

    public Restaurant toDomain() {
        return Restaurant.restore(id, name, address.toDomain(), cuisineType,
                new OpeningHours(opensAt, closesAt), ownerId);
    }
}
