package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.exception.CnpjAlreadyExistsException;
import com.restaurant.management.core.application.exception.CpfAlreadyExistsException;
import com.restaurant.management.core.application.exception.EmailAlreadyExistsException;
import com.restaurant.management.core.application.exception.LoginAlreadyExistsException;
import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.core.domain.user.User;

import java.util.Optional;

/**
 * Garante que e-mail, login, CPF e CNPJ não pertençam a <em>outro</em> usuário.
 * <p>
 * Usada no cadastro e na atualização. O banco também tem as restrições de unicidade; a
 * checagem aqui existe para responder 409 dizendo qual campo conflitou, em vez do erro
 * genérico de integridade.
 */
public class UserUniquenessPolicy {

    private final UserGateway gateway;

    public UserUniquenessPolicy(UserGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * @param candidate usuário com os dados novos; o id é {@code null} num cadastro
     *                  e o do próprio usuário numa atualização
     */
    public void ensureAvailable(User candidate) {
        if (takenByOther(gateway.findByEmail(candidate.getEmail()), candidate)) {
            throw new EmailAlreadyExistsException(candidate.getEmail());
        }
        if (takenByOther(gateway.findByLogin(candidate.getLogin()), candidate)) {
            throw new LoginAlreadyExistsException(candidate.getLogin());
        }
        switch (candidate.getDetails()) {
            case CustomerDetails customer -> {
                if (customer.cpf() != null && takenByOther(gateway.findByCpf(customer.cpf()), candidate)) {
                    throw new CpfAlreadyExistsException(customer.cpf());
                }
            }
            case RestaurantOwnerDetails owner -> {
                if (owner.cnpj() != null && takenByOther(gateway.findByCnpj(owner.cnpj()), candidate)) {
                    throw new CnpjAlreadyExistsException(owner.cnpj());
                }
            }
        }
    }

    private static boolean takenByOther(Optional<User> found, User candidate) {
        return found.filter(other -> !other.getId().equals(candidate.getId())).isPresent();
    }
}
