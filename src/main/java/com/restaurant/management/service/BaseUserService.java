package com.restaurant.management.service;

import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserBaseRepository;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.exception.InvalidPasswordException;
import com.restaurant.management.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra as operações comuns a qualquer tipo de usuário: troca de senha e
 * exclusão. A checagem de unicidade de e-mail e login é delegada a
 * {@link UserUniquenessValidator}, e as regras específicas de cada subtipo (CPF,
 * CNPJ, nome do restaurante) ficam nas implementações concretas.
 * <p>
 * Um novo subtipo de usuário — por exemplo, "Entregador" — só precisa de uma
 * subclasse desta base, um repositório e um controller; nada aqui muda
 * (Princípio Aberto/Fechado).
 *
 * @param <T> subtipo concreto de {@link User} gerenciado pelo serviço
 */
@Slf4j
public abstract class BaseUserService<T extends User> {

    protected final UserBaseRepository<T> repository;
    protected final UserUniquenessValidator uniquenessValidator;
    protected final BCryptPasswordEncoder passwordEncoder;

    protected BaseUserService(UserBaseRepository<T> repository,
                              UserUniquenessValidator uniquenessValidator,
                              BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.uniquenessValidator = uniquenessValidator;
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

    /** Unicidade de e-mail entre todos os usuários. Delegada ao colaborador dedicado. */
    protected void ensureEmailIsAvailable(String email, Long currentUserId) {
        uniquenessValidator.ensureEmailAvailable(email, currentUserId);
    }

    /** Unicidade de login entre todos os usuários. Delegada ao colaborador dedicado. */
    protected void ensureLoginIsAvailable(String login, Long currentUserId) {
        uniquenessValidator.ensureLoginAvailable(login, currentUserId);
    }
}
