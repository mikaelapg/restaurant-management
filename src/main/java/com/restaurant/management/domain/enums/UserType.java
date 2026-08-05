package com.restaurant.management.domain.enums;

/**
 * Tipos de usuário suportados pelo sistema.
 * Usado como discriminador na herança JOINED entre as entidades.
 */
public enum UserType {

    /** Dono de restaurante — pode gerenciar o estabelecimento. */
    RESTAURANT_OWNER,

    /** Cliente — pode consultar restaurantes e fazer pedidos. */
    CUSTOMER
}
