package com.restaurant.management.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateRestaurantOwnerRequest;
import com.restaurant.management.dto.response.AddressResponse;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.RestaurantOwnerResponse;
import com.restaurant.management.exception.UserNotFoundException;
import com.restaurant.management.service.RestaurantOwnerService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web dos donos de restaurante. Cobre o que é próprio desse
 * subtipo; o comportamento comum de erro já é verificado em CustomerControllerTest.
 */
@WebMvcTest(RestaurantOwnerController.class)
@DisplayName("RestaurantOwnerController")
class RestaurantOwnerControllerTest {

    private static final String BASE = "/api/v1/restaurant-owners";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    private RestaurantOwnerResponse sampleResponse() {
        return new RestaurantOwnerResponse(
                1L,
                UserType.RESTAURANT_OWNER,
                "João Silva",
                "joao@restaurante.com",
                "joao.silva",
                LocalDateTime.of(2025, 8, 24, 21, 0),
                new AddressResponse("Av. Paulista", "1000", "Conjunto 50", "São Paulo", "SP", "01310-100"),
                "Restaurante do João",
                "12.345.678/0001-90",
                new UserTypeResponse(1L, "Dono de Restaurante")
        );
    }

    @Test
    @DisplayName("POST devolve 201 com o restaurante e o CNPJ cadastrados")
    void shouldReturn201OnCreate() throws Exception {
        CreateRestaurantOwnerRequest request = new CreateRestaurantOwnerRequest(
                "João Silva", "joao@restaurante.com", "joao.silva", "Senha@123",
                null, "Restaurante do João", "12.345.678/0001-90"
        );
        when(restaurantOwnerService.create(any(CreateRestaurantOwnerRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType").value("RESTAURANT_OWNER"))
                .andExpect(jsonPath("$.restaurantName").value("Restaurante do João"))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET com name busca donos pelo nome, em um envelope de página")
    void shouldSearchByName() throws Exception {
        List<RestaurantOwnerResponse> content = List.of(sampleResponse());
        when(restaurantOwnerService.findAll(eq("João"), any(Pageable.class)))
                .thenReturn(new PageResponse<>(content, 0, 20, 1, 1, true));

        mockMvc.perform(get(BASE).param("name", "João"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("João Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(restaurantOwnerService).findAll(eq("João"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET por id inexistente devolve 404")
    void shouldReturn404WhenOwnerDoesNotExist() throws Exception {
        when(restaurantOwnerService.findById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Usuário Não Encontrado"));
    }

    @Test
    @DisplayName("A troca de senha tem rota própria e devolve 204")
    void shouldChangePasswordOnItsOwnRoute() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Senha@123", "NovaSenha@456", "NovaSenha@456");
        doNothing().when(restaurantOwnerService).changePassword(eq(1L), any(ChangePasswordRequest.class));

        mockMvc.perform(patch(BASE + "/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(restaurantOwnerService).changePassword(eq(1L), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("POST sem os campos obrigatórios devolve 400")
    void shouldReturn400WhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "email": "nao-e-email", "login": "", "password": "1"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de Validação"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
