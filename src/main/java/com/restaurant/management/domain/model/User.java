package com.restaurant.management.domain.model;

import com.restaurant.management.domain.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade base de usuário.
 * <p>
 * Utiliza herança JOINED: esta tabela ({@code users}) armazena campos comuns,
 * enquanto {@link RestaurantOwner} e {@link Customer} possuem tabelas próprias
 * com suas colunas específicas.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome completo do usuário. */
    @Column(nullable = false)
    private String name;

    /**
     * E-mail único do usuário.
     * A unicidade é garantida tanto no banco (UNIQUE INDEX) quanto na camada de serviço.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /** Login de acesso ao sistema. */
    @Column(nullable = false, unique = true)
    private String login;

    /**
     * Senha armazenada como hash BCrypt.
     * Nunca deve ser exposta em respostas da API.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Data e hora da última alteração do registro.
     * Atualizada automaticamente pelo Hibernate via {@code @UpdateTimestamp}.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

    /**
     * Endereço embutido.
     * Os campos são mapeados como colunas da tabela {@code users}.
     */
    @Embedded
    private Address address;

    /**
     * Tipo de usuário — reflete o discriminador da herança.
     * Inserido uma única vez na criação; não pode ser alterado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", insertable = false, updatable = false)
    private UserType userType;
}
