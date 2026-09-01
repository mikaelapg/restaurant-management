package com.restaurant.management.dto.request.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implementa a regra de {@link StrongPassword}.
 * <p>
 * Um valor {@code null} é considerado válido — a obrigatoriedade continua a cargo
 * de {@code @NotBlank}, mantendo cada anotação com uma única responsabilidade.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    /** 8+ caracteres, com minúscula, maiúscula, dígito e especial, e sem espaços. */
    private static final Pattern STRONG = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,72}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || STRONG.matcher(value).matches();
    }
}
