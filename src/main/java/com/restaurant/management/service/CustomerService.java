package com.restaurant.management.service;

import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateCustomerRequest;
import com.restaurant.management.dto.request.UpdateCustomerRequest;
import com.restaurant.management.dto.response.CustomerResponse;
import com.restaurant.management.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Regras de negócio do cliente.
 * <p>
 * A abstração existe para que a camada web (e os testes) dependam do contrato,
 * não da implementação (Princípio da Inversão de Dependência).
 */
public interface CustomerService {

    CustomerResponse create(CreateCustomerRequest request);

    PageResponse<CustomerResponse> findAll(String name, Pageable pageable);

    CustomerResponse findById(Long id);

    CustomerResponse update(Long id, UpdateCustomerRequest request);

    /** Troca de senha com verificação da senha atual. Contrato comum a qualquer usuário. */
    void changePassword(Long id, ChangePasswordRequest request);

    void delete(Long id);
}
