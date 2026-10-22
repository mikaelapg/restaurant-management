package com.restaurant.management.core.domain.user;

/**
 * Perfil cadastral do usuário: define quais dados próprios ele tem.
 * <p>
 * O cliente tem CPF e data de nascimento; o dono de restaurante tem nome do restaurante
 * e CNPJ. O perfil é escolhido no cadastro (pelo endpoint usado) e não muda depois.
 * <p>
 * Não confundir com o {@link com.restaurant.management.core.domain.usertype.UserType tipo
 * de usuário}, que é um cadastro gerenciado pela API. Os dois se ligam pelos tipos de
 * sistema: "Dono de Restaurante" corresponde a {@link #RESTAURANT_OWNER} e "Cliente" a
 * {@link #CUSTOMER}.
 */
public enum UserProfile {

    /** Dono de restaurante: pode ser responsável por restaurantes. */
    RESTAURANT_OWNER,

    /** Cliente: consulta restaurantes e cardápios. */
    CUSTOMER
}
