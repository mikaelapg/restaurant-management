package com.restaurant.management.service;

import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.exception.EmailAlreadyExistsException;
import com.restaurant.management.exception.LoginAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Única responsabilidade: garantir que e-mail e login não pertençam a outro
 * usuário, ignorando a caixa, antes de esbarrar nas constraints do banco.
 * <p>
 * Extraído de {@code BaseUserService} para que a classe base cuide apenas da
 * orquestração das operações comuns (troca de senha e exclusão), e a política de
 * unicidade viva num colaborador dedicado, reutilizável e testável isoladamente
 * (Princípio da Responsabilidade Única).
 */
@Component
@RequiredArgsConstructor
public class UserUniquenessValidator {

    private final UserRepository userRepository;

    /**
     * @param currentUserId id do próprio usuário numa atualização,
     *                      ou {@code null} quando for um cadastro novo
     */
    public void ensureEmailAvailable(String email, Long currentUserId) {
        userRepository.findByEmail(email.toLowerCase())
                .filter(other -> !other.getId().equals(currentUserId))
                .ifPresent(other -> {
                    throw new EmailAlreadyExistsException(email);
                });
    }

    /** Mesma ideia da checagem de e-mail, aplicada ao login — também ignorando a caixa. */
    public void ensureLoginAvailable(String login, Long currentUserId) {
        userRepository.findByLoginIgnoreCase(login)
                .filter(other -> !other.getId().equals(currentUserId))
                .ifPresent(other -> {
                    throw new LoginAlreadyExistsException(login);
                });
    }
}
