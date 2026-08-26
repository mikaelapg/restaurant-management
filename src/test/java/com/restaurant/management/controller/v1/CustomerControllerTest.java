package com.restaurant.management.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.dto.request.AddressRequest;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateCustomerRequest;
import com.restaurant.management.dto.request.UpdateCustomerRequest;
import com.restaurant.management.dto.response.AddressResponse;
import com.restaurant.management.dto.response.CustomerResponse;
import com.restaurant.management.exception.EmailAlreadyExistsException;
import com.restaurant.management.exception.InvalidPasswordException;
import com.restaurant.management.exception.UserNotFoundException;
import com.restaurant.management.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web dos clientes: rotas, códigos de status e o corpo de erro
 * no formato RFC 7807. A regra de negócio em si fica nos testes de service.
 */
@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController")
class CustomerControllerTest {

    private static final String BASE = "/api/v1/customers";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private CustomerResponse sampleResponse() {
        return new CustomerResponse(
                2L,
                UserType.CUSTOMER,
                "Maria Oliveira",
                "maria@email.com",
                "maria.oliveira",
                LocalDateTime.of(2025, 8, 24, 21, 0),
                new AddressResponse("Rua das Flores", "123", "Apto 45", "São Paulo", "SP", "01001-000"),
                "123.456.789-00",
                LocalDate.of(1995, 7, 15)
        );
    }

    private CreateCustomerRequest validCreateRequest() {
        return new CreateCustomerRequest(
                "Maria Oliveira", "maria@email.com", "maria.oliveira", "Senha@456",
                new AddressRequest("Rua das Flores", "123", "Apto 45", "São Paulo", "SP", "01001-000"),
                "123.456.789-00", LocalDate.of(1995, 7, 15)
        );
    }

    @Test
    @DisplayName("POST devolve 201 e o cliente cadastrado, sem a senha")
    void shouldReturn201OnCreate() throws Exception {
        when(customerService.create(any(CreateCustomerRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.userType").value("CUSTOMER"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.lastModifiedAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST com campos inválidos devolve 400 e aponta cada campo")
    void shouldReturn400WhenPayloadIsInvalid() throws Exception {
        String payload = """
                {"name": "", "email": "email-invalido", "login": "", "password": "123"}""";

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Erro de Validação"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("POST com CPF fora do formato devolve 400 apontando o campo")
    void shouldReturn400WhenCpfFormatIsInvalid() throws Exception {
        String payload = """
                {"name": "Maria Silva", "email": "maria@email.com", "login": "maria.silva",
                 "password": "Senha@123", "cpf": "abc", "dateOfBirth": "1995-05-20"}""";

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de Validação"))
                .andExpect(jsonPath("$.fieldErrors.cpf").exists());
    }

    @Test
    @DisplayName("POST com data de nascimento no futuro devolve 400 apontando o campo")
    void shouldReturn400WhenDateOfBirthIsInTheFuture() throws Exception {
        String payload = """
                {"name": "Maria Silva", "email": "maria@email.com", "login": "maria.silva",
                 "password": "Senha@123", "cpf": "123.456.789-00", "dateOfBirth": "2090-01-01"}""";

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de Validação"))
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists());
    }

    @Test
    @DisplayName("POST com e-mail repetido devolve 409 no formato ProblemDetail")
    void shouldReturn409WhenEmailIsTaken() throws Exception {
        when(customerService.create(any(CreateCustomerRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("maria@email.com"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("E-mail Já Cadastrado"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("maria@email.com")));
    }

    @Test
    @DisplayName("GET sem filtro lista todos os clientes")
    void shouldListAllWhenNoNameFilter() throws Exception {
        when(customerService.findAll(null)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(customerService).findAll(null);
    }

    @Test
    @DisplayName("GET com name repassa o filtro para o service")
    void shouldForwardNameFilter() throws Exception {
        when(customerService.findAll("Maria")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get(BASE).param("name", "Maria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Maria Oliveira"));

        verify(customerService).findAll("Maria");
    }

    @Test
    @DisplayName("GET por id inexistente devolve 404 no formato ProblemDetail")
    void shouldReturn404WhenCustomerDoesNotExist() throws Exception {
        when(customerService.findById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Usuário Não Encontrado"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET com id não numérico devolve 400")
    void shouldReturn400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get(BASE + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parâmetro Inválido"));
    }

    @Test
    @DisplayName("PUT atualiza os dados e devolve 200")
    void shouldReturn200OnUpdate() throws Exception {
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Maria A. Oliveira", "maria@email.com", "maria.oliveira",
                null, "123.456.789-00", LocalDate.of(1995, 7, 15)
        );
        when(customerService.update(eq(2L), any(UpdateCustomerRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put(BASE + "/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(customerService).update(eq(2L), any(UpdateCustomerRequest.class));
    }

    @Test
    @DisplayName("PUT não aceita alterar a senha por este endpoint")
    void shouldIgnorePasswordSentToUpdateEndpoint() throws Exception {
        when(customerService.update(eq(2L), any(UpdateCustomerRequest.class))).thenReturn(sampleResponse());

        String payloadComSenha = """
                {
                  "name": "Maria A. Oliveira",
                  "email": "maria@email.com",
                  "login": "maria.oliveira",
                  "password": "TentandoTrocar@123"
                }""";

        mockMvc.perform(put(BASE + "/2").contentType(MediaType.APPLICATION_JSON).content(payloadComSenha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("PATCH de senha devolve 204 e não retorna corpo")
    void shouldReturn204OnPasswordChange() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Senha@456", "NovaSenha@789", "NovaSenha@789");
        doNothing().when(customerService).changePassword(eq(2L), any(ChangePasswordRequest.class));

        mockMvc.perform(patch(BASE + "/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(customerService).changePassword(eq(2L), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("PATCH com senha atual errada devolve 422")
    void shouldReturn422WhenCurrentPasswordIsWrong() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("errada", "NovaSenha@789", "NovaSenha@789");
        doThrow(new InvalidPasswordException("A senha atual informada está incorreta."))
                .when(customerService).changePassword(eq(2L), any(ChangePasswordRequest.class));

        mockMvc.perform(patch(BASE + "/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Senha Inválida"));
    }

    @Test
    @DisplayName("PATCH com nova senha curta demais devolve 400 antes de chegar no service")
    void shouldReturn400WhenNewPasswordIsTooShort() throws Exception {
        String payload = """
                {"currentPassword": "Senha@456", "newPassword": "123", "confirmNewPassword": "123"}""";

        mockMvc.perform(patch(BASE + "/2/password").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newPassword").exists());
    }

    @Test
    @DisplayName("DELETE devolve 204")
    void shouldReturn204OnDelete() throws Exception {
        doNothing().when(customerService).delete(2L);

        mockMvc.perform(delete(BASE + "/2"))
                .andExpect(status().isNoContent());

        verify(customerService).delete(2L);
    }

    @Test
    @DisplayName("Método HTTP não suportado devolve 405")
    void shouldReturn405ForUnsupportedMethod() throws Exception {
        mockMvc.perform(patch(BASE))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Método Não Permitido"));
    }
}
