package com.restaurant.management.service;

import com.restaurant.management.core.application.usertype.UserTypeGateway;
import com.restaurant.management.core.domain.usertype.UserType;
import com.restaurant.management.domain.model.RestaurantOwner;
import com.restaurant.management.domain.repository.RestaurantOwnerRepository;
import com.restaurant.management.dto.request.CreateRestaurantOwnerRequest;
import com.restaurant.management.dto.request.UpdateRestaurantOwnerRequest;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.RestaurantOwnerResponse;
import com.restaurant.management.exception.CnpjAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação das regras de negócio dos donos de restaurante.
 * Troca de senha e exclusão vêm de {@link BaseUserService}.
 */
@Slf4j
@Service
public class RestaurantOwnerServiceImpl extends BaseUserService<RestaurantOwner> implements RestaurantOwnerService {

    /** Mesma instância de {@code repository}, na forma concreta, para as consultas por CNPJ. */
    private final RestaurantOwnerRepository restaurantOwnerRepository;

    public RestaurantOwnerServiceImpl(RestaurantOwnerRepository restaurantOwnerRepository,
                                      UserUniquenessValidator uniquenessValidator,
                                      BCryptPasswordEncoder passwordEncoder,
                                      UserTypeGateway userTypeGateway) {
        super(restaurantOwnerRepository, uniquenessValidator, passwordEncoder, userTypeGateway);
        this.restaurantOwnerRepository = restaurantOwnerRepository;
    }

    /**
     * Garante que o CNPJ não pertença a outro dono, recusando a duplicidade com uma mensagem
     * que aponta o campo — em vez de deixar a constraint do banco cair no tratamento genérico.
     * <p>
     * CNPJ ausente não é checado: o campo é opcional e o banco aceita vários registros sem ele.
     *
     * @param currentUserId id do próprio dono em uma atualização,
     *                      ou {@code null} quando for um cadastro novo
     */
    private void ensureCnpjIsAvailable(String cnpj, Long currentUserId) {
        if (cnpj == null || cnpj.isBlank()) {
            return;
        }
        restaurantOwnerRepository.findByCnpj(cnpj)
                .filter(other -> !other.getId().equals(currentUserId))
                .ifPresent(other -> {
                    throw new CnpjAlreadyExistsException(cnpj);
                });
    }

    @Override
    @Transactional
    public RestaurantOwnerResponse create(CreateRestaurantOwnerRequest request) {
        ensureEmailIsAvailable(request.email(), null);
        ensureLoginIsAvailable(request.login(), null);
        ensureCnpjIsAvailable(request.cnpj(), null);

        RestaurantOwner owner = RestaurantOwner.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .login(request.login())
                .password(passwordEncoder.encode(request.password()))
                .address(AddressMapper.toEntity(request.address()))
                .assignedUserType(defaultUserType(UserType.RESTAURANT_OWNER))
                .restaurantName(request.restaurantName())
                .cnpj(request.cnpj())
                .build();

        RestaurantOwner saved = repository.save(owner);
        log.info("Dono de restaurante cadastrado com o id {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RestaurantOwnerResponse> findAll(String name, Pageable pageable) {
        var page = (name == null || name.isBlank())
                ? restaurantOwnerRepository.findAll(pageable)
                : restaurantOwnerRepository.findByNameContainingIgnoreCase(name, pageable);

        return PageResponse.of(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantOwnerResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Atualiza os dados cadastrais do dono. A senha não passa por aqui —
     * ela tem endpoint exclusivo em {@code PATCH /api/v1/restaurant-owners/{id}/password}.
     * <p>
     * Endereço nulo significa "não mexer", e não "apagar".
     */
    @Override
    @Transactional
    public RestaurantOwnerResponse update(Long id, UpdateRestaurantOwnerRequest request) {
        RestaurantOwner owner = findOrThrow(id);

        ensureEmailIsAvailable(request.email(), id);
        ensureLoginIsAvailable(request.login(), id);
        ensureCnpjIsAvailable(request.cnpj(), id);

        owner.setName(request.name());
        owner.setEmail(request.email().toLowerCase());
        owner.setLogin(request.login());
        owner.setRestaurantName(request.restaurantName());
        owner.setCnpj(request.cnpj());
        if (request.address() != null) {
            owner.setAddress(AddressMapper.toEntity(request.address()));
        }

        log.info("Dados do dono de restaurante {} atualizados", id);
        // saveAndFlush: o @UpdateTimestamp só é aplicado no flush. Com save(), o DTO seria
        // montado antes disso e devolveria o lastModifiedAt do cadastro, não o da alteração.
        return toResponse(repository.saveAndFlush(owner));
    }

    /** A senha nunca é devolvida na resposta. */
    private RestaurantOwnerResponse toResponse(RestaurantOwner owner) {
        return new RestaurantOwnerResponse(
                owner.getId(),
                owner.getUserType(),
                owner.getName(),
                owner.getEmail(),
                owner.getLogin(),
                owner.getLastModifiedAt(),
                AddressMapper.toResponse(owner.getAddress()),
                owner.getRestaurantName(),
                owner.getCnpj(),
                UserTypeMapper.toResponse(owner.getAssignedUserType())
        );
    }
}
