package com.restaurant.management.core.application.user;

import com.restaurant.management.core.domain.shared.Address;
import com.restaurant.management.core.domain.user.ProfileDetails;

/**
 * Dados de entrada para cadastrar um usuário.
 * <p>
 * O perfil vem dos {@code details}: {@code CustomerDetails} cadastra um cliente e
 * {@code RestaurantOwnerDetails}, um dono de restaurante.
 *
 * @param address  endereço, opcional
 * @param password senha em texto puro; o caso de uso valida e gera o hash
 */
public record CreateUserCommand(String name, String email, String login, String password,
                                Address address, ProfileDetails details) {
}
