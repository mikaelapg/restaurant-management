package com.restaurant.management.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Tabela {@code customers}: os dados próprios dos clientes. */
@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class CustomerJpaEntity extends UserJpaEntity {

    @Column(name = "cpf", unique = true)
    private String cpf;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
}
