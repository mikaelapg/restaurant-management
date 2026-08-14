package com.restaurant.management.service;

import com.restaurant.management.domain.model.Address;
import com.restaurant.management.dto.request.AddressRequest;
import com.restaurant.management.dto.response.AddressResponse;

/**
 * Conversões entre o endereço embutido na entidade e os DTOs da API.
 * O endereço é opcional, então ambos os lados aceitam nulo.
 */
final class AddressMapper {

    private AddressMapper() {
    }

    static Address toEntity(AddressRequest request) {
        if (request == null) {
            return null;
        }
        return Address.builder()
                .street(request.street())
                .number(request.number())
                .complement(request.complement())
                .city(request.city())
                .state(request.state())
                .zipCode(request.zipCode())
                .build();
    }

    static AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getCity(),
                address.getState(),
                address.getZipCode()
        );
    }
}
