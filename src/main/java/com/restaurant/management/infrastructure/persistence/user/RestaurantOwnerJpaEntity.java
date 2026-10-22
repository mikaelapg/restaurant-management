package com.restaurant.management.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tabela {@code restaurant_owners}: os dados próprios dos donos de restaurante. */
@Entity
@Table(name = "restaurant_owners")
@DiscriminatorValue("RESTAURANT_OWNER")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class RestaurantOwnerJpaEntity extends UserJpaEntity {

    @Column(name = "restaurant_name")
    private String restaurantName;

    @Column(name = "cnpj", unique = true)
    private String cnpj;
}
