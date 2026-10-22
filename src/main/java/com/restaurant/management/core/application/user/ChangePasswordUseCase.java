package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.exception.InvalidPasswordException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.domain.user.PasswordPolicy;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;

/**
 * Troca a senha de um usuário. Exige a senha atual e a confirmação da nova, e fica
 * separada da atualização de dados cadastrais.
 */
public class ChangePasswordUseCase {

    private final UserGateway gateway;
    private final PasswordHasher passwordHasher;

    public ChangePasswordUseCase(UserGateway gateway, PasswordHasher passwordHasher) {
        this.gateway = gateway;
        this.passwordHasher = passwordHasher;
    }

    /**
     * @param profile perfil exigido do usuário (o do endpoint chamado), ou {@code null} para qualquer um
     */
    public void execute(Long id, UserProfile profile, String currentPassword,
                        String newPassword, String confirmNewPassword) {
        User user = gateway.findById(id)
                .filter(found -> profile == null || found.getProfile() == profile)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!passwordHasher.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("A senha atual informada está incorreta.");
        }
        if (newPassword == null || !newPassword.equals(confirmNewPassword)) {
            throw new InvalidPasswordException("A nova senha e a confirmação não correspondem.");
        }
        PasswordPolicy.validate(newPassword);
        gateway.save(user.changePassword(passwordHasher.hash(newPassword)));
    }
}
