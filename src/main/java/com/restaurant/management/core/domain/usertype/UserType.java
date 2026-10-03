package com.restaurant.management.core.domain.usertype;

import com.restaurant.management.core.domain.exception.DomainValidationException;

import java.util.Objects;

/**
 * Tipo de usuário do sistema, como "Dono de Restaurante" ou "Cliente".
 * <p>
 * Entidade de domínio: Java puro, sem anotações de JPA nem de Spring. Ela não sabe
 * que existe banco de dados ou HTTP; só conhece as próprias regras.
 * <p>
 * É imutável: renomear devolve uma nova instância em vez de alterar esta. Assim
 * nenhuma instância chega a existir em estado inválido, porque toda criação passa
 * pela validação do nome.
 */
public final class UserType {

    /** Tamanho máximo do nome, espelhado na coluna {@code user_types.name}. */
    public static final int NAME_MAX_LENGTH = 50;

    /** Tipo atribuído a quem se cadastra como dono de restaurante. Criado pela migration V3. */
    public static final String RESTAURANT_OWNER = "Dono de Restaurante";

    /** Tipo atribuído a quem se cadastra como cliente. Criado pela migration V3. */
    public static final String CUSTOMER = "Cliente";

    private final Long id;
    private final String name;

    private UserType(Long id, String name) {
        this.id = id;
        this.name = validateName(name);
    }

    /** Cria um tipo novo, ainda sem id: o id nasce quando ele for salvo. */
    public static UserType create(String name) {
        return new UserType(null, name);
    }

    /**
     * Reconstrói um tipo que já existe, por exemplo ao ler do banco.
     * As regras são verificadas de novo: o domínio não confia em quem o chama.
     */
    public static UserType restore(Long id, String name) {
        Objects.requireNonNull(id, "Um tipo de usuário existente precisa de id.");
        return new UserType(id, name);
    }

    /** Devolve uma cópia deste tipo com o novo nome, mantendo o id. */
    public UserType rename(String newName) {
        return new UserType(this.id, newName);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("O nome do tipo de usuário é obrigatório.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new DomainValidationException(
                    "O nome do tipo de usuário deve ter no máximo " + NAME_MAX_LENGTH + " caracteres.");
        }
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserType other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
