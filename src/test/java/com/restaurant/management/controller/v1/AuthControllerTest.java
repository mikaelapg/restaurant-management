package com.restaurant.management.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.dto.request.LoginRequest;
import com.restaurant.management.dto.response.LoginResponse;
import com.restaurant.management.exception.InvalidCredentialsException;
import com.restaurant.management.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web do login exigido pelo enunciado.
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController")
class AuthControllerTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("Credenciais válidas devolvem 200 e os dados do usuário")
    void shouldReturn200WhenCredentialsAreValid() throws Exception {
        LoginResponse response = new LoginResponse(
                1L, "João Silva", "joao.silva", "joao@restaurante.com",
                UserType.RESTAURANT_OWNER, "Login realizado com sucesso"
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("joao.silva", "Senha@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value("joao.silva"))
                .andExpect(jsonPath("$.userType").value("RESTAURANT_OWNER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Senha incorreta devolve 401 no formato ProblemDetail")
    void shouldReturn401WhenPasswordIsWrong() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("joao.silva", "errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Credenciais Inválidas"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("A mensagem de erro não revela se foi o login ou a senha")
    void shouldNotLeakWhichCredentialFailed() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("nao.existe", "Senha@123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Login ou senha inválidos. Por favor, verifique suas credenciais."));
    }

    @Test
    @DisplayName("Login e senha em branco devolvem 400 sem chamar o service")
    void shouldReturn400WhenFieldsAreBlank() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login": "", "password": ""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de Validação"))
                .andExpect(jsonPath("$.fieldErrors.login").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("JSON malformado devolve 400")
    void shouldReturn400WhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corpo da Requisição Inválido"));
    }
}
