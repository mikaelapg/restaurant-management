package com.restaurant.management.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Dono de restaurante — subtipo de {@link User}.
 * <p>
 * Mapeado na tabela {@code restaurant_owners} que referencia {@code users} via FK.
 */
@Entity
@Table(name = "restaurant_owners")
@DiscriminatorValue("RESTAURANT_OWNER")
@PrimaryKeyJoinColumn(name = "id")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RestaurantOwner extends User {

    /** Nome fantasia ou razão social do restaurante. */
    @Column(name = "restaurant_name")
    private String restaurantName;

    /** CNPJ do restaurante (formato: XX.XXX.XXX/XXXX-XX). */
    @Column(name = "cnpj", unique = true)
    private String cnpj;

    @Override
    public com.restaurant.management.domain.enums.UserType getUserType() {
        return com.restaurant.management.domain.enums.UserType.RESTAURANT_OWNER;
    }
}
