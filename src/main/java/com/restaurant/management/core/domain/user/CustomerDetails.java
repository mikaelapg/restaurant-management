package com.restaurant.management.core.domain.user;

import com.restaurant.management.core.domain.exception.DomainValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Dados próprios do cliente. Os dois campos são opcionais.
 *
 * @param cpf         CPF no formato {@code 000.000.000-00}; em branco vira {@code null}
 * @param dateOfBirth data de nascimento, sempre no passado
 */
public record CustomerDetails(String cpf, LocalDate dateOfBirth) implements ProfileDetails {

    private static final Pattern CPF_FORMAT = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");

    public CustomerDetails {
        cpf = cpf == null || cpf.isBlank() ? null : cpf.trim();
        if (cpf != null && !CPF_FORMAT.matcher(cpf).matches()) {
            throw new DomainValidationException("O CPF deve seguir o formato 000.000.000-00.");
        }
        if (dateOfBirth != null && !dateOfBirth.isBefore(LocalDate.now())) {
            throw new DomainValidationException("A data de nascimento deve estar no passado.");
        }
    }

    @Override
    public UserProfile profile() {
        return UserProfile.CUSTOMER;
    }
}
