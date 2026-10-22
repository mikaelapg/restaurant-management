package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserTypeAlreadyExistsException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.domain.usertype.UserType;

/**
 * Renomeia um tipo de usuário.
 * <p>
 * O nome novo pode ser igual ao atual, inclusive mudando só maiúsculas
 * ("cliente" para "Cliente"). Só é conflito se o nome pertencer a <em>outro</em> tipo.
 */
public class UpdateUserTypeUseCase {

    private final UserTypeGateway gateway;

    public UpdateUserTypeUseCase(UserTypeGateway gateway) {
        this.gateway = gateway;
    }

    public UserType execute(Long id, String newName) {
        UserType current = gateway.findById(id)
                .orElseThrow(() -> new UserTypeNotFoundException(id));

        UserType renamed = current.rename(newName);

        gateway.findByNameIgnoreCase(renamed.getName())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new UserTypeAlreadyExistsException(renamed.getName());
                });

        return gateway.save(renamed);
    }
}
