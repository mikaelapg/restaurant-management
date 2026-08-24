package com.restaurant.management.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários — GlobalExceptionHandler (RFC 7807 ProblemDetail)")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request para MethodArgumentTypeMismatchException")
    void shouldHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", null, null
        );

        ProblemDetail problem = handler.handleTypeMismatch(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Parâmetro Inválido");
        assertThat(problem.getDetail()).contains("id", "abc", "Long");
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request para HttpMessageNotReadableException")
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");

        ProblemDetail problem = handler.handleMessageNotReadable(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Corpo da Requisição Inválido");
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized para InvalidCredentialsException")
    void shouldHandleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Credenciais inválidas.");

        ProblemDetail problem = handler.handleInvalidCredentials(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getTitle()).isEqualTo("Credenciais Inválidas");
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found para UserNotFoundException")
    void shouldHandleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException(42L);

        ProblemDetail problem = handler.handleUserNotFound(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Usuário Não Encontrado");
        assertThat(problem.getDetail()).contains("42");
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict para EmailAlreadyExistsException")
    void shouldHandleEmailAlreadyExistsException() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("teste@email.com");

        ProblemDetail problem = handler.handleEmailAlreadyExists(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("E-mail Já Cadastrado");
        assertThat(problem.getDetail()).contains("teste@email.com");
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict para LoginAlreadyExistsException")
    void shouldHandleLoginAlreadyExistsException() {
        LoginAlreadyExistsException ex = new LoginAlreadyExistsException("joao.silva");

        ProblemDetail problem = handler.handleLoginAlreadyExists(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Login Já Cadastrado");
        assertThat(problem.getDetail()).contains("joao.silva");
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict para DataIntegrityViolationException")
    void shouldHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Unique constraint violation");

        ProblemDetail problem = handler.handleDataIntegrityViolation(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Conflito de Dados");
    }

    @Test
    @DisplayName("Deve retornar 422 Unprocessable Entity para InvalidPasswordException")
    void shouldHandleInvalidPasswordException() {
        InvalidPasswordException ex = new InvalidPasswordException("Senha incorreta");

        ProblemDetail problem = handler.handleInvalidPassword(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(problem.getTitle()).isEqualTo("Senha Inválida");
    }

    @Test
    @DisplayName("Deve retornar 405 Method Not Allowed para HttpRequestMethodNotSupportedException")
    void shouldHandleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ProblemDetail problem = handler.handleMethodNotSupported(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(problem.getTitle()).isEqualTo("Método Não Permitido");
    }

    @Test
    @DisplayName("Deve retornar 500 Internal Server Error para exceções genéricas inesperadas")
    void shouldHandleGenericException() {
        NullPointerException ex = new NullPointerException("Erro inesperado");

        ProblemDetail problem = handler.handleGenericException(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Erro Interno do Servidor");
    }
}
