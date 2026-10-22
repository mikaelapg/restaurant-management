package com.restaurant.management.infrastructure.web.exception;

import com.restaurant.management.core.application.exception.CnpjAlreadyExistsException;
import com.restaurant.management.core.application.exception.CpfAlreadyExistsException;
import com.restaurant.management.core.application.exception.EmailAlreadyExistsException;
import com.restaurant.management.core.application.exception.InvalidCredentialsException;
import com.restaurant.management.core.application.exception.InvalidPasswordException;
import com.restaurant.management.core.application.exception.InvalidRestaurantOwnerException;
import com.restaurant.management.core.application.exception.LoginAlreadyExistsException;
import com.restaurant.management.core.application.exception.MenuItemAlreadyExistsException;
import com.restaurant.management.core.application.exception.MenuItemNotFoundException;
import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.exception.SystemUserTypeException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserOwnsRestaurantsException;
import com.restaurant.management.core.application.exception.UserTypeAlreadyExistsException;
import com.restaurant.management.core.application.exception.UserTypeChangeNotAllowedException;
import com.restaurant.management.core.application.exception.UserTypeInUseException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.exception.IncompatibleUserTypeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cada exceção vira o status e o {@code type} certos. Os tratadores são chamados direto,
 * sem Spring: o que se testa aqui é a tradução, não o roteamento do MVC (que os testes de
 * controller cobrem).
 */
@DisplayName("GlobalExceptionHandler (RFC 7807)")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static void assertProblem(Supplier<ProblemDetail> call, HttpStatus status, String type, String title) {
        ProblemDetail problem = call.get();
        assertThat(problem.getStatus()).isEqualTo(status.value());
        assertThat(problem.getType()).isEqualTo(URI.create(GlobalExceptionHandler.TYPE_BASE + type));
        assertThat(problem.getTitle()).isEqualTo(title);
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("400: tipo de parâmetro, JSON malformado e regra de domínio")
    void shouldMapBadRequests() {
        ProblemDetail mismatch = handler.handleTypeMismatch(
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null));
        assertThat(mismatch.getStatus()).isEqualTo(400);
        assertThat(mismatch.getDetail()).contains("id", "abc", "Long");

        assertProblem(() -> handler.handleMessageNotReadable(new HttpMessageNotReadableException("x", null, null)),
                HttpStatus.BAD_REQUEST, "malformed-json", "Corpo da Requisição Inválido");
        assertProblem(() -> handler.handleDomainValidation(new DomainValidationException("regra")),
                HttpStatus.BAD_REQUEST, "domain-validation", "Regra de Domínio Violada");
    }

    @Test
    @DisplayName("401 e 405")
    void shouldMapUnauthorizedAndMethodNotAllowed() {
        assertProblem(() -> handler.handleInvalidCredentials(new InvalidCredentialsException()),
                HttpStatus.UNAUTHORIZED, "invalid-credentials", "Credenciais Inválidas");
        ProblemDetail method = handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("PATCH"));
        assertThat(method.getStatus()).isEqualTo(405);
        assertThat(method.getDetail()).contains("PATCH");
    }

    @Test
    @DisplayName("404: usuário, tipo, restaurante e item do cardápio")
    void shouldMapNotFound() {
        assertProblem(() -> handler.handleUserNotFound(new UserNotFoundException(42L)),
                HttpStatus.NOT_FOUND, "user-not-found", "Usuário Não Encontrado");
        assertProblem(() -> handler.handleUserTypeNotFound(new UserTypeNotFoundException(1L)),
                HttpStatus.NOT_FOUND, "user-type-not-found", "Tipo de Usuário Não Encontrado");
        assertProblem(() -> handler.handleRestaurantNotFound(new RestaurantNotFoundException(1L)),
                HttpStatus.NOT_FOUND, "restaurant-not-found", "Restaurante Não Encontrado");
        assertProblem(() -> handler.handleMenuItemNotFound(new MenuItemNotFoundException(1L, 2L)),
                HttpStatus.NOT_FOUND, "menu-item-not-found", "Item do Cardápio Não Encontrado");
    }

    @Test
    @DisplayName("409: duplicidades")
    void shouldMapDuplicates() {
        assertProblem(() -> handler.handleEmailAlreadyExists(new EmailAlreadyExistsException("a@b.com")),
                HttpStatus.CONFLICT, "email-conflict", "E-mail Já Cadastrado");
        assertProblem(() -> handler.handleLoginAlreadyExists(new LoginAlreadyExistsException("joao")),
                HttpStatus.CONFLICT, "login-conflict", "Login Já Cadastrado");
        assertProblem(() -> handler.handleCpfAlreadyExists(new CpfAlreadyExistsException("123.456.789-00")),
                HttpStatus.CONFLICT, "cpf-conflict", "CPF Já Cadastrado");
        assertProblem(() -> handler.handleCnpjAlreadyExists(new CnpjAlreadyExistsException("12.345.678/0001-90")),
                HttpStatus.CONFLICT, "cnpj-conflict", "CNPJ Já Cadastrado");
        assertProblem(() -> handler.handleUserTypeAlreadyExists(new UserTypeAlreadyExistsException("Cliente")),
                HttpStatus.CONFLICT, "user-type-conflict", "Tipo de Usuário Já Cadastrado");
        assertProblem(() -> handler.handleMenuItemAlreadyExists(new MenuItemAlreadyExistsException("Risoto")),
                HttpStatus.CONFLICT, "menu-item-conflict", "Item do Cardápio Já Cadastrado");
    }

    @Test
    @DisplayName("409: regras de integridade entre usuários, tipos e restaurantes")
    void shouldMapIntegrityRules() {
        assertProblem(() -> handler.handleUserTypeInUse(new UserTypeInUseException(1L)),
                HttpStatus.CONFLICT, "user-type-in-use", "Tipo de Usuário em Uso");
        assertProblem(() -> handler.handleSystemUserType(new SystemUserTypeException(1L, "Cliente")),
                HttpStatus.CONFLICT, "system-user-type", "Tipo de Usuário de Sistema");
        assertProblem(() -> handler.handleUserOwnsRestaurants(new UserOwnsRestaurantsException(1L)),
                HttpStatus.CONFLICT, "user-owns-restaurants", "Usuário É Dono de Restaurantes");
        assertProblem(() -> handler.handleUserTypeChangeNotAllowed(new UserTypeChangeNotAllowedException(1L)),
                HttpStatus.CONFLICT, "user-type-change-not-allowed", "Troca de Tipo Não Permitida");
        assertProblem(() -> handler.handleDataIntegrityViolation(new DataIntegrityViolationException("uk")),
                HttpStatus.CONFLICT, "data-integrity-conflict", "Conflito de Dados");
    }

    @Test
    @DisplayName("422: senha, dono de restaurante e tipo incompatível")
    void shouldMapUnprocessable() {
        assertProblem(() -> handler.handleInvalidPassword(new InvalidPasswordException("errada")),
                HttpStatus.UNPROCESSABLE_ENTITY, "invalid-password", "Senha Inválida");
        assertProblem(() -> handler.handleInvalidRestaurantOwner(new InvalidRestaurantOwnerException(5L, "Cliente")),
                HttpStatus.UNPROCESSABLE_ENTITY, "invalid-restaurant-owner", "Usuário Não É Dono de Restaurante");
        assertProblem(() -> handler.handleIncompatibleUserType(new IncompatibleUserTypeException("x")),
                HttpStatus.UNPROCESSABLE_ENTITY, "incompatible-user-type", "Tipo de Usuário Incompatível");
    }
}
