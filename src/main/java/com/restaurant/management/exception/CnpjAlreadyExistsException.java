package com.restaurant.management.exception;

/**
 * Lançada quando o CNPJ informado já está cadastrado para outro dono de restaurante.
 * Resulta em resposta HTTP 409 Conflict com ProblemDetail.
 */
public class CnpjAlreadyExistsException extends RuntimeException {

    public CnpjAlreadyExistsException(String cnpj) {
        super("Já existe um dono de restaurante cadastrado com o CNPJ: " + cnpj);
    }
}
