package com.restaurant.management.service;

import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

/**
 * Consultas que atravessam os dois subtipos de usuário.
 * <p>
 * Serve a busca por nome exigida no enunciado sem obrigar quem consulta a saber
 * de antemão se o usuário é cliente ou dono de restaurante.
 */
public interface UserService {

    PageResponse<UserResponse> findAll(String name, Pageable pageable);

    UserResponse findById(Long id);
}
