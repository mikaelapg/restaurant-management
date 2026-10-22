package com.restaurant.management.core.domain.user;

import com.restaurant.management.core.domain.exception.DomainValidationException;

import java.util.regex.Pattern;

/**
 * Dados próprios do dono de restaurante. Os dois campos são opcionais.
 * <p>
 * O nome aqui é o nome fantasia ou a razão social da empresa do dono. Os restaurantes
 * que ele administra são outro cadastro, em {@code /api/v1/restaurants}.
 *
 * @param restaurantName nome fantasia ou razão social; em branco vira {@code null}
 * @param cnpj           CNPJ no formato {@code 00.000.000/0000-00}; em branco vira {@code null}
 */
public record RestaurantOwnerDetails(String restaurantName, String cnpj) implements ProfileDetails {

    public static final int RESTAURANT_NAME_MAX_LENGTH = 255;

    private static final Pattern CNPJ_FORMAT = Pattern.compile("\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}");

    public RestaurantOwnerDetails {
        restaurantName = restaurantName == null || restaurantName.isBlank() ? null : restaurantName.trim();
        if (restaurantName != null && restaurantName.length() > RESTAURANT_NAME_MAX_LENGTH) {
            throw new DomainValidationException(
                    "O nome do restaurante deve ter no máximo " + RESTAURANT_NAME_MAX_LENGTH + " caracteres.");
        }
        cnpj = cnpj == null || cnpj.isBlank() ? null : cnpj.trim();
        if (cnpj != null && !CNPJ_FORMAT.matcher(cnpj).matches()) {
            throw new DomainValidationException("O CNPJ deve seguir o formato 00.000.000/0000-00.");
        }
    }

    @Override
    public UserProfile profile() {
        return UserProfile.RESTAURANT_OWNER;
    }
}
