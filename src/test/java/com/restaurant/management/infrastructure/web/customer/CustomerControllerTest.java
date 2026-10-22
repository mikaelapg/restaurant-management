package com.restaurant.management.infrastructure.web.customer;

import com.restaurant.management.core.application.exception.EmailAlreadyExistsException;
import com.restaurant.management.core.application.exception.InvalidPasswordException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
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
import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.core.domain.user.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.restaurant.management.support.Fixtures.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController")
class CustomerControllerTest {

    private static final String BASE = "/api/v1/customers";

    private static final String CREATE_BODY = """
            {
              "name": "Maria Oliveira",
              "email": "maria@email.com",
              "login": "maria.oliveira",
              "password": "Senha@456",
              "address": {"street": "Rua Augusta", "number": "1500", "city": "São Paulo",
                          "state": "SP", "zipCode": "01305-100"},
              "cpf": "123.456.789-00",
              "dateOfBirth": "1995-07-15"
            }
            """;

    private static final String UPDATE_BODY = """
            {"name": "Maria A. Oliveira", "email": "maria@email.com", "login": "maria.oliveira",
             "cpf": "123.456.789-00"}
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
    @DisplayName("POST cadastra, responde 201 com Location, sem senha e com o tipo \"Cliente\"")
    void shouldCreate() throws Exception {
        when(createUser.execute(any())).thenReturn(customer(2L));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/customers/2"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.profile").value("CUSTOMER"))
                .andExpect(jsonPath("$.cpf").value("123.456.789-00"))
                .andExpect(jsonPath("$.dateOfBirth").value("1995-07-15"))
                .andExpect(jsonPath("$.userType.name").value("Cliente"))
                .andExpect(jsonPath("$.userType.systemType").value(true))
                .andExpect(jsonPath("$.lastModifiedAt").value("2026-10-22T10:00:00"))
                .andExpect(jsonPath("$.password").doesNotExist());

        ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(createUser).execute(captor.capture());
        assertThat(captor.getValue().password()).isEqualTo("Senha@456");
        assertThat(captor.getValue().address().city()).isEqualTo("São Paulo");
        assertThat(captor.getValue().details())
                .isEqualTo(new CustomerDetails("123.456.789-00", LocalDate.of(1995, 7, 15)));
    }

    @Test
    @DisplayName("POST com senha fraca, CPF fora do formato e endereço incompleto responde 400 apontando os campos")
    void shouldRejectInvalidBody() throws Exception {
        String body = """
                {"name": "Maria", "email": "maria@email.com", "login": "maria", "password": "fraca",
                 "cpf": "123", "address": {"street": "Rua A"}}
                """;

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/validation-error"))
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.cpf").exists())
                .andExpect(jsonPath("$.fieldErrors['address.city']").exists());
        verifyNoInteractions(createUser);
    }

    @Test
    @DisplayName("POST com e-mail de outro usuário responde 409")
    void shouldReturnConflict() throws Exception {
        when(createUser.execute(any())).thenThrow(new EmailAlreadyExistsException("maria@email.com"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("E-mail Já Cadastrado"));
    }

    @Test
    @DisplayName("GET lista só clientes, repassando nome e paginação")
    void shouldList() throws Exception {
        when(listUsers.execute("Maria", UserProfile.CUSTOMER, new PageQuery(0, 5)))
                .thenReturn(new PageResult<>(List.of(customer(2L)), 0, 5, 1));

        mockMvc.perform(get(BASE).param("name", "Maria").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Maria Oliveira"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("GET /{id} busca exigindo o perfil de cliente; outro perfil responde 404")
    void shouldFindById() throws Exception {
        when(findUserById.execute(2L, UserProfile.CUSTOMER)).thenReturn(customer(2L));
        when(findUserById.execute(1L, UserProfile.CUSTOMER)).thenThrow(new UserNotFoundException(1L));

        mockMvc.perform(get(BASE + "/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").doesNotExist());
        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Usuário Não Encontrado"));
    }

    @Test
    @DisplayName("PUT atualiza; sem endereço no corpo, o comando leva endereço nulo (manter)")
    void shouldUpdate() throws Exception {
        when(updateUser.execute(eq(2L), any())).thenReturn(customer(2L));

        mockMvc.perform(put(BASE + "/2").contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);
        verify(updateUser).execute(eq(2L), captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Maria A. Oliveira");
        assertThat(captor.getValue().address()).isNull();
        assertThat(captor.getValue().details().profile()).isEqualTo(UserProfile.CUSTOMER);
    }

    @Test
    @DisplayName("PATCH /{id}/password troca a senha e responde 204; senha atual errada responde 422")
    void shouldChangePassword() throws Exception {
        String body = """
                {"currentPassword": "Senha@456", "newPassword": "NovaSenha@789", "confirmNewPassword": "NovaSenha@789"}
                """;

        mockMvc.perform(patch(BASE + "/2/password").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());
        verify(changePassword).execute(2L, UserProfile.CUSTOMER, "Senha@456", "NovaSenha@789", "NovaSenha@789");

        doThrow(new InvalidPasswordException("A senha atual informada está incorreta."))
                .when(changePassword).execute(3L, UserProfile.CUSTOMER, "Senha@456", "NovaSenha@789", "NovaSenha@789");
        mockMvc.perform(patch(BASE + "/3/password").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Senha Inválida"));
    }

    @Test
    @DisplayName("DELETE exclui exigindo o perfil de cliente")
    void shouldDelete() throws Exception {
        mockMvc.perform(delete(BASE + "/2"))
                .andExpect(status().isNoContent());

        verify(deleteUser).execute(2L, UserProfile.CUSTOMER);
    }

    @Test
    @DisplayName("id que não é número responde 400")
    void shouldRejectNonNumericId() throws Exception {
        mockMvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetro Inválido"));
    }
}
