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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduz exceções em respostas no formato RFC 7807 ({@code application/problem+json}).
 * <p>
 * É a fronteira entre o núcleo e o HTTP: os casos de uso lançam exceções que não sabem
 * nada de status code, e só aqui elas viram 400, 404, 409 ou 422. Cada resposta tem um
 * {@code type} estável, que identifica a classe do problema para quem consome a API.
 * <p>
 * A precedência máxima é necessária porque {@code spring.mvc.problemdetails.enabled} registra o
 * {@code ProblemDetailsExceptionHandler} do próprio Spring com ordem 0. Sem o {@code @Order}, ele
 * responderia primeiro às exceções padrão do MVC (validação, JSON malformado, tipo de parâmetro e
 * método não suportado), com {@code "type": "about:blank"} e títulos em inglês.
 * <p>
 * O tratamento genérico de {@code Exception} fica em {@link UnhandledExceptionHandler}, com a menor
 * precedência: se estivesse nesta classe, capturaria tudo antes do Spring e transformaria em 500
 * respostas que o tratamento nativo resolve corretamente (415, 406, parâmetro obrigatório ausente).
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    static final String TYPE_BASE = "https://restaurant-api.com/errors/";

    // ===========================
    // 400 Bad Request
    // ===========================

    /** Erros de Bean Validation ({@code @Valid}), com o mapa {@code fieldErrors} apontando cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Erro de Validação", "validation-error",
                "Um ou mais campos da requisição são inválidos.");
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    /** Parâmetro de caminho ou de consulta com tipo errado, como {@code /customers/abc}. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido";
        return problem(HttpStatus.BAD_REQUEST, "Parâmetro Inválido", "invalid-parameter",
                String.format("O parâmetro '%s' com valor '%s' é inválido. Tipo esperado: %s.",
                        ex.getName(), ex.getValue(), expected));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Corpo da Requisição Inválido", "malformed-json",
                "O corpo da requisição é inválido ou contém JSON malformado.");
    }

    /** Dados que quebram uma regra de uma entidade de domínio. */
    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Regra de Domínio Violada", "domain-validation", ex.getMessage());
    }

    // ===========================
    // 401 Unauthorized
    // ===========================

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciais Inválidas", "invalid-credentials", ex.getMessage());
    }

    // ===========================
    // 404 Not Found
    // ===========================

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Usuário Não Encontrado", "user-not-found", ex.getMessage());
    }

    @ExceptionHandler(UserTypeNotFoundException.class)
    public ProblemDetail handleUserTypeNotFound(UserTypeNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Tipo de Usuário Não Encontrado", "user-type-not-found", ex.getMessage());
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ProblemDetail handleRestaurantNotFound(RestaurantNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Restaurante Não Encontrado", "restaurant-not-found", ex.getMessage());
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ProblemDetail handleMenuItemNotFound(MenuItemNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Item do Cardápio Não Encontrado", "menu-item-not-found", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso Não Encontrado", "resource-not-found",
                "O recurso solicitado não foi encontrado: " + ex.getResourcePath());
    }

    // ===========================
    // 405 Method Not Allowed
    // ===========================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Método Não Permitido", "method-not-allowed",
                String.format("O método HTTP '%s' não é suportado para este endpoint.", ex.getMethod()));
    }

    // ===========================
    // 409 Conflict
    // ===========================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "E-mail Já Cadastrado", "email-conflict", ex.getMessage());
    }

    @ExceptionHandler(LoginAlreadyExistsException.class)
    public ProblemDetail handleLoginAlreadyExists(LoginAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Login Já Cadastrado", "login-conflict", ex.getMessage());
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ProblemDetail handleCpfAlreadyExists(CpfAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "CPF Já Cadastrado", "cpf-conflict", ex.getMessage());
    }

    @ExceptionHandler(CnpjAlreadyExistsException.class)
    public ProblemDetail handleCnpjAlreadyExists(CnpjAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "CNPJ Já Cadastrado", "cnpj-conflict", ex.getMessage());
    }

    @ExceptionHandler(UserTypeAlreadyExistsException.class)
    public ProblemDetail handleUserTypeAlreadyExists(UserTypeAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Tipo de Usuário Já Cadastrado", "user-type-conflict", ex.getMessage());
    }

    @ExceptionHandler(MenuItemAlreadyExistsException.class)
    public ProblemDetail handleMenuItemAlreadyExists(MenuItemAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Item do Cardápio Já Cadastrado", "menu-item-conflict", ex.getMessage());
    }

    @ExceptionHandler(UserTypeInUseException.class)
    public ProblemDetail handleUserTypeInUse(UserTypeInUseException ex) {
        return problem(HttpStatus.CONFLICT, "Tipo de Usuário em Uso", "user-type-in-use", ex.getMessage());
    }

    @ExceptionHandler(SystemUserTypeException.class)
    public ProblemDetail handleSystemUserType(SystemUserTypeException ex) {
        return problem(HttpStatus.CONFLICT, "Tipo de Usuário de Sistema", "system-user-type", ex.getMessage());
    }

    @ExceptionHandler(UserOwnsRestaurantsException.class)
    public ProblemDetail handleUserOwnsRestaurants(UserOwnsRestaurantsException ex) {
        return problem(HttpStatus.CONFLICT, "Usuário É Dono de Restaurantes", "user-owns-restaurants", ex.getMessage());
    }

    @ExceptionHandler(UserTypeChangeNotAllowedException.class)
    public ProblemDetail handleUserTypeChangeNotAllowed(UserTypeChangeNotAllowedException ex) {
        return problem(HttpStatus.CONFLICT, "Troca de Tipo Não Permitida", "user-type-change-not-allowed",
                ex.getMessage());
    }

    /** Última barreira: uma restrição do banco que a aplicação não conferiu antes. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade de dados no banco: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Conflito de Dados", "data-integrity-conflict",
                "Conflito de integridade de dados. Um dos valores informados já existe no sistema "
                        + "ou viola restrições do banco.");
    }

    // ===========================
    // 422 Unprocessable Entity
    // ===========================

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPassword(InvalidPasswordException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Senha Inválida", "invalid-password", ex.getMessage());
    }

    @ExceptionHandler(InvalidRestaurantOwnerException.class)
    public ProblemDetail handleInvalidRestaurantOwner(InvalidRestaurantOwnerException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Usuário Não É Dono de Restaurante", "invalid-restaurant-owner",
                ex.getMessage());
    }

    @ExceptionHandler(IncompatibleUserTypeException.class)
    public ProblemDetail handleIncompatibleUserType(IncompatibleUserTypeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Tipo de Usuário Incompatível", "incompatible-user-type",
                ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String type, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(TYPE_BASE + type));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
