package com.restaurant.management.controller.v1;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.UserResponse;
import com.restaurant.management.exception.UserNotFoundException;
import com.restaurant.management.service.UserService;
import com.restaurant.management.infrastructure.web.usertype.UserTypeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da busca unificada, que é o caminho que atende a exigência de
 * "buscar usuários pelo nome" sem separar por tipo.
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController")
class UserControllerTest {

    private static final String BASE = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserResponse owner() {
        return new UserResponse(1L, UserType.RESTAURANT_OWNER, "João Silva",
                "joao@restaurante.com", "joao.silva", LocalDateTime.of(2025, 8, 24, 21, 0), null,
                new UserTypeResponse(1L, "Dono de Restaurante"));
    }

    private UserResponse customer() {
        return new UserResponse(2L, UserType.CUSTOMER, "Maria Silva",
                "maria@email.com", "maria.silva", LocalDateTime.of(2025, 8, 24, 21, 5), null,
                new UserTypeResponse(2L, "Cliente"));
    }

    private PageResponse<UserResponse> pageOf(UserResponse... items) {
        List<UserResponse> content = List.of(items);
        return new PageResponse<>(content, 0, 20, content.size(), 1, true);
    }

    @Test
    @DisplayName("GET sem filtro devolve os dois tipos de usuário, em um envelope de página")
    void shouldListBothUserTypes() throws Exception {
        when(userService.findAll(eq(null), any(Pageable.class))).thenReturn(pageOf(owner(), customer()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userType").value("RESTAURANT_OWNER"))
                .andExpect(jsonPath("$.content[1].userType").value("CUSTOMER"))
                .andExpect(jsonPath("$.content[0].assignedUserType.name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$.content[1].assignedUserType.name").value("Cliente"));

        verify(userService).findAll(eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("GET com name filtra pelo trecho informado")
    void shouldSearchByName() throws Exception {
        when(userService.findAll(eq("Silva"), any(Pageable.class))).thenReturn(pageOf(owner(), customer()));

        mockMvc.perform(get(BASE).param("name", "Silva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(userService).findAll(eq("Silva"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET por id devolve o usuário sem expor a senha")
    void shouldReturnUserById() throws Exception {
        when(userService.findById(1L)).thenReturn(owner());

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET por id inexistente devolve 404 no formato ProblemDetail")
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        when(userService.findById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Usuário Não Encontrado"))
                .andExpect(jsonPath("$.detail").value("Usuário não encontrado com o ID: 99"));
    }
}
