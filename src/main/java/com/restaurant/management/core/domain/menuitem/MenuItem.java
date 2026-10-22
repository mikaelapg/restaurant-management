package com.restaurant.management.core.domain.menuitem;

import com.restaurant.management.core.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Item vendido no cardápio de um restaurante.
 * <p>
 * Imutável, como as demais entidades de domínio. O restaurante dono do item é
 * guardado pelo id e não muda: mover um prato de restaurante seria, na prática,
 * cadastrar outro item.
 */
public final class MenuItem {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final int PHOTO_PATH_MAX_LENGTH = 500;

    /** Maior preço aceito, espelhado na coluna {@code NUMERIC(10, 2)}. */
    public static final BigDecimal PRICE_MAX = new BigDecimal("99999999.99");

    private final Long id;
    private final Long restaurantId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final boolean dineInOnly;
    private final String photoPath;

    private MenuItem(Long id, Long restaurantId, String name, String description,
                     BigDecimal price, boolean dineInOnly, String photoPath) {
        if (restaurantId == null) {
            throw new DomainValidationException("O restaurante do item é obrigatório.");
        }
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = requiredText(name, "nome do item", NAME_MAX_LENGTH);
        this.description = requiredText(description, "descrição", DESCRIPTION_MAX_LENGTH);
        this.price = validatePrice(price);
        this.dineInOnly = dineInOnly;
        this.photoPath = optionalText(photoPath, "caminho da foto", PHOTO_PATH_MAX_LENGTH);
    }

    /** Cria um item novo, ainda sem id. */
    public static MenuItem create(Long restaurantId, String name, String description,
                                  BigDecimal price, boolean dineInOnly, String photoPath) {
        return new MenuItem(null, restaurantId, name, description, price, dineInOnly, photoPath);
    }

    /** Reconstrói um item que já existe, por exemplo ao ler do banco. */
    public static MenuItem restore(Long id, Long restaurantId, String name, String description,
                                   BigDecimal price, boolean dineInOnly, String photoPath) {
        Objects.requireNonNull(id, "Um item existente precisa de id.");
        return new MenuItem(id, restaurantId, name, description, price, dineInOnly, photoPath);
    }

    /** Devolve uma cópia com os dados novos, mantendo id e restaurante. */
    public MenuItem update(String name, String description, BigDecimal price, boolean dineInOnly, String photoPath) {
        return new MenuItem(this.id, this.restaurantId, name, description, price, dineInOnly, photoPath);
    }

    /** Preço positivo, com no máximo duas casas decimais; é guardado sempre com duas. */
    private static BigDecimal validatePrice(BigDecimal price) {
        if (price == null) {
            throw new DomainValidationException("O preço é obrigatório.");
        }
        if (price.signum() <= 0) {
            throw new DomainValidationException("O preço deve ser maior que zero.");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new DomainValidationException("O preço deve ter no máximo duas casas decimais.");
        }
        if (price.compareTo(PRICE_MAX) > 0) {
            throw new DomainValidationException("O preço deve ser no máximo " + PRICE_MAX + ".");
        }
        return price.setScale(2);
    }

    private static String requiredText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("O campo " + field + " é obrigatório.");
        }
        return checkLength(value.trim(), field, maxLength);
    }

    private static String optionalText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return checkLength(value.trim(), field, maxLength);
    }

    private static String checkLength(String value, String field, int maxLength) {
        if (value.length() > maxLength) {
            throw new DomainValidationException(
                    "O campo " + field + " deve ter no máximo " + maxLength + " caracteres.");
        }
        return value;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /** Indica se o item só pode ser pedido para consumo no restaurante (não vai para entrega). */
    public boolean isDineInOnly() {
        return dineInOnly;
    }

    /** Caminho de onde a foto do prato está armazenada; o arquivo em si não passa pela API. */
    public String getPhotoPath() {
        return photoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuItem other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
