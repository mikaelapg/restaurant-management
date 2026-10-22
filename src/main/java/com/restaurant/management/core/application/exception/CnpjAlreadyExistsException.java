package com.restaurant.management.core.application.exception;

/** Lançada quando o CNPJ já pertence a outro dono de restaurante. Resulta em HTTP 409. */
public class CnpjAlreadyExistsException extends RuntimeException {

    public CnpjAlreadyExistsException(String cnpj) {
        super("Já existe um dono de restaurante cadastrado com o CNPJ: " + cnpj);
    }
}
