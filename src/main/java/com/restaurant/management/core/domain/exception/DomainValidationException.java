package com.restaurant.management.core.domain.exception;

/**
 * Lançada quando uma entidade de domínio recebe dados que quebram uma regra dela,
 * como um nome vazio ou longo demais. Resulta em HTTP 400.
 * <p>
 * Existe além do Bean Validation dos DTOs porque o domínio pode ser usado sem passar
 * pela camada web (por um teste, um job, outro caso de uso) e precisa se proteger sozinho.
 */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
