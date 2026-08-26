package com.restaurant.management.exception;

/**
 * Lançada quando o CPF informado já está cadastrado para outro cliente.
 * Resulta em resposta HTTP 409 Conflict com ProblemDetail.
 */
public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException(String cpf) {
        super("Já existe um cliente cadastrado com o CPF: " + cpf);
    }
}
