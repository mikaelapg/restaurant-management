package com.restaurant.management.infrastructure.web.restaurant;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.restaurant.CreateRestaurantUseCase;
import com.restaurant.management.core.application.restaurant.DeleteRestaurantUseCase;
import com.restaurant.management.core.application.restaurant.FindRestaurantByIdUseCase;
import com.restaurant.management.core.application.restaurant.ListRestaurantsUseCase;
import com.restaurant.management.core.application.restaurant.UpdateRestaurantUseCase;
import com.restaurant.management.core.domain.restaurant.Restaurant;
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

/** CRUD de restaurantes. Só traduz HTTP; as regras ficam nos casos de uso e no domínio. */
@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurantes", description = "Cadastro de restaurantes")
public class RestaurantController {

    private final CreateRestaurantUseCase createRestaurant;
    private final FindRestaurantByIdUseCase findRestaurantById;
    private final ListRestaurantsUseCase listRestaurants;
    private final UpdateRestaurantUseCase updateRestaurant;
    private final DeleteRestaurantUseCase deleteRestaurant;

    @PostMapping
    @Operation(summary = "Cadastrar restaurante",
            description = "O dono precisa ser um usuário existente com o tipo de usuário \"Dono de Restaurante\".")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurante cadastrado; o header Location aponta para ele"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos ou horários de abertura e fechamento iguais"),
            @ApiResponse(responseCode = "404", description = "O dono informado não existe"),
            @ApiResponse(responseCode = "422", description = "O usuário informado não é dono de restaurante")
    })
    public ResponseEntity<RestaurantResponse> create(@Valid @RequestBody RestaurantRequest request) {
        Restaurant created = createRestaurant.execute(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(RestaurantResponse.fromDomain(created));
    }

    @GetMapping
    @Operation(summary = "Listar restaurantes",
            description = "Paginado e em ordem alfabética. Filtros opcionais por trecho do nome e do tipo de cozinha, "
                    + "sem diferenciar maiúsculas.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada")
    public ResponseEntity<PageResponse<RestaurantResponse>> findAll(
            @Parameter(description = "Trecho do nome", example = "Cantina")
            @RequestParam(required = false) String name,
            @Parameter(description = "Trecho do tipo de cozinha", example = "Italiana")
            @RequestParam(required = false) String cuisineType,
            @Parameter(description = "Página, base zero", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página, de 1 a 100", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        var result = listRestaurants.execute(name, cuisineType, new PageQuery(page, size));
        return ResponseEntity.ok(PageResponse.of(result, RestaurantResponse::fromDomain));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar restaurante por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurante encontrado"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<RestaurantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(RestaurantResponse.fromDomain(findRestaurantById.execute(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar restaurante",
            description = "Substitui todos os dados, inclusive o dono, que segue a mesma regra do cadastro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurante atualizado"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos"),
            @ApiResponse(responseCode = "404", description = "Restaurante ou dono não encontrado"),
            @ApiResponse(responseCode = "422", description = "O usuário informado não é dono de restaurante")
    })
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(RestaurantResponse.fromDomain(updateRestaurant.execute(id, request.toCommand())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir restaurante", description = "Os itens do cardápio do restaurante são excluídos junto.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Restaurante excluído"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRestaurant.execute(id);
        return ResponseEntity.noContent().build();
    }
}
