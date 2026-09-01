package com.restaurant.management.service;

import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateRestaurantOwnerRequest;
import com.restaurant.management.dto.request.UpdateRestaurantOwnerRequest;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.RestaurantOwnerResponse;
import org.springframework.data.domain.Pageable;

/**
 * Regras de negócio do dono de restaurante.
 * <p>
 * A abstração existe para que a camada web (e os testes) dependam do contrato,
 * não da implementação (Princípio da Inversão de Dependência).
 */
public interface RestaurantOwnerService {

    RestaurantOwnerResponse create(CreateRestaurantOwnerRequest request);

    PageResponse<RestaurantOwnerResponse> findAll(String name, Pageable pageable);

    RestaurantOwnerResponse findById(Long id);

    RestaurantOwnerResponse update(Long id, UpdateRestaurantOwnerRequest request);

    /** Troca de senha com verificação da senha atual. Contrato comum a qualquer usuário. */
    void changePassword(Long id, ChangePasswordRequest request);

    void delete(Long id);
}
