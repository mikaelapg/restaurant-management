package com.restaurant.management.core.domain.restaurant;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.shared.Address;

import java.util.Objects;

/**
 * Restaurante cadastrado no sistema.
 * <p>
 * Segue o mesmo desenho do {@code UserType}: Java puro e imutável. Toda alteração
 * devolve uma nova instância, que passa outra vez pelas validações.
 * <p>
 * O dono é guardado só pelo id. Quem confere se esse usuário existe é o caso de uso,
 * porque a entidade não tem como consultar o cadastro de usuários.
 */
public final class Restaurant {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int CUISINE_TYPE_MAX_LENGTH = 50;

    private final Long id;
    private final String name;
    private final Address address;
    private final String cuisineType;
    private final OpeningHours openingHours;
    private final Long ownerId;

    private Restaurant(Long id, String name, Address address, String cuisineType,
                       OpeningHours openingHours, Long ownerId) {
        this.id = id;
        this.name = requiredText(name, "nome do restaurante", NAME_MAX_LENGTH);
        this.address = required(address, "O endereço do restaurante é obrigatório.");
        this.cuisineType = requiredText(cuisineType, "tipo de cozinha", CUISINE_TYPE_MAX_LENGTH);
        this.openingHours = required(openingHours, "O horário de funcionamento é obrigatório.");
        this.ownerId = required(ownerId, "O dono do restaurante é obrigatório.");
    }

    /** Cria um restaurante novo, ainda sem id. */
    public static Restaurant create(String name, Address address, String cuisineType,
                                    OpeningHours openingHours, Long ownerId) {
        return new Restaurant(null, name, address, cuisineType, openingHours, ownerId);
    }

    /** Reconstrói um restaurante que já existe, por exemplo ao ler do banco. */
    public static Restaurant restore(Long id, String name, Address address, String cuisineType,
                                     OpeningHours openingHours, Long ownerId) {
        Objects.requireNonNull(id, "Um restaurante existente precisa de id.");
        return new Restaurant(id, name, address, cuisineType, openingHours, ownerId);
    }

    /** Devolve uma cópia com os dados novos, mantendo o id. */
    public Restaurant update(String name, Address address, String cuisineType,
                             OpeningHours openingHours, Long ownerId) {
        return new Restaurant(this.id, name, address, cuisineType, openingHours, ownerId);
    }

    private static String requiredText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("O campo " + field + " é obrigatório.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new DomainValidationException(
                    "O campo " + field + " deve ter no máximo " + maxLength + " caracteres.");
        }
        return trimmed;
    }

    private static <T> T required(T value, String message) {
        if (value == null) {
            throw new DomainValidationException(message);
        }
        return value;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
