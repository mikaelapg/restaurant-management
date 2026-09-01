package com.restaurant.management.service;

import com.restaurant.management.dto.request.LoginRequest;
import com.restaurant.management.dto.response.LoginResponse;

/**
 * Serviço de validação de credenciais exigido pelo enunciado.
 * <p>
 * A abstração mantém a camada web desacoplada da forma como a checagem é feita
 * (hoje, consulta ao banco e comparação de hash BCrypt).
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);
}
