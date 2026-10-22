package com.restaurant.management.infrastructure.web.shared;

import com.restaurant.management.core.application.pagination.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope de página para os endpoints de listagem.
 * <p>
 * Expõe só o que o consumidor precisa: o conteúdo e os metadados de paginação, com um
 * contrato JSON que não depende da versão do Spring.
 */
@Schema(description = "Página de resultados com metadados de paginação")
public record PageResponse<T>(
        @Schema(description = "Itens da página atual")
        List<T> content,

        @Schema(description = "Número da página atual (base zero)", example = "0")
        int page,

        @Schema(description = "Tamanho da página solicitado", example = "20")
        int size,

        @Schema(description = "Total de elementos em todas as páginas", example = "42")
        long totalElements,

        @Schema(description = "Total de páginas", example = "3")
        int totalPages,

        @Schema(description = "Indica se esta é a última página", example = "false")
        boolean last
) {

    /** Converte uma página da camada de aplicação em um {@code PageResponse} de DTOs. */
    public static <E, D> PageResponse<D> of(PageResult<E> page, Function<E, D> mapper) {
        return new PageResponse<>(
                page.content().stream().map(mapper).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.isLast()
        );
    }
}
