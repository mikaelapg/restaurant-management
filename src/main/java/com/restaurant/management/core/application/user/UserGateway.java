package com.restaurant.management.core.application.user;

/**
 * O que os casos de uso da Fase 2 precisam saber sobre usuários.
 * <p>
 * O cadastro de usuários ainda é o da Fase 1, em camadas tradicionais. Em vez de os
 * casos de uso dependerem daquele código, eles pedem só isto, e a infraestrutura
 * faz a ponte. Quando o cadastro de usuários migrar para a estrutura nova, esta
 * interface continua valendo.
 */
public interface UserGateway {

    boolean existsById(Long userId);

    /** Associa o usuário ao tipo informado, substituindo o tipo anterior, se houver. */
    void assignUserType(Long userId, Long userTypeId);

    /** Indica se algum usuário está associado ao tipo. */
    boolean existsByUserTypeId(Long userTypeId);
}
