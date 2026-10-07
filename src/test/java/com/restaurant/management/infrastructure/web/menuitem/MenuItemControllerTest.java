package com.restaurant.management.infrastructure.web.menuitem;

import com.restaurant.management.core.application.exception.MenuItemAlreadyExistsException;
import com.restaurant.management.core.application.exception.MenuItemNotFoundException;
import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.menuitem.CreateMenuItemUseCase;
import com.restaurant.management.core.application.menuitem.DeleteMenuItemUseCase;
import com.restaurant.management.core.application.menuitem.FindMenuItemByIdUseCase;
import com.restaurant.management.core.application.menuitem.ListMenuItemsUseCase;
import com.restaurant.management.core.application.menuitem.MenuItemCommand;
import com.restaurant.management.core.application.menuitem.UpdateMenuItemUseCase;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.menuitem.MenuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(MenuItemController.class)
@DisplayName("MenuItemController")
class MenuItemControllerTest {

    private static final String BASE = "/api/v1/restaurants/1/menu-items";

    private static final String BODY = """
            {"name": "Risoto", "description": "Arroz arbóreo", "price": 68.90,
             "dineInOnly": true, "photoPath": "/images/1/risoto.jpg"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateMenuItemUseCase createMenuItem;
    @MockBean
    private FindMenuItemByIdUseCase findMenuItemById;
    @MockBean
    private ListMenuItemsUseCase listMenuItems;
    @MockBean
    private UpdateMenuItemUseCase updateMenuItem;
    @MockBean
    private DeleteMenuItemUseCase deleteMenuItem;

    private static MenuItem item(Long id) {
        return MenuItem.restore(id, 1L, "Risoto", "Arroz arbóreo", new BigDecimal("68.90"), true, "/images/1/risoto.jpg");
    }

    @Test
    @DisplayName("POST cria, responde 201 com Location aninhado ao restaurante")
    void shouldCreate() throws Exception {
        when(createMenuItem.execute(eq(1L), any())).thenReturn(item(5L));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/restaurants/1/menu-items/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.restaurantId").value(1))
                .andExpect(jsonPath("$.price").value(68.90))
                .andExpect(jsonPath("$.dineInOnly").value(true))
                .andExpect(jsonPath("$.photoPath").value("/images/1/risoto.jpg"));

        ArgumentCaptor<MenuItemCommand> captor = ArgumentCaptor.forClass(MenuItemCommand.class);
        verify(createMenuItem).execute(eq(1L), captor.capture());
        assertThat(captor.getValue().price()).isEqualByComparingTo("68.90");
        assertThat(captor.getValue().dineInOnly()).isTrue();
    }

    @Test
    @DisplayName("POST inválido responde 400 apontando cada campo, sem chamar o caso de uso")
    void shouldRejectInvalidBody() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\": 10.999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.dineInOnly").exists());

        verifyNoInteractions(createMenuItem);
    }

    @Test
    @DisplayName("POST com nome repetido responde 409")
    void shouldReturnConflict() throws Exception {
        when(createMenuItem.execute(eq(1L), any())).thenThrow(new MenuItemAlreadyExistsException("Risoto"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/menu-item-conflict"));
    }

    @Test
    @DisplayName("POST em restaurante inexistente responde 404")
    void shouldReturnNotFoundForRestaurant() throws Exception {
        when(createMenuItem.execute(eq(1L), any())).thenThrow(new RestaurantNotFoundException(1L));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/restaurant-not-found"));
    }

    @Test
    @DisplayName("GET lista o cardápio paginado")
    void shouldList() throws Exception {
        when(listMenuItems.execute(1L, new PageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(item(5L)), 0, 20, 1));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Risoto"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /{itemId} devolve o item; inexistente responde 404")
    void shouldFindById() throws Exception {
        when(findMenuItemById.execute(1L, 5L)).thenReturn(item(5L));
        when(findMenuItemById.execute(1L, 6L)).thenThrow(new MenuItemNotFoundException(1L, 6L));

        mockMvc.perform(get(BASE + "/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
        mockMvc.perform(get(BASE + "/6"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/menu-item-not-found"));
    }

    @Test
    @DisplayName("PUT /{itemId} atualiza")
    void shouldUpdate() throws Exception {
        when(updateMenuItem.execute(eq(1L), eq(5L), any())).thenReturn(item(5L));

        mockMvc.perform(put(BASE + "/5").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Risoto"));
    }

    @Test
    @DisplayName("DELETE /{itemId} responde 204; inexistente responde 404")
    void shouldDelete() throws Exception {
        doThrow(new MenuItemNotFoundException(1L, 6L)).when(deleteMenuItem).execute(1L, 6L);

        mockMvc.perform(delete(BASE + "/5")).andExpect(status().isNoContent());
        verify(deleteMenuItem).execute(1L, 5L);
        mockMvc.perform(delete(BASE + "/6")).andExpect(status().isNotFound());
    }
}
