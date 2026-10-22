package com.restaurant.management.core.domain.user;

/**
 * Dados próprios de cada perfil de usuário.
 * <p>
 * É uma interface selada: só existem as duas implementações abaixo, e o compilador
 * obriga quem faz {@code switch} sobre ela a tratar as duas. O perfil do usuário é
 * deduzido do tipo destes dados, então não há como um usuário ter CPF e ser dono.
 */
public sealed interface ProfileDetails permits CustomerDetails, RestaurantOwnerDetails {

    /** Perfil ao qual estes dados pertencem. */
    UserProfile profile();
}
