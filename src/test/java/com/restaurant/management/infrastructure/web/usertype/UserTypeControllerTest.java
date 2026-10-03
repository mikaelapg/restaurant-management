package com.restaurant.management.infrastructure.web.usertype;

import com.restaurant.management.core.application.exception.UserTypeAlreadyExistsException;
import com.restaurant.management.core.application.exception.UserTypeInUseException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.application.usertype.CreateUserTypeUseCase;
import com.restaurant.management.core.application.usertype.DeleteUserTypeUseCase;
import com.restaurant.management.core.application.usertype.FindUserTypeByIdUseCase;
import com.restaurant.management.core.application.usertype.ListUserTypesUseCase;
import com.restaurant.management.core.application.usertype.UpdateUserTypeUseCase;
import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.usertype.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa só a camada HTTP: rotas, status, JSON, validação e tradução de exceções.
 * Os casos de uso são mocks; a regra de negócio já está coberta nos testes deles.
 */
@WebMvcTest(UserTypeController.class)
@DisplayName("UserTypeController")
class UserTypeControllerTest {

    private static final String BASE = "/api/v1/user-types";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateUserTypeUseCase createUserType;
    @MockBean
    private FindUserTypeByIdUseCase findUserTypeById;
    @MockBean
    private ListUserTypesUseCase listUserTypes;
    @MockBean
    private UpdateUserTypeUseCase updateUserType;
    @MockBean
    private DeleteUserTypeUseCase deleteUserType;

    private static String body(String name) {
        return "{\"name\": \"" + name + "\"}";
    }

    @Test
    @DisplayName("POST cria, responde 201 e aponta o Location para o novo recurso")
    void shouldCreate() throws Exception {
        when(createUserType.execute("Cliente")).thenReturn(UserType.restore(1L, "Cliente"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Cliente")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/user-types/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cliente"));
    }

    @Test
    @DisplayName("POST com nome vazio responde 400 com o campo recusado, sem chamar o caso de uso")
    void shouldRejectBlankName() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verifyNoInteractions(createUserType);
    }

    @Test
    @DisplayName("POST com nome repetido responde 409")
    void shouldReturnConflict() throws Exception {
        when(createUserType.execute(anyString())).thenThrow(new UserTypeAlreadyExistsException("Cliente"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Cliente")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-conflict"));
    }

    @Test
    @DisplayName("regra de domínio violada vira 400 no formato ProblemDetail")
    void shouldMapDomainValidationTo400() throws Exception {
        when(createUserType.execute(anyString())).thenThrow(new DomainValidationException("Nome inválido."));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("x")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/domain-validation"))
                .andExpect(jsonPath("$.detail").value("Nome inválido."));
    }

    @Test
    @DisplayName("GET lista os tipos")
    void shouldList() throws Exception {
        when(listUserTypes.execute()).thenReturn(List.of(
                UserType.restore(2L, "Cliente"), UserType.restore(1L, "Dono de Restaurante")));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cliente"))
                .andExpect(jsonPath("$[1].name").value("Dono de Restaurante"));
    }

    @Test
    @DisplayName("GET /{id} devolve o tipo")
    void shouldFindById() throws Exception {
        when(findUserTypeById.execute(1L)).thenReturn(UserType.restore(1L, "Cliente"));

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cliente"));
    }

    @Test
    @DisplayName("GET /{id} inexistente responde 404")
    void shouldReturnNotFound() throws Exception {
        when(findUserTypeById.execute(99L)).thenThrow(new UserTypeNotFoundException(99L));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-not-found"));
    }

    @Test
    @DisplayName("PUT /{id} atualiza e devolve o tipo")
    void shouldUpdate() throws Exception {
        when(updateUserType.execute(1L, "Consumidor")).thenReturn(UserType.restore(1L, "Consumidor"));

        mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(body("Consumidor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Consumidor"));
    }

    @Test
    @DisplayName("DELETE /{id} responde 204")
    void shouldDelete() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());

        verify(deleteUserType).execute(1L);
    }

    @Test
    @DisplayName("DELETE /{id} inexistente responde 404")
    void shouldReturnNotFoundOnDelete() throws Exception {
        doThrow(new UserTypeNotFoundException(99L)).when(deleteUserType).execute(99L);

        mockMvc.perform(delete(BASE + "/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("DELETE /{id} de tipo em uso responde 409")
    void shouldReturnConflictWhenInUse() throws Exception {
        doThrow(new UserTypeInUseException(1L)).when(deleteUserType).execute(1L);

        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-in-use"));
    }
}
