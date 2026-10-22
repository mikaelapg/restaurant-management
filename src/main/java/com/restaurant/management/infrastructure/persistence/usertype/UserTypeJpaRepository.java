package com.restaurant.management.infrastructure.persistence.usertype;

import com.restaurant.management.core.domain.user.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repositório Spring Data. Só é usado pelo {@link UserTypeJpaGateway}, nunca pelos casos de uso. */
public interface UserTypeJpaRepository extends JpaRepository<UserTypeJpaEntity, Long> {

    Optional<UserTypeJpaEntity> findByNameIgnoreCase(String name);

    Optional<UserTypeJpaEntity> findByProfile(UserProfile profile);

    List<UserTypeJpaEntity> findAllByOrderByNameAsc();
}
