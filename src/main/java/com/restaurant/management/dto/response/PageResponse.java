package com.restaurant.management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope de página para os endpoints de listagem.
 * <p>
 * Expõe apenas o que o consumidor precisa — o conteúdo e os metadados de
 * paginação — sem serializar a estrutura interna do {@link Page} do Spring Data,
 * cujo contrato JSON não é estável entre versões.
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

    /** Converte um {@link Page} de entidades em um {@code PageResponse} de DTOs. */
    public static <E, D> PageResponse<D> of(Page<E> page, Function<E, D> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
