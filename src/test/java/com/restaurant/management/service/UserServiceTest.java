package com.restaurant.management.service;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.model.RestaurantOwner;
import com.restaurant.management.domain.model.User;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.UserResponse;
import com.restaurant.management.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários — UserServiceImpl (Busca Unificada)")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Deve buscar todos os usuários, paginados, quando filtro de nome for nulo")
    void shouldFindAllUsers_WhenNameIsNull() {
        User u1 = Customer.builder().name("Maria Silva").email("maria@email.com").login("maria.silva").build();
        u1.setId(1L);
        User u2 = RestaurantOwner.builder().name("Carlos Eduardo").email("carlos@email.com").login("carlos.eduardo").build();
        u2.setId(2L);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(u1, u2)));

        PageResponse<UserResponse> result = userService.findAll(null, PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).userType()).isEqualTo(UserType.CUSTOMER);
        assertThat(result.content().get(1).userType()).isEqualTo(UserType.RESTAURANT_OWNER);
        assertThat(result.totalElements()).isEqualTo(2);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar usuários por nome usando findByNameContainingIgnoreCase")
    void shouldFindUsersByName_WhenNameIsProvided() {
        User u1 = Customer.builder().name("Maria Silva").email("maria@email.com").login("maria.silva").build();
        u1.setId(1L);

        when(userRepository.findByNameContainingIgnoreCase(eq("Silva"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(u1)));

        PageResponse<UserResponse> result = userService.findAll("Silva", PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("Maria Silva");
        verify(userRepository).findByNameContainingIgnoreCase(eq("Silva"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldFindUserById_WhenExists() {
        User u1 = Customer.builder().name("Maria Silva").email("maria@email.com").login("maria.silva").build();
        u1.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));

        UserResponse response = userService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maria Silva");
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando ID não existir")
    void shouldThrowUserNotFoundException_WhenIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }
}
