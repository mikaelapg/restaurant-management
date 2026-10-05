package com.restaurant.management.core.domain.shared;

import com.restaurant.management.core.domain.exception.DomainValidationException;

/**
 * Endereço como objeto de valor: não tem id, e dois endereços com os mesmos campos
 * são o mesmo endereço. Por ser um {@code record}, é imutável e já vem com
 * {@code equals} e {@code hashCode} por valor.
 * <p>
 * Logradouro, número, cidade, estado e CEP são obrigatórios; o complemento não.
 * Os textos chegam aqui já sem espaços nas pontas, e o complemento em branco vira {@code null}.
 */
public record Address(String street, String number, String complement,
                      String city, String state, String zipCode) {

    public static final int STREET_MAX_LENGTH = 255;
    public static final int NUMBER_MAX_LENGTH = 20;
    public static final int COMPLEMENT_MAX_LENGTH = 255;
    public static final int CITY_MAX_LENGTH = 100;
    public static final int STATE_MAX_LENGTH = 50;
    public static final int ZIP_CODE_MAX_LENGTH = 20;

    public Address {
        street = required(street, "logradouro", STREET_MAX_LENGTH);
        number = required(number, "número", NUMBER_MAX_LENGTH);
        complement = optional(complement, "complemento", COMPLEMENT_MAX_LENGTH);
        city = required(city, "cidade", CITY_MAX_LENGTH);
        state = required(state, "estado", STATE_MAX_LENGTH);
        zipCode = required(zipCode, "CEP", ZIP_CODE_MAX_LENGTH);
    }

    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("O campo " + field + " do endereço é obrigatório.");
        }
        return checkLength(value.trim(), field, maxLength);
    }

    private static String optional(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return checkLength(value.trim(), field, maxLength);
    }

    private static String checkLength(String value, String field, int maxLength) {
        if (value.length() > maxLength) {
            throw new DomainValidationException(
                    "O campo " + field + " do endereço deve ter no máximo " + maxLength + " caracteres.");
        }
        return value;
    }
}
