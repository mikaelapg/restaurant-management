package com.restaurant.management.infrastructure.web.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes Unitários — UnhandledExceptionHandler (RFC 7807 ProblemDetail)")
class UnhandledExceptionHandlerTest {

    private UnhandledExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UnhandledExceptionHandler();
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
