package com.restaurant.management.infrastructure.persistence.user;

import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.core.domain.user.ProfileDetails;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaEntity;

/**
 * Traduz entre o {@link User} de domínio e a hierarquia JPA de usuários.
 * <p>
 * O domínio usa composição ({@link ProfileDetails}); o banco usa herança. A subclasse
 * JPA é escolhida pelo tipo dos dados do perfil, e vice-versa.
 */
final class UserJpaMapper {

    private UserJpaMapper() {
    }

    static User toDomain(UserJpaEntity entity) {
        ProfileDetails details = switch (entity) {
            case CustomerJpaEntity customer -> new CustomerDetails(customer.getCpf(), customer.getDateOfBirth());
            case RestaurantOwnerJpaEntity owner -> new RestaurantOwnerDetails(owner.getRestaurantName(), owner.getCnpj());
            default -> throw new IllegalStateException("Perfil de usuário desconhecido: " + entity.getClass());
        };
        return User.restore(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getLogin(),
                entity.getPassword(),
                entity.getAddress() == null ? null : entity.getAddress().toDomain(),
                details,
                entity.getUserType() == null ? null : entity.getUserType().toDomain(),
                entity.getLastModifiedAt());
    }

    /**
     * Monta a entidade JPA com os dados do domínio. O tipo de usuário vai como entidade
     * só com id e nome; o gateway troca por uma referência gerenciada antes de gravar.
     */
    static UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = switch (user.getDetails()) {
            case CustomerDetails customer -> {
                CustomerJpaEntity jpa = new CustomerJpaEntity();
                jpa.setCpf(customer.cpf());
                jpa.setDateOfBirth(customer.dateOfBirth());
                yield jpa;
            }
            case RestaurantOwnerDetails owner -> {
                RestaurantOwnerJpaEntity jpa = new RestaurantOwnerJpaEntity();
                jpa.setRestaurantName(owner.restaurantName());
                jpa.setCnpj(owner.cnpj());
                yield jpa;
            }
        };
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setLogin(user.getLogin());
        entity.setPassword(user.getPasswordHash());
        entity.setLastModifiedAt(user.getLastModifiedAt());
        entity.setAddress(UserAddressEmbeddable.fromDomain(user.getAddress()));
        entity.setUserType(user.getUserType() == null ? null : UserTypeJpaEntity.fromDomain(user.getUserType()));
        return entity;
    }
}
