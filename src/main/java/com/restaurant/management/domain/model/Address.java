package com.restaurant.management.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Endereço embutido nos usuários do sistema.
 * Mapeado com {@code @Embeddable} e armazenado nas colunas da tabela {@code users}.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /** Logradouro / Rua / Avenida. */
    @Column(name = "street")
    private String street;

    /** Número do imóvel. */
    @Column(name = "number")
    private String number;

    /** Complemento (apartamento, bloco etc.). */
    @Column(name = "complement")
    private String complement;

    /** Cidade. */
    @Column(name = "city")
    private String city;

    /** Estado (UF). */
    @Column(name = "state")
    private String state;

    /** Código de Endereçamento Postal (CEP). */
    @Column(name = "zip_code")
    private String zipCode;
}
