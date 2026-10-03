package com.restaurant.management.core.application.exception;

/**
 * Lançada quando nenhum usuário tem o id informado. Resulta em HTTP 404.
 * <p>
 * Os casos de uso da Fase 2 a usam ao validar referências a usuários (associação de
 * tipo, dono de restaurante). A exceção homônima da Fase 1 herda desta, então as
 * duas camadas respondem com o mesmo corpo de erro.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuário não encontrado com o ID: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
