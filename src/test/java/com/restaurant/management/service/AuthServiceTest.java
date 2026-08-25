package com.restaurant.management.service;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.request.LoginRequest;
import com.restaurant.management.dto.response.LoginResponse;
import com.restaurant.management.exception.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários — AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Autenticação de Usuário (Login)")
    class LoginTests {

        @Test
        @DisplayName("Deve autenticar com sucesso quando as credenciais estiverem corretas")
        void shouldAuthenticateSuccessfully_WhenCredentialsAreValid() {
            // Arrange
            LoginRequest request = new LoginRequest("maria.silva", "Senha@123");
            User user = Customer.builder()
                    .name("Maria Silva")
                    .email("maria@email.com")
                    .login("maria.silva")
                    .password("$2a$10$hashedPassword")
                    .build();
            user.setId(1L);

            when(userRepository.findByLoginIgnoreCase("maria.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("Senha@123", "$2a$10$hashedPassword")).thenReturn(true);

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.userType()).isEqualTo(UserType.CUSTOMER);
            assertThat(response.name()).isEqualTo("Maria Silva");
            assertThat(response.email()).isEqualTo("maria@email.com");
            assertThat(response.login()).isEqualTo("maria.silva");
            assertThat(response.message()).isEqualTo("Login realizado com sucesso");

            verify(userRepository).findByLoginIgnoreCase("maria.silva");
            verify(passwordEncoder).matches("Senha@123", "$2a$10$hashedPassword");
        }

        @Test
        @DisplayName("Deve lançar InvalidCredentialsException quando o login não existir")
        void shouldThrowInvalidCredentialsException_WhenLoginDoesNotExist() {
            // Arrange
            LoginRequest request = new LoginRequest("usuario.inexistente", "Senha@123");
            when(userRepository.findByLoginIgnoreCase("usuario.inexistente")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidCredentialsException quando a senha estiver incorreta")
        void shouldThrowInvalidCredentialsException_WhenPasswordIsIncorrect() {
            // Arrange
            LoginRequest request = new LoginRequest("maria.silva", "SenhaErrada");
            User user = Customer.builder()
                    .name("Maria Silva")
                    .email("maria@email.com")
                    .login("maria.silva")
                    .password("$2a$10$hashedPassword")
                    .build();

            when(userRepository.findByLoginIgnoreCase("maria.silva")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("SenhaErrada", "$2a$10$hashedPassword")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
