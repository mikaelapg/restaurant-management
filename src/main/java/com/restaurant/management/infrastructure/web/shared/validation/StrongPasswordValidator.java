package com.restaurant.management.infrastructure.web.shared.validation;

import com.restaurant.management.core.domain.user.PasswordPolicy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementa {@link StrongPassword} delegando à {@link PasswordPolicy} do domínio, para
 * que a regra exista num lugar só.
 * <p>
 * Um valor {@code null} é considerado válido: a obrigatoriedade continua a cargo de
 * {@code @NotBlank}, e cada anotação fica com uma única responsabilidade.
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || PasswordPolicy.isStrong(value);
    }
}
