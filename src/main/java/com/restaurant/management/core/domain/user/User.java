package com.restaurant.management.core.domain.user;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.exception.IncompatibleUserTypeException;
import com.restaurant.management.core.domain.shared.Address;
import com.restaurant.management.core.domain.usertype.UserType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Usuário do sistema: cliente ou dono de restaurante.
 * <p>
 * Segue o desenho das demais entidades de domínio: Java puro e imutável. Toda alteração
 * devolve uma nova instância, que passa outra vez pelas validações.
 * <p>
 * Os dados próprios de cada perfil ficam em {@link ProfileDetails}, por composição, e não
 * em subclasses. O perfil é deduzido desses dados e não muda.
 * <p>
 * A senha chega aqui já como hash: gerar e conferir hash é trabalho da infraestrutura
 * (BCrypt), por trás da interface {@code PasswordHasher} da camada de aplicação.
 */
public final class User {

    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 255;
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final int LOGIN_MIN_LENGTH = 3;
    public static final int LOGIN_MAX_LENGTH = 100;

    /** Formato mínimo: algo, uma arroba, algo, sem espaços. A validação completa fica no DTO. */
    private static final Pattern EMAIL_FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+$");

    private final Long id;
    private final String name;
    private final String email;
    private final String login;
    private final String passwordHash;
    private final Address address;
    private final ProfileDetails details;
    private final UserType userType;
    private final LocalDateTime lastModifiedAt;

    private User(Long id, String name, String email, String login, String passwordHash, Address address,
                 ProfileDetails details, UserType userType, LocalDateTime lastModifiedAt) {
        this.id = id;
        this.name = text(name, "nome", NAME_MIN_LENGTH, NAME_MAX_LENGTH);
        this.email = validateEmail(email);
        this.login = text(login, "login", LOGIN_MIN_LENGTH, LOGIN_MAX_LENGTH);
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainValidationException("A senha do usuário é obrigatória.");
        }
        this.passwordHash = passwordHash;
        this.address = address;
        if (details == null) {
            throw new DomainValidationException("Os dados do perfil do usuário são obrigatórios.");
        }
        this.details = details;
        this.userType = checkCompatible(userType, details.profile());
        this.lastModifiedAt = lastModifiedAt;
    }

    /**
     * Cria um usuário novo, ainda sem id.
     *
     * @param address  endereço, opcional
     * @param userType tipo de usuário inicial; normalmente o tipo de sistema do perfil
     */
    public static User create(String name, String email, String login, String passwordHash,
                              Address address, ProfileDetails details, UserType userType) {
        return new User(null, name, email, login, passwordHash, address, details, userType, null);
    }

    /** Reconstrói um usuário que já existe, por exemplo ao ler do banco. */
    public static User restore(Long id, String name, String email, String login, String passwordHash,
                               Address address, ProfileDetails details, UserType userType,
                               LocalDateTime lastModifiedAt) {
        Objects.requireNonNull(id, "Um usuário existente precisa de id.");
        return new User(id, name, email, login, passwordHash, address, details, userType, lastModifiedAt);
    }

    /**
     * Devolve uma cópia com os dados cadastrais novos. Senha e tipo de usuário não mudam
     * aqui: cada um tem a sua operação.
     *
     * @throws DomainValidationException se os dados do perfil forem de outro perfil
     */
    public User updateData(String name, String email, String login, Address address, ProfileDetails details) {
        if (details != null && details.profile() != getProfile()) {
            throw new DomainValidationException("O perfil do usuário não pode ser alterado.");
        }
        return new User(id, name, email, login, passwordHash, address, details, userType, lastModifiedAt);
    }

    /** Devolve uma cópia com o novo hash de senha. */
    public User changePassword(String newPasswordHash) {
        return new User(id, name, email, login, newPasswordHash, address, details, userType, lastModifiedAt);
    }

    /**
     * Devolve uma cópia associada ao tipo informado.
     *
     * @throws IncompatibleUserTypeException se o tipo for de sistema e de outro perfil
     */
    public User assignType(UserType newType) {
        Objects.requireNonNull(newType, "O tipo de usuário é obrigatório.");
        return new User(id, name, email, login, passwordHash, address, details, newType, lastModifiedAt);
    }

    /**
     * Indica se o usuário pode ser responsável por restaurantes: precisa ter o tipo de
     * sistema dos donos ("Dono de Restaurante").
     */
    public boolean canOwnRestaurants() {
        return userType != null && userType.getProfile() == UserProfile.RESTAURANT_OWNER;
    }

    /**
     * Um tipo de sistema só pode ser dado a usuários do mesmo perfil: "Cliente" a clientes,
     * "Dono de Restaurante" a donos. Tipos criados pela API não têm perfil e servem a qualquer um.
     */
    private static UserType checkCompatible(UserType userType, UserProfile profile) {
        if (userType != null && userType.isSystemType() && userType.getProfile() != profile) {
            throw new IncompatibleUserTypeException("O tipo de usuário '" + userType.getName()
                    + "' é exclusivo do perfil " + userType.getProfile()
                    + " e não pode ser associado a um usuário do perfil " + profile + ".");
        }
        return userType;
    }

    private static String validateEmail(String email) {
        String value = text(email, "e-mail", 1, EMAIL_MAX_LENGTH).toLowerCase();
        if (!EMAIL_FORMAT.matcher(value).matches()) {
            throw new DomainValidationException("O e-mail deve ser válido.");
        }
        return value;
    }

    private static String text(String value, String field, int minLength, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("O campo " + field + " é obrigatório.");
        }
        String trimmed = value.trim();
        if (trimmed.length() < minLength || trimmed.length() > maxLength) {
            throw new DomainValidationException(
                    "O campo " + field + " deve ter entre " + minLength + " e " + maxLength + " caracteres.");
        }
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Sempre em minúsculas: e-mail não diferencia maiúsculas. */
    public String getEmail() {
        return email;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /** Endereço do usuário, ou {@code null} se não tiver sido informado. */
    public Address getAddress() {
        return address;
    }

    public ProfileDetails getDetails() {
        return details;
    }

    public UserProfile getProfile() {
        return details.profile();
    }

    /** Tipo de usuário associado, ou {@code null} se não houver. */
    public UserType getUserType() {
        return userType;
    }

    /** Data e hora da última alteração; {@code null} enquanto o usuário não tiver sido salvo. */
    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
