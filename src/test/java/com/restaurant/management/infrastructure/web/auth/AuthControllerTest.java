package com.restaurant.management.infrastructure.web.auth;

import com.restaurant.management.core.application.auth.AuthenticateUserUseCase;
import com.restaurant.management.core.application.exception.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.restaurant.management.support.Fixtures.owner;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController")
class AuthControllerTest {

    private static final String URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticateUserUseCase authenticateUser;

    @Test
    @DisplayName("login aceito devolve os dados do usuário, o perfil e o tipo")
    void shouldLogin() throws Exception {
        when(authenticateUser.execute("joao.silva", "Senha@123")).thenReturn(owner(1L));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"joao.silva\", \"password\": \"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.profile").value("RESTAURANT_OWNER"))
                .andExpect(jsonPath("$.userType.name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso"));
    }

    @Test
    @DisplayName("credenciais inválidas respondem 401")
    void shouldRejectInvalidCredentials() throws Exception {
        when(authenticateUser.execute("joao.silva", "Errada@1")).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"joao.silva\", \"password\": \"Errada@1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Credenciais Inválidas"));
    }

    @Test
    @DisplayName("campos ausentes respondem 400; JSON malformado também")
    void shouldRejectInvalidBody() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.login").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{login"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Corpo da Requisição Inválido"));
        verifyNoInteractions(authenticateUser);
    }
}
