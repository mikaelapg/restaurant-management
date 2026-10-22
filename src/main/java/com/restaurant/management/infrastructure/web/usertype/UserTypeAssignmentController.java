package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.application.usertype.AssignUserTypeUseCase;
import com.restaurant.management.infrastructure.web.user.UserResponse;
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

    @PutMapping
    @Operation(summary = "Associar usuário a um tipo",
            description = "Substitui o tipo atual do usuário. Clientes e donos já nascem com o tipo de sistema do "
                    + "seu perfil. Um tipo de sistema só pode ser dado a usuários do mesmo perfil, e quem é "
                    + "responsável por restaurantes precisa continuar com o tipo de dono.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Associação feita; devolve o usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "userTypeId ausente"),
            @ApiResponse(responseCode = "404", description = "Usuário ou tipo de usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "O usuário é dono de restaurantes e perderia o tipo de dono"),
            @ApiResponse(responseCode = "422", description = "Tipo de sistema de outro perfil (ex.: \"Cliente\" para um dono)")
    })
    public ResponseEntity<UserResponse> assign(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long userId,
            @Valid @RequestBody AssignUserTypeRequest request) {
        return ResponseEntity.ok(UserResponse.fromDomain(assignUserType.execute(userId, request.userTypeId())));
    }
}
