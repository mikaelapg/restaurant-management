package com.restaurant.management.infrastructure.web.restaurantowner;

import com.restaurant.management.core.application.exception.UserOwnsRestaurantsException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.user.ChangePasswordUseCase;
import com.restaurant.management.core.application.user.CreateUserCommand;
import com.restaurant.management.core.application.user.CreateUserUseCase;
import com.restaurant.management.core.application.user.DeleteUserUseCase;
import com.restaurant.management.core.application.user.FindUserByIdUseCase;
import com.restaurant.management.core.application.user.ListUsersUseCase;
import com.restaurant.management.core.application.user.UpdateUserCommand;
import com.restaurant.management.core.application.user.UpdateUserUseCase;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.core.domain.user.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.restaurant.management.support.Fixtures.owner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantOwnerController.class)
@DisplayName("RestaurantOwnerController")
class RestaurantOwnerControllerTest {

    private static final String BASE = "/api/v1/restaurant-owners";

    private static final String BODY = """
            {
              "name": "João Silva",
              "email": "joao@restaurante.com",
              "login": "joao.silva",
              "password": "Senha@123",
              "restaurantName": "Grupo Silva",
              "cnpj": "12.345.678/0001-90"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateUserUseCase createUser;
    @MockBean
    private FindUserByIdUseCase findUserById;
    @MockBean
    private ListUsersUseCase listUsers;
    @MockBean
    private UpdateUserUseCase updateUser;
    @MockBean
    private ChangePasswordUseCase changePassword;
    @MockBean
    private DeleteUserUseCase deleteUser;

    @Test
    @DisplayName("POST cadastra o dono com o tipo \"Dono de Restaurante\" e responde 201 com Location")
    void shouldCreate() throws Exception {
        when(createUser.execute(any())).thenReturn(owner(1L));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/restaurant-owners/1"))
                .andExpect(jsonPath("$.profile").value("RESTAURANT_OWNER"))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"))
                .andExpect(jsonPath("$.restaurantName").value("Grupo Silva"))
                .andExpect(jsonPath("$.address.city").value("São Paulo"))
                .andExpect(jsonPath("$.userType.name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$.userType.profile").value("RESTAURANT_OWNER"));

        ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(createUser).execute(captor.capture());
        assertThat(captor.getValue().details()).isEqualTo(new RestaurantOwnerDetails("Grupo Silva", "12.345.678/0001-90"));
        assertThat(captor.getValue().address()).isNull();
    }

    @Test
    @DisplayName("POST com CNPJ fora do formato responde 400")
    void shouldRejectInvalidCnpj() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("12.345.678/0001-90", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.cnpj").exists());
    }

    @Test
    @DisplayName("GET lista só donos; GET /{id} busca exigindo o perfil de dono")
    void shouldRead() throws Exception {
        when(listUsers.execute("", UserProfile.RESTAURANT_OWNER, new PageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(owner(1L)), 0, 20, 1));
        when(listUsers.execute(null, UserProfile.RESTAURANT_OWNER, new PageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(owner(1L)), 0, 20, 1));
        when(findUserById.execute(1L, UserProfile.RESTAURANT_OWNER)).thenReturn(owner(1L));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].login").value("joao.silva"));
        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("João Silva"));
    }

    @Test
    @DisplayName("PUT atualiza com os dados do perfil de dono")
    void shouldUpdate() throws Exception {
        when(updateUser.execute(eq(1L), any())).thenReturn(owner(1L));
        String body = """
                {"name": "João P. Silva", "email": "joao@restaurante.com", "login": "joao.p",
                 "address": {"street": "Av. Paulista", "number": "1000", "city": "São Paulo",
                             "state": "SP", "zipCode": "01310-100"}}
                """;

        mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);
        verify(updateUser).execute(eq(1L), captor.capture());
        assertThat(captor.getValue().details()).isEqualTo(new RestaurantOwnerDetails(null, null));
        assertThat(captor.getValue().address().street()).isEqualTo("Av. Paulista");
    }

    @Test
    @DisplayName("PATCH troca a senha; DELETE de dono com restaurantes responde 409")
    void shouldChangePasswordAndRefuseDelete() throws Exception {
        String body = """
                {"currentPassword": "Senha@123", "newPassword": "NovaSenha@456", "confirmNewPassword": "NovaSenha@456"}
                """;
        doThrow(new UserOwnsRestaurantsException(1L)).when(deleteUser).execute(1L, UserProfile.RESTAURANT_OWNER);

        mockMvc.perform(patch(BASE + "/1/password").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());
        verify(changePassword).execute(1L, UserProfile.RESTAURANT_OWNER, "Senha@123", "NovaSenha@456", "NovaSenha@456");

        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-owns-restaurants"));
    }
}
