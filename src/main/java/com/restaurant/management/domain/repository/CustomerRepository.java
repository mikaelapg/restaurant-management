package com.restaurant.management.domain.repository;

import com.restaurant.management.domain.model.Customer;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends UserBaseRepository<Customer> {

    /** Usado para recusar CPF duplicado antes de esbarrar na constraint do banco. */
    Optional<Customer> findByCpf(String cpf);
}
