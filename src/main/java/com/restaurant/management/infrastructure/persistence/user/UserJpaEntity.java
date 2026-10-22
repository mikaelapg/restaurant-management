package com.restaurant.management.infrastructure.persistence.user;

import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Representação da tabela {@code users} para o JPA: os campos comuns a qualquer usuário.
 * <p>
 * Herança {@code JOINED}: os dados próprios de cada perfil ficam em tabelas filhas
 * ({@code customers} e {@code restaurant_owners}), ligadas pelo id. A coluna
 * {@code user_type} é o discriminador e diz em qual tabela filha procurar.
 * <p>
 * A herança é detalhe de banco. No domínio, o {@code User} usa composição: os dados do
 * perfil ficam num {@code ProfileDetails}, e o {@link UserJpaMapper} faz a tradução.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String login;

    /** Hash BCrypt da senha. Nunca sai pela API. */
    @Column(nullable = false)
    private String password;

    /** Preenchida pelo Hibernate a cada gravação que altera o registro. */
    @UpdateTimestamp
    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt;

    @Embedded
    private UserAddressEmbeddable address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_type_id")
    private UserTypeJpaEntity userType;
}
