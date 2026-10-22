package com.restaurant.management.infrastructure.web.user;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.user.FindUserByIdUseCase;
import com.restaurant.management.core.application.user.ListUsersUseCase;
import com.restaurant.management.core.domain.user.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.restaurant.management.support.Fixtures.customer;
import static com.restaurant.management.support.Fixtures.owner;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("UserController")
class UserControllerTest {

    private static final String BASE = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListUsersUseCase listUsers;
    @MockBean
    private FindUserByIdUseCase findUserById;

    @Test
    @DisplayName("GET lista clientes e donos juntos")
    void shouldListAllProfiles() throws Exception {
        when(listUsers.execute("Silva", null, new PageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(owner(1L), customer(2L)), 0, 20, 2));

        mockMvc.perform(get(BASE).param("name", "Silva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].profile").value("RESTAURANT_OWNER"))
                .andExpect(jsonPath("$.content[0].userType.name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$.content[1].profile").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[1].cpf").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET filtra por perfil; perfil inválido responde 400")
    void shouldFilterByProfile() throws Exception {
        when(listUsers.execute(null, UserProfile.CUSTOMER, new PageQuery(1, 5)))
                .thenReturn(new PageResult<>(List.of(customer(2L)), 1, 5, 6));

        mockMvc.perform(get(BASE).param("profile", "CUSTOMER").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get(BASE).param("profile", "ADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{id} busca qualquer perfil e responde 404 quando não existe")
    void shouldFindById() throws Exception {
        when(findUserById.execute(1L)).thenReturn(owner(1L));
        when(findUserById.execute(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.street").value("Rua Augusta"));
        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("rota inexistente responde 404 no formato de erro da API")
    void shouldReturnNotFoundForUnknownRoute() throws Exception {
        mockMvc.perform(get("/api/v1/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"));
        verifyNoInteractions(listUsers, findUserById);
    }
}
