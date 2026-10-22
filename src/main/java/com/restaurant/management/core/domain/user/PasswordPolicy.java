package com.restaurant.management.core.domain.user;

import com.restaurant.management.core.domain.exception.DomainValidationException;

import java.util.regex.Pattern;

/**
 * Regra de senha forte: de 8 a 72 caracteres, com pelo menos uma letra minúscula, uma
 * maiúscula, um dígito e um caractere especial, e sem espaços.
 * <p>
 * O limite de 72 vem do BCrypt, que ignora o que passa disso. A regra mora no domínio e
 * é a mesma usada pela anotação {@code @StrongPassword} dos DTOs: a API responde 400 com
 * o campo apontado, e os casos de uso continuam protegidos se forem chamados por outro
 * caminho que não o HTTP.
 */
public final class PasswordPolicy {

    public static final String MESSAGE = "A senha deve ter no mínimo 8 caracteres, incluindo letra minúscula, "
            + "maiúscula, dígito e caractere especial, sem espaços";

    private static final Pattern STRONG = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,72}$");

    private PasswordPolicy() {
    }

    public static boolean isStrong(String rawPassword) {
        return rawPassword != null && STRONG.matcher(rawPassword).matches();
    }

    /** Recusa a senha que não segue a regra. */
    public static void validate(String rawPassword) {
        if (!isStrong(rawPassword)) {
            throw new DomainValidationException(MESSAGE + ".");
        }
    }
}
