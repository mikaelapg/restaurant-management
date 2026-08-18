package com.restaurant.management.controller.v1;

import com.restaurant.management.config.OpenApiExamples;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateRestaurantOwnerRequest;
import com.restaurant.management.dto.request.UpdateRestaurantOwnerRequest;
import com.restaurant.management.dto.response.RestaurantOwnerResponse;
import com.restaurant.management.service.RestaurantOwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cadastro e manutenção de donos de restaurante.
 * <p>
 * A atualização de dados e a troca de senha são endpoints distintos,
 * como pede o enunciado: {@code PUT /{id}} e {@code PATCH /{id}/password}.
 */
@RestController
@RequestMapping("/api/v1/restaurant-owners")
@RequiredArgsConstructor
@Tag(name = "Donos de Restaurante", description = "Gerenciamento de donos de restaurante")
public class RestaurantOwnerController {

    private final RestaurantOwnerService restaurantOwnerService;

    @PostMapping
    @Operation(summary = "Cadastrar dono de restaurante",
            description = "Cria um dono de restaurante. E-mail e login precisam ser únicos no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dono cadastrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantOwnerResponse.class),
                            examples = @ExampleObject(name = "Dono criado", value = OpenApiExamples.OWNER_CREATED))),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes ou fora do formato",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Erro de validação", value = OpenApiExamples.VALIDATION_ERROR))),
            @ApiResponse(responseCode = "409", description = "E-mail ou login já em uso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "E-mail duplicado", value = OpenApiExamples.EMAIL_CONFLICT)))
    })
    public ResponseEntity<RestaurantOwnerResponse> create(@Valid @RequestBody CreateRestaurantOwnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantOwnerService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar ou buscar donos pelo nome",
            description = "Sem o parâmetro `name`, devolve todos. Com ele, filtra pelo trecho informado, ignorando maiúsculas e minúsculas.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RestaurantOwnerResponse.class))))
    public ResponseEntity<List<RestaurantOwnerResponse>> findAll(
            @Parameter(description = "Trecho do nome para filtrar", example = "João")
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(restaurantOwnerService.findAll(name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar dono de restaurante por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dono encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantOwnerResponse.class),
                            examples = @ExampleObject(name = "Dono", value = OpenApiExamples.OWNER_CREATED))),
            @ApiResponse(responseCode = "404", description = "Nenhum dono com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND)))
    })
    public ResponseEntity<RestaurantOwnerResponse> findById(
            @Parameter(description = "Identificador do dono", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(restaurantOwnerService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados cadastrais do dono",
            description = "Não altera a senha. Para isso existe o `PATCH /{id}/password`. Endereço omitido é mantido como está.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantOwnerResponse.class),
                            examples = @ExampleObject(name = "Dono atualizado", value = OpenApiExamples.OWNER_CREATED))),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes ou fora do formato",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Erro de validação", value = OpenApiExamples.VALIDATION_ERROR))),
            @ApiResponse(responseCode = "404", description = "Nenhum dono com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND))),
            @ApiResponse(responseCode = "409", description = "Novo e-mail ou login já pertence a outro usuário",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "E-mail duplicado", value = OpenApiExamples.EMAIL_CONFLICT)))
    })
    public ResponseEntity<RestaurantOwnerResponse> update(
            @Parameter(description = "Identificador do dono", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantOwnerRequest request) {
        return ResponseEntity.ok(restaurantOwnerService.update(id, request));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Trocar a senha do dono de restaurante",
            description = "Endpoint exclusivo de senha. Exige a senha atual e a confirmação da nova.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Campos de senha ausentes ou muito curtos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Erro de validação", value = OpenApiExamples.VALIDATION_ERROR))),
            @ApiResponse(responseCode = "404", description = "Nenhum dono com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND))),
            @ApiResponse(responseCode = "422", description = "Senha atual incorreta ou confirmação divergente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Senha atual incorreta", value = OpenApiExamples.INVALID_PASSWORD)))
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Identificador do dono", example = "1") @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        restaurantOwnerService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir dono de restaurante")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dono excluído"),
            @ApiResponse(responseCode = "404", description = "Nenhum dono com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador do dono", example = "1") @PathVariable Long id) {
        restaurantOwnerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
