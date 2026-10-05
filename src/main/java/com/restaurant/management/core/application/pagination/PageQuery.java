package com.restaurant.management.core.application.pagination;

/**
 * Pedido de uma página: índice (base zero) e tamanho.
 * <p>
 * Existe para que os casos de uso e gateways não dependam do {@code Pageable} do Spring.
 * Valores fora da faixa são ajustados em vez de recusados, como faz o próprio Spring Data.
 */
public record PageQuery(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PageQuery {
        page = Math.max(page, 0);
        size = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }
}
