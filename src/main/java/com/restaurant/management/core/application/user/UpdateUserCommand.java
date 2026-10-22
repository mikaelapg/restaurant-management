package com.restaurant.management.core.application.user;

import com.restaurant.management.core.domain.shared.Address;
import com.restaurant.management.core.domain.user.ProfileDetails;

/**
 * Dados de entrada para atualizar o cadastro de um usuário. A senha não passa por aqui:
 * ela tem caso de uso próprio, que exige a senha atual.
 *
 * @param address endereço novo; {@code null} significa "manter o atual", e não "apagar"
 * @param details dados do perfil; precisam ser do mesmo perfil do usuário
 */
public record UpdateUserCommand(String name, String email, String login,
                                Address address, ProfileDetails details) {
}
