package com.restaurant.management.infrastructure.persistence.usertype;

import com.restaurant.management.core.domain.usertype.UserType;
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

/**
 * Representação da tabela {@code user_types} para o JPA.
 * <p>
 * Não é a entidade de domínio. O JPA exige construtor vazio e setters, coisas que o
 * {@link UserType} de domínio não tem de propósito. Separando as duas, cada uma segue
 * as regras de quem a usa, e os métodos {@link #fromDomain} e {@link #toDomain}
 * fazem a ponte.
 */
@Entity
@Table(name = "user_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTypeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = UserType.NAME_MAX_LENGTH)
    private String name;

    public static UserTypeJpaEntity fromDomain(UserType userType) {
        return new UserTypeJpaEntity(userType.getId(), userType.getName());
    }

    public UserType toDomain() {
        return UserType.restore(id, name);
    }
}
