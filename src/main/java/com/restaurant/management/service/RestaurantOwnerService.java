package com.restaurant.management.service;

import com.restaurant.management.domain.model.RestaurantOwner;
import com.restaurant.management.domain.repository.RestaurantOwnerRepository;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.request.CreateRestaurantOwnerRequest;
import com.restaurant.management.dto.request.UpdateRestaurantOwnerRequest;
import com.restaurant.management.dto.response.RestaurantOwnerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio dos donos de restaurante.
 * Troca de senha e exclusão vêm de {@link BaseUserService}.
 */
@Slf4j
@Service
public class RestaurantOwnerService extends BaseUserService<RestaurantOwner> {

    public RestaurantOwnerService(RestaurantOwnerRepository restaurantOwnerRepository,
                                  UserRepository userRepository,
                                  BCryptPasswordEncoder passwordEncoder) {
        super(restaurantOwnerRepository, userRepository, passwordEncoder);
    }

    @Transactional
    public RestaurantOwnerResponse create(CreateRestaurantOwnerRequest request) {
        ensureEmailIsAvailable(request.email(), null);
        ensureLoginIsAvailable(request.login(), null);

        RestaurantOwner owner = RestaurantOwner.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .login(request.login())
                .password(passwordEncoder.encode(request.password()))
                .address(AddressMapper.toEntity(request.address()))
                .restaurantName(request.restaurantName())
                .cnpj(request.cnpj())
                .build();

        RestaurantOwner saved = repository.save(owner);
        log.info("Dono de restaurante cadastrado com o id {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RestaurantOwnerResponse> findAll(String name) {
        List<RestaurantOwner> owners = (name == null || name.isBlank())
                ? repository.findAll()
                : repository.findByNameContainingIgnoreCase(name);

        return owners.stream().map(this::toResponse).toList();
    }

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
    @Transactional
    public RestaurantOwnerResponse update(Long id, UpdateRestaurantOwnerRequest request) {
        RestaurantOwner owner = findOrThrow(id);

        ensureEmailIsAvailable(request.email(), id);
        ensureLoginIsAvailable(request.login(), id);

        owner.setName(request.name());
        owner.setEmail(request.email().toLowerCase());
        owner.setLogin(request.login());
        owner.setRestaurantName(request.restaurantName());
        owner.setCnpj(request.cnpj());
        if (request.address() != null) {
            owner.setAddress(AddressMapper.toEntity(request.address()));
        }

        log.info("Dados do dono de restaurante {} atualizados", id);
        return toResponse(repository.save(owner));
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
                owner.getCnpj()
        );
    }
}
