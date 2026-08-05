package com.restaurant.management.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Cliente do sistema — subtipo de {@link User}.
 * <p>
 * Mapeado na tabela {@code customers} que referencia {@code users} via FK.
 */
@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
@PrimaryKeyJoinColumn(name = "id")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Customer extends User {

    /** CPF do cliente (formato: XXX.XXX.XXX-XX). */
    @Column(name = "cpf", unique = true)
    private String cpf;

    /** Data de nascimento. */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Override
    public com.restaurant.management.domain.enums.UserType getUserType() {
        return com.restaurant.management.domain.enums.UserType.CUSTOMER;
    }
}
