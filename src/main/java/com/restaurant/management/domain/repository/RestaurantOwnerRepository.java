package com.restaurant.management.domain.repository;

import com.restaurant.management.domain.model.RestaurantOwner;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantOwnerRepository extends UserBaseRepository<RestaurantOwner> {

    /** Usado para recusar CNPJ duplicado antes de esbarrar na constraint do banco. */
    Optional<RestaurantOwner> findByCnpj(String cnpj);
}
