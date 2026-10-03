package com.restaurant.management.service;

import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.UserResponse;
import com.restaurant.management.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação das consultas que atravessam os dois subtipos de usuário.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(String name, Pageable pageable) {
        var page = (name == null || name.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByNameContainingIgnoreCase(name, pageable);

        return PageResponse.of(page, this::toResponse);
    }

    @Override
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
                AddressMapper.toResponse(user.getAddress()),
                UserTypeMapper.toResponse(user.getAssignedUserType())
        );
    }
}
