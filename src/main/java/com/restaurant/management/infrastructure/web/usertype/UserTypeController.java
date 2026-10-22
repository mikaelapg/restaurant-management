package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.application.usertype.CreateUserTypeUseCase;
import com.restaurant.management.core.application.usertype.DeleteUserTypeUseCase;
import com.restaurant.management.core.application.usertype.FindUserTypeByIdUseCase;
import com.restaurant.management.core.application.usertype.ListUserTypesUseCase;
import com.restaurant.management.core.application.usertype.UpdateUserTypeUseCase;
import com.restaurant.management.core.domain.usertype.UserType;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * CRUD de tipos de usuário.
 * <p>
 * O controller só traduz HTTP: recebe o DTO, chama o caso de uso e converte o
 * resultado em resposta. Nenhuma regra de negócio mora aqui.
 */
@RestController
@RequestMapping("/api/v1/user-types")
@RequiredArgsConstructor
@Tag(name = "Tipos de usuário", description = "Cadastro dos tipos de usuário (ex.: Dono de Restaurante, Cliente)")
public class UserTypeController {

    private final CreateUserTypeUseCase createUserType;
    private final FindUserTypeByIdUseCase findUserTypeById;
    private final ListUserTypesUseCase listUserTypes;
    private final UpdateUserTypeUseCase updateUserType;
    private final DeleteUserTypeUseCase deleteUserType;

    @PostMapping
    @Operation(summary = "Cadastrar tipo de usuário", description = "O nome precisa ser único, sem diferenciar maiúsculas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tipo cadastrado; o header Location aponta para ele"),
            @ApiResponse(responseCode = "400", description = "Nome vazio ou com mais de 50 caracteres"),
            @ApiResponse(responseCode = "409", description = "Já existe um tipo com esse nome")
    })
    public ResponseEntity<UserTypeResponse> create(@Valid @RequestBody UserTypeRequest request) {
        UserType created = createUserType.execute(request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(UserTypeResponse.fromDomain(created));
    }

    @GetMapping
    @Operation(summary = "Listar tipos de usuário", description = "Devolve todos, em ordem alfabética.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada")
    public ResponseEntity<List<UserTypeResponse>> findAll() {
        List<UserTypeResponse> body = listUserTypes.execute().stream()
                .map(UserTypeResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de usuário por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado")
    })
    public ResponseEntity<UserTypeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UserTypeResponse.fromDomain(findUserTypeById.execute(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de usuário",
            description = "Renomeia o tipo. Os tipos de sistema também podem ser renomeados e continuam ligados ao perfil.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo atualizado"),
            @ApiResponse(responseCode = "400", description = "Nome vazio ou com mais de 50 caracteres"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado"),
            @ApiResponse(responseCode = "409", description = "O nome já pertence a outro tipo")
    })
    public ResponseEntity<UserTypeResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UserTypeRequest request) {
        return ResponseEntity.ok(UserTypeResponse.fromDomain(updateUserType.execute(id, request.name())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tipo de usuário",
            description = "Os tipos de sistema (\"Dono de Restaurante\" e \"Cliente\") e os tipos associados a "
                    + "algum usuário não podem ser excluídos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tipo excluído"),
            @ApiResponse(responseCode = "404", description = "Tipo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Tipo de sistema ou associado a usuários")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUserType.execute(id);
        return ResponseEntity.noContent().build();
    }
}
