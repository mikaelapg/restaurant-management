package com.restaurant.management.service;

import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.request.LoginRequest;
import com.restaurant.management.dto.response.LoginResponse;
import com.restaurant.management.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação da validação de credenciais.
 * <p>
 * Realiza a validação de login e senha consultando o banco de dados.
 * A senha é comparada com o hash BCrypt armazenado.
 * Não utiliza Spring Security — apenas o módulo de criptografia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Valida as credenciais do usuário.
     *
     * @param request DTO com login e senha
     * @return {@link LoginResponse} com dados do usuário autenticado
     * @throws InvalidCredentialsException se o login não existir ou a senha estiver incorreta
     */
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para o usuário: {}", request.login());

        User user = userRepository.findByLoginIgnoreCase(request.login())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Senha incorreta para o login: {}", request.login());
            throw new InvalidCredentialsException();
        }

        log.info("Login realizado com sucesso para: {} (tipo: {})", user.getLogin(), user.getUserType());

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getUserType(),
                "Login realizado com sucesso"
        );
    }
}
