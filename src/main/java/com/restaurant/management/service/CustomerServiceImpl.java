package com.restaurant.management.service;

import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.usertype.UserTypeGateway;
import com.restaurant.management.core.domain.usertype.UserType;
import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.repository.CustomerRepository;
import com.restaurant.management.dto.request.CreateCustomerRequest;
import com.restaurant.management.dto.request.UpdateCustomerRequest;
import com.restaurant.management.dto.response.CustomerResponse;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.exception.CpfAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação das regras de negócio dos clientes.
 * Troca de senha e exclusão vêm de {@link BaseUserService}.
 */
@Slf4j
@Service
public class CustomerServiceImpl extends BaseUserService<Customer> implements CustomerService {

    /** Mesma instância de {@code repository}, na forma concreta, para as consultas por CPF. */
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               UserUniquenessValidator uniquenessValidator,
                               BCryptPasswordEncoder passwordEncoder,
                               UserTypeGateway userTypeGateway,
                               RestaurantGateway restaurantGateway) {
        super(customerRepository, uniquenessValidator, passwordEncoder, userTypeGateway, restaurantGateway);
        this.customerRepository = customerRepository;
    }

    /**
     * Garante que o CPF não pertença a outro cliente, recusando a duplicidade com uma mensagem
     * que aponta o campo — em vez de deixar a constraint do banco cair no tratamento genérico.
     * <p>
     * CPF ausente não é checado: o campo é opcional e o banco aceita vários registros sem ele.
     *
     * @param currentUserId id do próprio cliente em uma atualização,
     *                      ou {@code null} quando for um cadastro novo
     */
    private void ensureCpfIsAvailable(String cpf, Long currentUserId) {
        if (cpf == null || cpf.isBlank()) {
            return;
        }
        customerRepository.findByCpf(cpf)
                .filter(other -> !other.getId().equals(currentUserId))
                .ifPresent(other -> {
                    throw new CpfAlreadyExistsException(cpf);
                });
    }

    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        ensureEmailIsAvailable(request.email(), null);
        ensureLoginIsAvailable(request.login(), null);
        ensureCpfIsAvailable(request.cpf(), null);

        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .login(request.login())
                .password(passwordEncoder.encode(request.password()))
                .address(AddressMapper.toEntity(request.address()))
                .assignedUserType(defaultUserType(UserType.CUSTOMER))
                .cpf(request.cpf())
                .dateOfBirth(request.dateOfBirth())
                .build();

        Customer saved = repository.save(customer);
        log.info("Cliente cadastrado com o id {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> findAll(String name, Pageable pageable) {
        var page = (name == null || name.isBlank())
                ? customerRepository.findAll(pageable)
                : customerRepository.findByNameContainingIgnoreCase(name, pageable);

        return PageResponse.of(page, this::toResponse);
    }

    @Override
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
    @Override
    @Transactional
    public CustomerResponse update(Long id, UpdateCustomerRequest request) {
        Customer customer = findOrThrow(id);

        ensureEmailIsAvailable(request.email(), id);
        ensureLoginIsAvailable(request.login(), id);
        ensureCpfIsAvailable(request.cpf(), id);

        customer.setName(request.name());
        customer.setEmail(request.email().toLowerCase());
        customer.setLogin(request.login());
        customer.setCpf(request.cpf());
        customer.setDateOfBirth(request.dateOfBirth());
        if (request.address() != null) {
            customer.setAddress(AddressMapper.toEntity(request.address()));
        }

        log.info("Dados do cliente {} atualizados", id);
        // saveAndFlush: o @UpdateTimestamp só é aplicado no flush. Com save(), o DTO seria
        // montado antes disso e devolveria o lastModifiedAt do cadastro, não o da alteração.
        return toResponse(repository.saveAndFlush(customer));
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
                customer.getDateOfBirth(),
                UserTypeMapper.toResponse(customer.getAssignedUserType())
        );
    }
}
