package com.restaurant.management.core.domain.usertype;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.user.UserProfile;

import java.util.Objects;

/**
 * Tipo de usuário do sistema, como "Dono de Restaurante" ou "Cliente".
 * <p>
 * Entidade de domínio: Java puro, sem anotações de JPA nem de Spring. Ela não sabe
 * que existe banco de dados ou HTTP; só conhece as próprias regras.
 * <p>
 * Há dois tipos <em>de sistema</em>, criados pelas migrations: "Dono de Restaurante" e
 * "Cliente". Cada um está ligado a um {@link UserProfile}, e é essa ligação, e não o
 * nome, que o sistema usa para saber quem pode ser dono de restaurante. Por isso eles
 * podem ser renomeados, mas não excluídos. Os tipos criados pela API não têm perfil.
 * <p>
 * É imutável: renomear devolve uma nova instância em vez de alterar esta. Assim
 * nenhuma instância chega a existir em estado inválido, porque toda criação passa
 * pela validação do nome.
 */
public final class UserType {

    /** Tamanho máximo do nome, espelhado na coluna {@code user_types.name}. */
    public static final int NAME_MAX_LENGTH = 50;

    /** Nome inicial do tipo de sistema dos donos de restaurante. */
    public static final String RESTAURANT_OWNER = "Dono de Restaurante";

    /** Nome inicial do tipo de sistema dos clientes. */
    public static final String CUSTOMER = "Cliente";

    private final Long id;
    private final String name;
    private final UserProfile profile;

    private UserType(Long id, String name, UserProfile profile) {
        this.id = id;
        this.name = validateName(name);
        this.profile = profile;
    }

    /** Cria um tipo novo, ainda sem id: o id nasce quando ele for salvo. Tipos criados pela API não são de sistema. */
    public static UserType create(String name) {
        return new UserType(null, name, null);
    }

    /**
     * Reconstrói um tipo que já existe, por exemplo ao ler do banco.
     * As regras são verificadas de novo: o domínio não confia em quem o chama.
     *
     * @param profile perfil ligado a um tipo de sistema, ou {@code null} para os demais
     */
    public static UserType restore(Long id, String name, UserProfile profile) {
        Objects.requireNonNull(id, "Um tipo de usuário existente precisa de id.");
        return new UserType(id, name, profile);
    }

    /** Reconstrói um tipo que não é de sistema. */
    public static UserType restore(Long id, String name) {
        return restore(id, name, null);
    }

    /** Devolve uma cópia deste tipo com o novo nome, mantendo o id e o perfil. */
    public UserType rename(String newName) {
        return new UserType(this.id, newName, this.profile);
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

    /** Perfil ao qual o tipo de sistema está ligado, ou {@code null} se não for de sistema. */
    public UserProfile getProfile() {
        return profile;
    }

    /** Indica se é um dos tipos criados pelas migrations ("Dono de Restaurante" ou "Cliente"). */
    public boolean isSystemType() {
        return profile != null;
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
