package com.restaurant.management.infrastructure.persistence.usertype;

import com.restaurant.management.core.application.usertype.UserTypeGateway;
import com.restaurant.management.core.domain.usertype.UserType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementação do {@link UserTypeGateway} com JPA.
 * <p>
 * É aqui que a seta de dependência "vira": a infraestrutura implementa uma interface
 * definida na aplicação. Os casos de uso só conhecem a interface; trocar o PostgreSQL
 * por outro banco mudaria esta classe e mais nada.
 */
@Component
public class UserTypeJpaGateway implements UserTypeGateway {

    private final UserTypeJpaRepository repository;

    public UserTypeJpaGateway(UserTypeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserType save(UserType userType) {
        return repository.save(UserTypeJpaEntity.fromDomain(userType)).toDomain();
    }

    @Override
    public Optional<UserType> findById(Long id) {
        return repository.findById(id).map(UserTypeJpaEntity::toDomain);
    }

    @Override
    public List<UserType> findAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(UserTypeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<UserType> findByNameIgnoreCase(String name) {
        return repository.findByNameIgnoreCase(name).map(UserTypeJpaEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
