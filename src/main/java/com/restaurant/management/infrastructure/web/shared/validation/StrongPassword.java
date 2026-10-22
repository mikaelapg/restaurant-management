package com.restaurant.management.infrastructure.web.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Exige senha forte: mínimo de 8 caracteres, com pelo menos uma letra minúscula,
 * uma maiúscula, um dígito e um caractere especial, e sem espaços.
 * <p>
 * A regra fica num único ponto e é reaproveitada por todos os DTOs que carregam
 * senha (cadastro de cliente, cadastro de dono e troca de senha), em vez de
 * repetir anotações {@code @Size}/{@code @Pattern} em cada campo.
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface StrongPassword {

    String message() default
            "A senha deve ter no mínimo 8 caracteres, incluindo letra minúscula, "
            + "maiúscula, dígito e caractere especial, sem espaços";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
