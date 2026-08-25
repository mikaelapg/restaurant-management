package com.restaurant.management.service;

import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserBaseRepository;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.exception.EmailAlreadyExistsException;
import com.restaurant.management.exception.InvalidPasswordException;
import com.restaurant.management.exception.LoginAlreadyExistsException;
import com.restaurant.management.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras que valem para qualquer tipo de usuário: unicidade de e-mail e login,
 * troca de senha e exclusão.
 * <p>
 * Os serviços concretos herdam esse comportamento e cuidam apenas do que é
 * específico do seu subtipo (CPF, CNPJ, nome do restaurante etc.).
 */
@Slf4j
public abstract class BaseUserService<T extends User> {

    protected final UserBaseRepository<T> repository;
    protected final UserRepository userRepository;
    protected final BCryptPasswordEncoder passwordEncoder;

    protected BaseUserService(UserBaseRepository<T> repository,
                              UserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Troca a senha do usuário. Exige a senha atual e a confirmação da nova,
     * e é exposta em endpoint próprio, separado da atualização de dados.
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        T user = findOrThrow(id);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("A senha atual informada está incorreta.");
        }
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new InvalidPasswordException("A nova senha e a confirmação não correspondem.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repository.save(user);
        log.info("Senha alterada para o usuário {}", id);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findOrThrow(id));
        log.info("Usuário {} excluído", id);
    }

    protected T findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Garante que o e-mail não pertença a outro usuário.
     *
     * @param currentUserId id do próprio usuário em uma atualização,
     *                      ou {@code null} quando for um cadastro novo
     */
    protected void ensureEmailIsAvailable(String email, Long currentUserId) {
        userRepository.findByEmail(email.toLowerCase())
                .filter(other -> !other.getId().equals(currentUserId))
                .ifPresent(other -> {
                    throw new EmailAlreadyExistsException(email);
                });
    }

    /** Mesma ideia da checagem de e-mail, aplicada ao login — também ignorando a caixa. */
    protected void ensureLoginIsAvailable(String login, Long currentUserId) {
        userRepository.findByLoginIgnoreCase(login)
                .filter(other -> !other.getId().equals(currentUserId))
                .ifPresent(other -> {
                    throw new LoginAlreadyExistsException(login);
                });
    }
}
