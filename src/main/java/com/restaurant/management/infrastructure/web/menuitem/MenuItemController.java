package com.restaurant.management.infrastructure.web.menuitem;

import com.restaurant.management.core.application.menuitem.CreateMenuItemUseCase;
import com.restaurant.management.core.application.menuitem.DeleteMenuItemUseCase;
import com.restaurant.management.core.application.menuitem.FindMenuItemByIdUseCase;
import com.restaurant.management.core.application.menuitem.ListMenuItemsUseCase;
import com.restaurant.management.core.application.menuitem.UpdateMenuItemUseCase;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.domain.menuitem.MenuItem;
import com.restaurant.management.infrastructure.web.shared.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * CRUD dos itens do cardápio.
 * <p>
 * As rotas ficam sob o restaurante porque o item não existe fora dele: o caminho já
 * diz de qual cardápio se trata, e um item de outro restaurante responde 404.
 */
@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
@Tag(name = "Cardápio", description = "Itens vendidos no cardápio de cada restaurante")
public class MenuItemController {

    private final CreateMenuItemUseCase createMenuItem;
    private final FindMenuItemByIdUseCase findMenuItemById;
    private final ListMenuItemsUseCase listMenuItems;
    private final UpdateMenuItemUseCase updateMenuItem;
    private final DeleteMenuItemUseCase deleteMenuItem;

    @PostMapping
    @Operation(summary = "Cadastrar item no cardápio", description = "O nome precisa ser único dentro do restaurante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item cadastrado; o header Location aponta para ele"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado"),
            @ApiResponse(responseCode = "409", description = "O restaurante já tem um item com esse nome")
    })
    public ResponseEntity<MenuItemResponse> create(
            @Parameter(description = "Id do restaurante", example = "1") @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        MenuItem created = createMenuItem.execute(restaurantId, request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(MenuItemResponse.fromDomain(created));
    }

    @GetMapping
    @Operation(summary = "Listar o cardápio do restaurante", description = "Paginado e em ordem alfabética.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<PageResponse<MenuItemResponse>> findAll(
            @Parameter(description = "Id do restaurante", example = "1") @PathVariable Long restaurantId,
            @Parameter(description = "Página, base zero", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página, de 1 a 100", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        var result = listMenuItems.execute(restaurantId, new PageQuery(page, size));
        return ResponseEntity.ok(PageResponse.of(result, MenuItemResponse::fromDomain));
    }

    @GetMapping("/{itemId}")
    @Operation(summary = "Buscar item do cardápio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado neste restaurante")
    })
    public ResponseEntity<MenuItemResponse> findById(@PathVariable Long restaurantId, @PathVariable Long itemId) {
        return ResponseEntity.ok(MenuItemResponse.fromDomain(findMenuItemById.execute(restaurantId, itemId)));
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Atualizar item do cardápio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado neste restaurante"),
            @ApiResponse(responseCode = "409", description = "O nome já pertence a outro item do restaurante")
    })
    public ResponseEntity<MenuItemResponse> update(@PathVariable Long restaurantId, @PathVariable Long itemId,
                                                   @Valid @RequestBody MenuItemRequest request) {
        MenuItem updated = updateMenuItem.execute(restaurantId, itemId, request.toCommand());
        return ResponseEntity.ok(MenuItemResponse.fromDomain(updated));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Excluir item do cardápio")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item excluído"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado neste restaurante")
    })
    public ResponseEntity<Void> delete(@PathVariable Long restaurantId, @PathVariable Long itemId) {
        deleteMenuItem.execute(restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }
}
