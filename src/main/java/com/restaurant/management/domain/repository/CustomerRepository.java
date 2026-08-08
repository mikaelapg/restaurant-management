package com.restaurant.management.domain.repository;

import com.restaurant.management.domain.model.Customer;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends UserBaseRepository<Customer> {
}
