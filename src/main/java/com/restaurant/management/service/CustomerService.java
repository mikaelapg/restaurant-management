package com.restaurant.management.service;

import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.repository.CustomerRepository;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.request.CreateCustomerRequest;
import com.restaurant.management.dto.request.UpdateCustomerRequest;
import com.restaurant.management.dto.response.CustomerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio dos clientes.
 * Troca de senha e exclusão vêm de {@link BaseUserService}.
 */
@Slf4j
@Service
public class CustomerService extends BaseUserService<Customer> {

    public CustomerService(CustomerRepository customerRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        super(customerRepository, userRepository, passwordEncoder);
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        ensureEmailIsAvailable(request.email(), null);
        ensureLoginIsAvailable(request.login(), null);

        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .login(request.login())
                .password(passwordEncoder.encode(request.password()))
                .address(AddressMapper.toEntity(request.address()))
                .cpf(request.cpf())
                .dateOfBirth(request.dateOfBirth())
                .build();

        Customer saved = repository.save(customer);
        log.info("Cliente cadastrado com o id {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll(String name) {
        List<Customer> customers = (name == null || name.isBlank())
                ? repository.findAll()
                : repository.findByNameContainingIgnoreCase(name);

        return customers.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Atualiza os dados cadastrais do cliente. A senha não passa por aqui —
     * ela tem endpoint exclusivo em {@code PATCH /api/v1/customers/{id}/password}.
     * <p>
     * Endereço nulo significa "não mexer", e não "apagar".
     */
    @Transactional
    public CustomerResponse update(Long id, UpdateCustomerRequest request) {
        Customer customer = findOrThrow(id);

        ensureEmailIsAvailable(request.email(), id);
        ensureLoginIsAvailable(request.login(), id);

        customer.setName(request.name());
        customer.setEmail(request.email().toLowerCase());
        customer.setLogin(request.login());
        customer.setCpf(request.cpf());
        customer.setDateOfBirth(request.dateOfBirth());
        if (request.address() != null) {
            customer.setAddress(AddressMapper.toEntity(request.address()));
        }

        log.info("Dados do cliente {} atualizados", id);
        return toResponse(repository.save(customer));
    }

    /** A senha nunca é devolvida na resposta. */
    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getUserType(),
                customer.getName(),
                customer.getEmail(),
                customer.getLogin(),
                customer.getLastModifiedAt(),
                AddressMapper.toResponse(customer.getAddress()),
                customer.getCpf(),
                customer.getDateOfBirth()
        );
    }
}
