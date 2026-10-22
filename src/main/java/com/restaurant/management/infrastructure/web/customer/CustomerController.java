package com.restaurant.management.infrastructure.web.customer;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.user.ChangePasswordUseCase;
import com.restaurant.management.core.application.user.CreateUserUseCase;
import com.restaurant.management.core.application.user.DeleteUserUseCase;
import com.restaurant.management.core.application.user.FindUserByIdUseCase;
import com.restaurant.management.core.application.user.ListUsersUseCase;
import com.restaurant.management.core.application.user.UpdateUserUseCase;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.web.shared.ChangePasswordRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
 * Cadastro e manutenção de clientes. Só traduz HTTP; as regras ficam nos casos de uso.
 * <p>
 * Os casos de uso de usuário servem aos dois perfis. Aqui eles sempre recebem o perfil
 * {@link UserProfile#CUSTOMER}: um id de dono de restaurante responde 404 nestas rotas.
 * A atualização de dados e a troca de senha são endpoints distintos.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Cadastro e manutenção de clientes")
public class CustomerController {

    private static final UserProfile PROFILE = UserProfile.CUSTOMER;

    private final CreateUserUseCase createUser;
    private final FindUserByIdUseCase findUserById;
    private final ListUsersUseCase listUsers;
    private final UpdateUserUseCase updateUser;
    private final ChangePasswordUseCase changePassword;
    private final DeleteUserUseCase deleteUser;

    @PostMapping
    @Operation(summary = "Cadastrar cliente",
            description = "E-mail, login e CPF precisam ser únicos. O cliente já nasce com o tipo de usuário \"Cliente\".")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado; o header Location aponta para ele"),
            @ApiResponse(responseCode = "400", description = "Campos ausentes ou fora do formato"),
            @ApiResponse(responseCode = "409", description = "E-mail, login ou CPF já em uso")
    })
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        User created = createUser.execute(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(CustomerResponse.fromDomain(created));
    }

    @GetMapping
    @Operation(summary = "Listar ou buscar clientes pelo nome",
            description = "Paginado e em ordem alfabética. Filtro opcional por trecho do nome, sem diferenciar maiúsculas.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada")
    public ResponseEntity<PageResponse<CustomerResponse>> findAll(
            @Parameter(description = "Trecho do nome", example = "Maria")
            @RequestParam(required = false) String name,
            @Parameter(description = "Página, base zero", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página, de 1 a 100", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        var result = listUsers.execute(name, PROFILE, new PageQuery(page, size));
        return ResponseEntity.ok(PageResponse.of(result, CustomerResponse::fromDomain));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id")
    })
    public ResponseEntity<CustomerResponse> findById(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id) {
        return ResponseEntity.ok(CustomerResponse.fromDomain(findUserById.execute(id, PROFILE)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados cadastrais do cliente",
            description = "Não altera a senha: para isso existe o PATCH /{id}/password. Endereço omitido é mantido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados"),
            @ApiResponse(responseCode = "400", description = "Campos ausentes ou fora do formato"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id"),
            @ApiResponse(responseCode = "409", description = "E-mail, login ou CPF já pertence a outro usuário")
    })
    public ResponseEntity<CustomerResponse> update(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(CustomerResponse.fromDomain(updateUser.execute(id, request.toCommand())));
    }

    @PatchMapping("/{id}/password")
    @Operation(summary = "Trocar a senha do cliente", description = "Exige a senha atual e a confirmação da nova.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Campos ausentes ou nova senha fraca"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id"),
            @ApiResponse(responseCode = "422", description = "Senha atual incorreta ou confirmação divergente")
    })
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        changePassword.execute(id, PROFILE, request.currentPassword(), request.newPassword(),
                request.confirmNewPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído"),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com esse id")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador do cliente", example = "2") @PathVariable Long id) {
        deleteUser.execute(id, PROFILE);
        return ResponseEntity.noContent().build();
    }
}
