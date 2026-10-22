package com.restaurant.management.core.application.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageQuery e PageResult")
class PaginationTest {

    @Test
    @DisplayName("PageQuery ajusta página negativa e tamanho fora da faixa")
    void shouldClampPageQuery() {
        assertThat(new PageQuery(-1, 0)).isEqualTo(new PageQuery(0, PageQuery.DEFAULT_SIZE));
        assertThat(new PageQuery(2, 500).size()).isEqualTo(PageQuery.MAX_SIZE);
        assertThat(new PageQuery(3, 10)).extracting(PageQuery::page, PageQuery::size).containsExactly(3, 10);
    }

    @Test
    @DisplayName("PageResult calcula total de páginas e última página")
    void shouldComputeTotals() {
        PageResult<String> first = new PageResult<>(List.of("a", "b"), 0, 2, 5);
        PageResult<String> last = new PageResult<>(List.of("e"), 2, 2, 5);
        PageResult<String> empty = new PageResult<>(List.of(), 0, 20, 0);

        assertThat(first.totalPages()).isEqualTo(3);
        assertThat(first.isLast()).isFalse();
        assertThat(last.isLast()).isTrue();
        assertThat(empty.totalPages()).isZero();
        assertThat(empty.isLast()).isTrue();
        assertThat(new PageResult<>(List.of(), 0, 0, 0).totalPages()).isZero();
    }

    @Test
    @DisplayName("PageResult.map converte o conteúdo e mantém os metadados")
    void shouldMap() {
        PageResult<Integer> mapped = new PageResult<>(List.of("a", "bb"), 1, 2, 4).map(String::length);

        assertThat(mapped.content()).containsExactly(1, 2);
        assertThat(mapped).extracting(PageResult::page, PageResult::size, PageResult::totalElements)
                .containsExactly(1, 2, 4L);
    }
}
