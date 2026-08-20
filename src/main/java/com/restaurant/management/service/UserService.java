package com.restaurant.management.service;

import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.response.UserResponse;
import com.restaurant.management.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Consultas que atravessam os dois subtipos de usuário.
 * Serve a busca por nome exigida no enunciado sem obrigar quem consulta
 * a saber de antemão se o usuário é cliente ou dono de restaurante.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(String name) {
        List<User> users = (name == null || name.isBlank())
                ? userRepository.findAll()
                : userRepository.findByNameContainingIgnoreCase(name);

        return users.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserType(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getLastModifiedAt(),
                AddressMapper.toResponse(user.getAddress())
        );
    }
}
