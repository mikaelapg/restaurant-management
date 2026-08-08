package com.restaurant.management.domain.repository;

import com.restaurant.management.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * Contrato comum aos repositórios dos subtipos de usuário.
 * Não é um bean por si só — cada subtipo declara a sua própria interface.
 */
@NoRepositoryBean
public interface UserBaseRepository<T extends User> extends JpaRepository<T, Long> {

    /** Busca por trecho do nome, ignorando maiúsculas e minúsculas. */
    List<T> findByNameContainingIgnoreCase(String name);
}
