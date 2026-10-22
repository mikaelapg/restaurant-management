package com.restaurant.management.infrastructure.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Rede de segurança para exceções não previstas, no formato RFC 7807 (ProblemDetail).
 * <p>
 * Fica isolado do {@link GlobalExceptionHandler} e com a menor precedência de propósito. Como
 * {@code @ExceptionHandler(Exception.class)} casa com qualquer exceção, o Spring pararia neste
 * advice antes de consultar os demais — inclusive o {@code ProblemDetailsExceptionHandler} nativo,
 * que responde corretamente a casos como 415 e 406. Com {@code LOWEST_PRECEDENCE}, a ordem fica:
 * handlers específicos da aplicação, depois os padrão do Spring MVC, e só então este 500.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class UnhandledExceptionHandler {

    // ===========================
    // 500 Internal Server Error
    // ===========================

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Erro interno não tratado:", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Por favor, contate o suporte."
        );
        problem.setTitle("Erro Interno do Servidor");
        problem.setType(URI.create("https://restaurant-api.com/errors/internal-error"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
