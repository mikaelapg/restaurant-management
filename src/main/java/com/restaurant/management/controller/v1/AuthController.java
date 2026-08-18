package com.restaurant.management.controller.v1;

import com.restaurant.management.config.OpenApiExamples;
import com.restaurant.management.dto.request.LoginRequest;
import com.restaurant.management.dto.response.LoginResponse;
import com.restaurant.management.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * O enunciado não exige Spring Security, então a checagem é feita direto
 * contra o banco, comparando a senha com o hash BCrypt gravado.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Validação de credenciais de acesso ao sistema")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Validar login",
            description = "Confere login e senha e devolve os dados do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais válidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(name = "Login aceito", value = OpenApiExamples.LOGIN_SUCCESS))),
            @ApiResponse(responseCode = "400", description = "Login ou senha não informados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Campos obrigatórios", value = OpenApiExamples.VALIDATION_ERROR))),
            @ApiResponse(responseCode = "401", description = "Login inexistente ou senha incorreta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Credenciais inválidas", value = OpenApiExamples.INVALID_CREDENTIALS)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
