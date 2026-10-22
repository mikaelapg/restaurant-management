package com.restaurant.management.infrastructure.web.user;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.user.FindUserByIdUseCase;
import com.restaurant.management.core.application.user.ListUsersUseCase;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.web.shared.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * Consulta de usuários sem distinguir o perfil.
 * <p>
 * Atende a busca por nome sem obrigar quem consulta a saber de antemão se a pessoa é
 * cliente ou dona de restaurante.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários (Geral)", description = "Consultas de usuários de qualquer perfil")
public class UserController {

    private final ListUsersUseCase listUsers;
    private final FindUserByIdUseCase findUserById;

    @GetMapping
    @Operation(summary = "Listar ou buscar usuários pelo nome",
            description = "Paginado e em ordem alfabética. Filtros opcionais por trecho do nome, sem diferenciar "
                    + "maiúsculas, e por perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada"),
            @ApiResponse(responseCode = "400", description = "Perfil inválido")
    })
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @Parameter(description = "Trecho do nome", example = "Silva")
            @RequestParam(required = false) String name,
            @Parameter(description = "Perfil: CUSTOMER ou RESTAURANT_OWNER")
            @RequestParam(required = false) UserProfile profile,
            @Parameter(description = "Página, base zero", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página, de 1 a 100", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        var result = listUsers.execute(name, profile, new PageQuery(page, size));
        return ResponseEntity.ok(PageResponse.of(result, UserResponse::fromDomain));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por id, seja ele cliente ou dono de restaurante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponse> findById(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.fromDomain(findUserById.execute(id)));
    }
}
