package com.restaurant.management.core.application.exception;

/**
 * Lançada quando nenhum tipo de usuário tem o id informado. Resulta em HTTP 404.
 * <p>
 * Fica na camada de aplicação, e não no domínio, porque "não encontrado" só existe
 * quando um caso de uso vai buscar algo no gateway; a entidade em si não tem essa noção.
 */
public class UserTypeNotFoundException extends RuntimeException {

    public UserTypeNotFoundException(Long id) {
        super("Tipo de usuário não encontrado com o ID: " + id);
    }
}
