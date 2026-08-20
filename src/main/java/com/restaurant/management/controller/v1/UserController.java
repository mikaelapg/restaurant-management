package com.restaurant.management.controller.v1;

import com.restaurant.management.config.OpenApiExamples;
import com.restaurant.management.dto.response.UserResponse;
import com.restaurant.management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta de usuários sem distinguir o subtipo.
 * <p>
 * Existe para atender a busca por nome do enunciado: quem consulta não precisa
 * saber de antemão se a pessoa é cliente ou dona de restaurante.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários (Geral)", description = "Consultas consolidadas de usuários do sistema")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Listar ou buscar usuários pelo nome",
            description = "Sem o parâmetro `name`, devolve todos os usuários. Com ele, filtra pelo trecho informado, ignorando maiúsculas e minúsculas."
    )
    @ApiResponse(responseCode = "200", description = "Consulta realizada",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)),
                    examples = @ExampleObject(name = "Clientes e donos", value = OpenApiExamples.USER_LIST)))
    public ResponseEntity<List<UserResponse>> findAll(
            @Parameter(description = "Trecho do nome para filtrar", example = "Silva")
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(userService.findAll(name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por id, seja ele cliente ou dono de restaurante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND)))
    })
    public ResponseEntity<UserResponse> findById(
            @Parameter(description = "Identificador do usuário", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
