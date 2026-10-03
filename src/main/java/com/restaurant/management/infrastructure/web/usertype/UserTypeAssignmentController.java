package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.application.usertype.AssignUserTypeUseCase;
import com.restaurant.management.dto.response.UserResponse;
import com.restaurant.management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Associação entre usuários e tipos de usuário.
 * <p>
 * Fica sob {@code /users/{id}} porque o que muda é o usuário: o tipo é um atributo dele.
 * A resposta é o usuário já com o tipo novo, na mesma forma de {@code GET /users/{id}}.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/user-type")
@RequiredArgsConstructor
@Tag(name = "Tipos de usuário", description = "Cadastro dos tipos de usuário (ex.: Dono de Restaurante, Cliente)")
public class UserTypeAssignmentController {

    private final AssignUserTypeUseCase assignUserType;
    private final UserService userService;

    @PutMapping
    @Operation(summary = "Associar usuário a um tipo",
            description = "Substitui o tipo atual do usuário. Clientes e donos já nascem com o tipo correspondente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Associação feita; devolve o usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "userTypeId ausente"),
            @ApiResponse(responseCode = "404", description = "Usuário ou tipo de usuário não encontrado")
    })
    public ResponseEntity<UserResponse> assign(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long userId,
            @Valid @RequestBody AssignUserTypeRequest request) {
        assignUserType.execute(userId, request.userTypeId());
        return ResponseEntity.ok(userService.findById(userId));
    }
}
