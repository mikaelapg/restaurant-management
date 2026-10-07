package com.restaurant.management.exception;

import com.restaurant.management.core.application.exception.MenuItemAlreadyExistsException;
import com.restaurant.management.core.application.exception.MenuItemNotFoundException;
import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserOwnsRestaurantsException;
import com.restaurant.management.core.application.exception.UserTypeAlreadyExistsException;
import com.restaurant.management.core.application.exception.UserTypeInUseException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.domain.exception.DomainValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Tratamento global de exceções com retorno padronizado no formato RFC 7807 (ProblemDetail).
 * <p>
 * A precedência máxima é necessária porque {@code spring.mvc.problemdetails.enabled} registra o
 * {@code ProblemDetailsExceptionHandler} do próprio Spring com ordem 0. Sem o {@code @Order}, este
 * advice ficaria em {@code LOWEST_PRECEDENCE} e o handler nativo responderia primeiro às exceções
 * padrão do MVC (validação, JSON malformado, tipo de parâmetro e método não suportado), devolvendo
 * {@code "type": "about:blank"} e títulos em inglês no lugar dos definidos aqui.
 * <p>
 * O tratamento genérico de {@code Exception} fica em {@link UnhandledExceptionHandler}, com a menor
 * precedência: se estivesse nesta classe, capturaria tudo antes do Spring e transformaria em 500
 * respostas que o handler nativo trata corretamente (415, 406, parâmetro obrigatório ausente).
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // ===========================
    // 400 Bad Request
    // ===========================

    /** Trata erros de validação de Bean Validation (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos da requisição são inválidos."
        );
        problem.setTitle("Erro de Validação");
        problem.setType(URI.create("https://restaurant-api.com/errors/validation-error"));
        problem.setProperty("timestamp", Instant.now());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    /** Trata incompatibilidade de tipo em parâmetros de requisição (ex: /api/v1/customers/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format("O parâmetro '%s' com valor '%s' é inválido. Tipo esperado: %s.",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Parâmetro Inválido");
        problem.setType(URI.create("https://restaurant-api.com/errors/invalid-parameter"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** Trata corpo da requisição inválido ou JSON malformado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "O corpo da requisição é inválido ou contém JSON malformado."
        );
        problem.setTitle("Corpo da Requisição Inválido");
        problem.setType(URI.create("https://restaurant-api.com/errors/malformed-json"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /** Trata dados que quebram uma regra de uma entidade de domínio. */
    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Regra de Domínio Violada");
        problem.setType(URI.create("https://restaurant-api.com/errors/domain-validation"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // ===========================
    // 401 Unauthorized
    // ===========================

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
        problem.setTitle("Credenciais Inválidas");
        problem.setType(URI.create("https://restaurant-api.com/errors/invalid-credentials"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // ===========================
    // 404 Not Found
    // ===========================

    /** Responde também à exceção homônima da Fase 1, que herda desta. */
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Usuário Não Encontrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/user-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UserTypeNotFoundException.class)
    public ProblemDetail handleUserTypeNotFound(UserTypeNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Tipo de Usuário Não Encontrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/user-type-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ProblemDetail handleRestaurantNotFound(RestaurantNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Restaurante Não Encontrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/restaurant-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ProblemDetail handleMenuItemNotFound(MenuItemNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Item do Cardápio Não Encontrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/menu-item-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "O recurso solicitado não foi encontrado: " + ex.getResourcePath()
        );
        problem.setTitle("Recurso Não Encontrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/resource-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // ===========================
    // 405 Method Not Allowed
    // ===========================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.METHOD_NOT_ALLOWED,
                String.format("O método HTTP '%s' não é suportado para este endpoint.", ex.getMethod())
        );
        problem.setTitle("Método Não Permitido");
        problem.setType(URI.create("https://restaurant-api.com/errors/method-not-allowed"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // ===========================
    // 409 Conflict
    // ===========================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("E-mail Já Cadastrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/email-conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(LoginAlreadyExistsException.class)
    public ProblemDetail handleLoginAlreadyExists(LoginAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Login Já Cadastrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/login-conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ProblemDetail handleCpfAlreadyExists(CpfAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("CPF Já Cadastrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/cpf-conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(CnpjAlreadyExistsException.class)
    public ProblemDetail handleCnpjAlreadyExists(CnpjAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("CNPJ Já Cadastrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/cnpj-conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UserTypeAlreadyExistsException.class)
    public ProblemDetail handleUserTypeAlreadyExists(UserTypeAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Tipo de Usuário Já Cadastrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/user-type-conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MenuItemAlreadyExistsException.class)
    public ProblemDetail handleMenuItemAlreadyExists(MenuItemAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Item do Cardápio Já Cadastrado");
        problem.setType(URI.create("https://restaurant-api.com/errors/menu-item-conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UserTypeInUseException.class)
    public ProblemDetail handleUserTypeInUse(UserTypeInUseException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Tipo de Usuário em Uso");
        problem.setType(URI.create("https://restaurant-api.com/errors/user-type-in-use"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UserOwnsRestaurantsException.class)
    public ProblemDetail handleUserOwnsRestaurants(UserOwnsRestaurantsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Usuário É Dono de Restaurantes");
        problem.setType(URI.create("https://restaurant-api.com/errors/user-owns-restaurants"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade de dados no banco: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Conflito de integridade de dados. Um dos valores informados já existe no sistema ou viola restrições do banco."
        );
        problem.setTitle("Conflito de Dados");
        problem.setType(URI.create("https://restaurant-api.com/errors/data-integrity-conflict"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // ===========================
    // 422 Unprocessable Entity
    // ===========================

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPassword(InvalidPasswordException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Senha Inválida");
        problem.setType(URI.create("https://restaurant-api.com/errors/invalid-password"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
