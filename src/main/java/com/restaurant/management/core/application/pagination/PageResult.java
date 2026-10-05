package com.restaurant.management.core.application.pagination;

import java.util.List;
import java.util.function.Function;

/** Uma página de resultados e o total de itens em todas as páginas. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content);
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean isLast() {
        return page + 1 >= totalPages();
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements);
    }
}
