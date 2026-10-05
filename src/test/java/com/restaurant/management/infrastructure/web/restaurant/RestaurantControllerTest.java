package com.restaurant.management.infrastructure.web.restaurant;

import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.restaurant.CreateRestaurantUseCase;
import com.restaurant.management.core.application.restaurant.DeleteRestaurantUseCase;
import com.restaurant.management.core.application.restaurant.FindRestaurantByIdUseCase;
import com.restaurant.management.core.application.restaurant.ListRestaurantsUseCase;
import com.restaurant.management.core.application.restaurant.RestaurantCommand;
import com.restaurant.management.core.application.restaurant.UpdateRestaurantUseCase;
import com.restaurant.management.core.domain.restaurant.OpeningHours;
import com.restaurant.management.core.domain.restaurant.Restaurant;
import com.restaurant.management.core.domain.shared.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(RestaurantController.class)
@DisplayName("RestaurantController")
class RestaurantControllerTest {

    private static final String BASE = "/api/v1/restaurants";

    private static final String BODY = """
            {
              "name": "Cantina da Nonna",
              "address": {"street": "Rua Augusta", "number": "1500", "complement": "Loja 2",
                          "city": "São Paulo", "state": "SP", "zipCode": "01305-100"},
              "cuisineType": "Italiana",
              "openingHours": {"opensAt": "18:00", "closesAt": "02:00"},
              "ownerId": 1
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateRestaurantUseCase createRestaurant;
    @MockBean
    private FindRestaurantByIdUseCase findRestaurantById;
    @MockBean
    private ListRestaurantsUseCase listRestaurants;
    @MockBean
    private UpdateRestaurantUseCase updateRestaurant;
    @MockBean
    private DeleteRestaurantUseCase deleteRestaurant;

    private static Restaurant restaurant(Long id) {
        return Restaurant.restore(id, "Cantina da Nonna",
                new Address("Rua Augusta", "1500", "Loja 2", "São Paulo", "SP", "01305-100"),
                "Italiana", new OpeningHours(LocalTime.of(18, 0), LocalTime.of(2, 0)), 1L);
    }

    @Test
    @DisplayName("POST cria, responde 201 com Location e devolve horários em HH:mm")
    void shouldCreate() throws Exception {
        when(createRestaurant.execute(any())).thenReturn(restaurant(7L));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/restaurants/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.address.complement").value("Loja 2"))
                .andExpect(jsonPath("$.openingHours.opensAt").value("18:00"))
                .andExpect(jsonPath("$.openingHours.closesAt").value("02:00"))
                .andExpect(jsonPath("$.ownerId").value(1));

        ArgumentCaptor<RestaurantCommand> captor = ArgumentCaptor.forClass(RestaurantCommand.class);
        verify(createRestaurant).execute(captor.capture());
        assertThat(captor.getValue().openingHours().crossesMidnight()).isTrue();
        assertThat(captor.getValue().address().city()).isEqualTo("São Paulo");
    }

    @Test
    @DisplayName("POST com campos ausentes responde 400 apontando cada um, inclusive os aninhados")
    void shouldRejectInvalidBody() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\": {}, \"openingHours\": {}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.cuisineType").exists())
                .andExpect(jsonPath("$.fieldErrors.ownerId").exists())
                .andExpect(jsonPath("$.fieldErrors['address.street']").exists())
                .andExpect(jsonPath("$.fieldErrors['openingHours.opensAt']").exists());

        verifyNoInteractions(createRestaurant);
    }

    @Test
    @DisplayName("POST com abertura igual ao fechamento responde 400 de regra de domínio")
    void shouldRejectEqualHours() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("02:00", "18:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/domain-validation"));

        verifyNoInteractions(createRestaurant);
    }

    @Test
    @DisplayName("POST com horário fora do formato responde 400 de JSON malformado")
    void shouldRejectBadTime() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("02:00", "25:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/malformed-json"));
    }

    @Test
    @DisplayName("POST com dono inexistente responde 404 user-not-found")
    void shouldReturnNotFoundForOwner() throws Exception {
        when(createRestaurant.execute(any())).thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-not-found"));
    }

    @Test
    @DisplayName("GET lista paginada com filtros")
    void shouldList() throws Exception {
        when(listRestaurants.execute(eq("cantina"), eq("ital"), eq(new PageQuery(1, 5))))
                .thenReturn(new PageResult<>(List.of(restaurant(7L)), 1, 5, 6));

        mockMvc.perform(get(BASE).param("name", "cantina").param("cuisineType", "ital")
                        .param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("GET /{id} devolve o restaurante")
    void shouldFindById() throws Exception {
        when(findRestaurantById.execute(7L)).thenReturn(restaurant(7L));

        mockMvc.perform(get(BASE + "/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cantina da Nonna"));
    }

    @Test
    @DisplayName("GET /{id} inexistente responde 404 restaurant-not-found")
    void shouldReturnNotFound() throws Exception {
        when(findRestaurantById.execute(99L)).thenThrow(new RestaurantNotFoundException(99L));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/restaurant-not-found"));
    }

    @Test
    @DisplayName("PUT /{id} atualiza")
    void shouldUpdate() throws Exception {
        when(updateRestaurant.execute(eq(7L), any())).thenReturn(restaurant(7L));

        mockMvc.perform(put(BASE + "/7").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("DELETE /{id} responde 204")
    void shouldDelete() throws Exception {
        mockMvc.perform(delete(BASE + "/7"))
                .andExpect(status().isNoContent());

        verify(deleteRestaurant).execute(7L);
    }

    @Test
    @DisplayName("DELETE /{id} inexistente responde 404")
    void shouldReturnNotFoundOnDelete() throws Exception {
        doThrow(new RestaurantNotFoundException(99L)).when(deleteRestaurant).execute(99L);

        mockMvc.perform(delete(BASE + "/99"))
                .andExpect(status().isNotFound());
    }
}
