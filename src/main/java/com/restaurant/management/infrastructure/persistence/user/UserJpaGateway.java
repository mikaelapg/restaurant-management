package com.restaurant.management.infrastructure.persistence.user;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do {@link UserGateway} sobre o repositório de usuários da Fase 1.
 * <p>
 * É a ponte entre os casos de uso novos e o cadastro de usuários existente: os casos de
 * uso não sabem que por trás há uma herança JPA com duas tabelas filhas.
 */
@Component
public class UserJpaGateway implements UserGateway {

    private final UserRepository userRepository;
    private final UserTypeJpaRepository userTypeRepository;

    public UserJpaGateway(UserRepository userRepository, UserTypeJpaRepository userTypeRepository) {
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional
    public void assignUserType(Long userId, Long userTypeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setAssignedUserType(userTypeRepository.getReferenceById(userTypeId));
        userRepository.saveAndFlush(user);
    }

    @Override
    public boolean existsByUserTypeId(Long userTypeId) {
        return userRepository.existsByAssignedUserTypeId(userTypeId);
    }
}
