package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserTypeChangeNotAllowedException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.usertype.UserType;

/**
 * Associa um usuário existente a um tipo de usuário existente.
 * <p>
 * Um usuário tem um único tipo; associar de novo troca o tipo anterior. Duas regras
 * mantêm o cadastro coerente:
 * <ul>
 *   <li>um tipo de sistema só vai para usuários do mesmo perfil ("Cliente" não pode ser
 *       dado a um dono de restaurante) — 422, regra do próprio {@link User};</li>
 *   <li>quem é responsável por restaurantes não pode perder o tipo de dono — 409.</li>
 * </ul>
 */
public class AssignUserTypeUseCase {

    private final UserGateway userGateway;
    private final UserTypeGateway userTypeGateway;
    private final RestaurantGateway restaurantGateway;

    public AssignUserTypeUseCase(UserGateway userGateway, UserTypeGateway userTypeGateway,
                                 RestaurantGateway restaurantGateway) {
        this.userGateway = userGateway;
        this.userTypeGateway = userTypeGateway;
        this.restaurantGateway = restaurantGateway;
    }

    /** Devolve o usuário já com o tipo novo. */
    public User execute(Long userId, Long userTypeId) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        UserType userType = userTypeGateway.findById(userTypeId)
                .orElseThrow(() -> new UserTypeNotFoundException(userTypeId));

        User updated = user.assignType(userType);
        if (user.canOwnRestaurants() && !updated.canOwnRestaurants() && restaurantGateway.existsByOwnerId(userId)) {
            throw new UserTypeChangeNotAllowedException(userId);
        }
        return userGateway.save(updated);
    }
}
