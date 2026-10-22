package com.restaurant.management.core.application.exception;

/** Lançada quando o CPF já pertence a outro cliente. Resulta em HTTP 409. */
public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException(String cpf) {
        super("Já existe um cliente cadastrado com o CPF: " + cpf);
    }
}
