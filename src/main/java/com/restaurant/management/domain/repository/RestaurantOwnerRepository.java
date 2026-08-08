package com.restaurant.management.domain.repository;

import com.restaurant.management.domain.model.RestaurantOwner;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantOwnerRepository extends UserBaseRepository<RestaurantOwner> {
}
