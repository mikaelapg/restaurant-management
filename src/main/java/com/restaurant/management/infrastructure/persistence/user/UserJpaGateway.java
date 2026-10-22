package com.restaurant.management.infrastructure.persistence.user;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementação do {@link UserGateway} com JPA.
 * <p>
 * Os casos de uso não sabem que por trás há uma herança com uma tabela por perfil: as
 * consultas por perfil vão ao repositório da subclasse, e as demais ao da raiz.
 */
@Component
public class UserJpaGateway implements UserGateway {

    /** Ordem estável: por nome e, entre homônimos, por id. */
    private static final Sort ORDER = Sort.by("name", "id");

    private final UserJpaRepository userRepository;
    private final CustomerJpaRepository customerRepository;
    private final RestaurantOwnerJpaRepository ownerRepository;
    private final UserTypeJpaRepository userTypeRepository;

    public UserJpaGateway(UserJpaRepository userRepository,
                          CustomerJpaRepository customerRepository,
                          RestaurantOwnerJpaRepository ownerRepository,
                          UserTypeJpaRepository userTypeRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.ownerRepository = ownerRepository;
        this.userTypeRepository = userTypeRepository;
    }

    /**
     * Grava e devolve o usuário como ficou no banco.
     * <p>
     * {@code saveAndFlush}, e não {@code save}: a data da última alteração só é preenchida
     * no flush, e sem ele a resposta sairia com a data anterior.
     */
    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity = UserJpaMapper.toEntity(user);
        if (user.getUserType() != null) {
            entity.setUserType(userTypeRepository.getReferenceById(user.getUserType().getId()));
        }
        return UserJpaMapper.toDomain(userRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(UserJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<User> findAll(String name, UserProfile profile, PageQuery pageQuery) {
        String filter = name == null ? "" : name;
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), ORDER);
        Page<? extends UserJpaEntity> page;
        if (profile == UserProfile.CUSTOMER) {
            page = customerRepository.findByNameContainingIgnoreCase(filter, pageable);
        } else if (profile == UserProfile.RESTAURANT_OWNER) {
            page = ownerRepository.findByNameContainingIgnoreCase(filter, pageable);
        } else {
            page = userRepository.findByNameContainingIgnoreCase(filter, pageable);
        }
        return new PageResult<>(
                page.getContent().stream().map(UserJpaMapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(UserJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLoginIgnoreCase(login).map(UserJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByCpf(String cpf) {
        return customerRepository.findByCpf(cpf).map(UserJpaMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByCnpj(String cnpj) {
        return ownerRepository.findByCnpj(cnpj).map(UserJpaMapper::toDomain);
    }

    @Override
    public boolean existsByUserTypeId(Long userTypeId) {
        return userRepository.existsByUserTypeId(userTypeId);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
