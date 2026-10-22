package com.restaurant.management.infrastructure.web.auth;

import com.restaurant.management.core.application.auth.AuthenticateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Validação de credenciais.
 * <p>
 * Sem Spring Security nem token: a checagem é feita contra o banco, comparando a senha
 * com o hash BCrypt gravado.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Validação de credenciais de acesso")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUser;

    @PostMapping("/login")
    @Operation(summary = "Validar login", description = "Confere login e senha e devolve os dados do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais válidas"),
            @ApiResponse(responseCode = "400", description = "Login ou senha não informados"),
            @ApiResponse(responseCode = "401", description = "Login inexistente ou senha incorreta")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(LoginResponse.fromDomain(authenticateUser.execute(request.login(), request.password())));
    }
}
