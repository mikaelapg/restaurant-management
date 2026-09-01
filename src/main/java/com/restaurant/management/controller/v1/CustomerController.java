package com.restaurant.management.controller.v1;

import com.restaurant.management.config.OpenApiExamples;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateCustomerRequest;
import com.restaurant.management.dto.request.UpdateCustomerRequest;
import com.restaurant.management.dto.response.CustomerResponse;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
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

/**
 * Cadastro e manutenção de clientes.
 * <p>
 * A atualização de dados e a troca de senha são endpoints distintos,
 * como pede o enunciado: {@code PUT /{id}} e {@code PATCH /{id}/password}.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes do sistema")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Cadastrar cliente",
            description = "Cria um cliente. E-mail e login precisam ser únicos no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class),
                            examples = @ExampleObject(name = "Cliente criado", value = OpenApiExamples.CUSTOMER_CREATED))),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes ou fora do formato",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Erro de validação", value = OpenApiExamples.VALIDATION_ERROR))),
            @ApiResponse(responseCode = "409", description = "E-mail ou login já em uso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "E-mail duplicado", value = OpenApiExamples.EMAIL_CONFLICT)))
    })
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar ou buscar clientes pelo nome",
            description = "Sem o parâmetro `name`, devolve todos. Com ele, filtra pelo trecho informado, ignorando maiúsculas e minúsculas. "
                    + "O resultado é paginado: use `page` (base zero), `size` e `sort` (ex.: `sort=name,asc`).")
    @ApiResponse(responseCode = "200", description = "Consulta realizada",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)))
    public ResponseEntity<PageResponse<CustomerResponse>> findAll(
            @Parameter(description = "Trecho do nome para filtrar", example = "Maria")
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(customerService.findAll(name, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class),
                            examples = @ExampleObject(name = "Cliente", value = OpenApiExamples.CUSTOMER_CREATED))),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND)))
    })
    public ResponseEntity<CustomerResponse> findById(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados cadastrais do cliente",
            description = "Não altera a senha. Para isso existe o `PATCH /{id}/password`. Endereço omitido é mantido como está.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class),
                            examples = @ExampleObject(name = "Cliente atualizado", value = OpenApiExamples.CUSTOMER_CREATED))),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes ou fora do formato",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Erro de validação", value = OpenApiExamples.VALIDATION_ERROR))),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND))),
            @ApiResponse(responseCode = "409", description = "Novo e-mail ou login já pertence a outro usuário",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "E-mail duplicado", value = OpenApiExamples.EMAIL_CONFLICT)))
    })
    public ResponseEntity<CustomerResponse> update(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Trocar a senha do cliente",
            description = "Endpoint exclusivo de senha. Exige a senha atual e a confirmação da nova.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Campos de senha ausentes ou muito curtos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Erro de validação", value = OpenApiExamples.VALIDATION_ERROR))),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND))),
            @ApiResponse(responseCode = "422", description = "Senha atual incorreta ou confirmação divergente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Senha atual incorreta", value = OpenApiExamples.INVALID_PASSWORD)))
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        customerService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ProblemDetail"),
                            examples = @ExampleObject(name = "Não encontrado", value = OpenApiExamples.USER_NOT_FOUND)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
