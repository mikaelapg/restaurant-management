package com.restaurant.management.support;

import com.restaurant.management.core.domain.shared.Address;
import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.core.domain.usertype.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Objetos de domínio prontos, para os testes não repetirem a montagem. */
public final class Fixtures {

    public static final UserType OWNER_TYPE = UserType.restore(1L, "Dono de Restaurante", UserProfile.RESTAURANT_OWNER);
    public static final UserType CUSTOMER_TYPE = UserType.restore(2L, "Cliente", UserProfile.CUSTOMER);
    public static final UserType CUSTOM_TYPE = UserType.restore(3L, "Gerente");

    public static final Address ADDRESS = new Address("Rua Augusta", "1500", "Loja 2", "São Paulo", "SP", "01305-100");
    public static final String PASSWORD = "Senha@123";
    public static final String HASH = "$2a$12$hash-de-teste";
    public static final LocalDateTime MODIFIED_AT = LocalDateTime.of(2026, 10, 22, 10, 0);

    public static final CustomerDetails CUSTOMER_DETAILS = new CustomerDetails("123.456.789-00", LocalDate.of(1995, 7, 15));
    public static final RestaurantOwnerDetails OWNER_DETAILS = new RestaurantOwnerDetails("Grupo Silva", "12.345.678/0001-90");

    private Fixtures() {
    }

    public static User owner(Long id) {
        return owner(id, OWNER_TYPE);
    }

    public static User owner(Long id, UserType userType) {
        return User.restore(id, "João Silva", "joao@restaurante.com", "joao.silva", HASH, ADDRESS,
                OWNER_DETAILS, userType, MODIFIED_AT);
    }

    public static User customer(Long id) {
        return customer(id, CUSTOMER_TYPE);
    }

    public static User customer(Long id, UserType userType) {
        return User.restore(id, "Maria Oliveira", "maria@email.com", "maria.oliveira", HASH, null,
                CUSTOMER_DETAILS, userType, MODIFIED_AT);
    }
}
