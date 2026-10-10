package com.restaurant.management.infrastructure.persistence;

import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.menuitem.MenuItem;
import com.restaurant.management.core.domain.restaurant.OpeningHours;
import com.restaurant.management.core.domain.restaurant.Restaurant;
import com.restaurant.management.core.domain.shared.Address;
import com.restaurant.management.core.domain.usertype.UserType;
import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.infrastructure.persistence.menuitem.MenuItemJpaEntity;
import com.restaurant.management.infrastructure.persistence.menuitem.MenuItemJpaGateway;
import com.restaurant.management.infrastructure.persistence.menuitem.MenuItemJpaRepository;
import com.restaurant.management.infrastructure.persistence.restaurant.RestaurantJpaEntity;
import com.restaurant.management.infrastructure.persistence.restaurant.RestaurantJpaGateway;
import com.restaurant.management.infrastructure.persistence.restaurant.RestaurantJpaRepository;
import com.restaurant.management.infrastructure.persistence.shared.AddressEmbeddable;
import com.restaurant.management.infrastructure.persistence.user.UserJpaGateway;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaEntity;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaGateway;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários dos gateways JPA, com os repositórios Spring Data mockados.
 * <p>
 * Conferem o que é responsabilidade do gateway: a conversão entre entidade JPA e
 * entidade de domínio, a ordenação e a paginação repassadas ao repositório. As consultas
 * em si são exercitadas contra o PostgreSQL nos testes de integração.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Gateways JPA")
class JpaGatewaysTest {

    private static final Address ADDRESS = new Address("Rua Augusta", "1500", "Loja 2", "São Paulo", "SP", "01305-100");

    @Nested
    @DisplayName("UserTypeJpaGateway")
    class UserTypes {

        @Mock
        private UserTypeJpaRepository repository;

        @Test
        @DisplayName("converte entre domínio e JPA em todas as operações")
        void shouldMap() {
            UserTypeJpaGateway gateway = new UserTypeJpaGateway(repository);
            when(repository.save(any())).thenReturn(new UserTypeJpaEntity(1L, "Cliente"));
            when(repository.findById(1L)).thenReturn(Optional.of(new UserTypeJpaEntity(1L, "Cliente")));
            when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(new UserTypeJpaEntity(2L, "Admin")));
            when(repository.findByNameIgnoreCase("cliente")).thenReturn(Optional.empty());

            assertThat(gateway.save(UserType.create("Cliente")).getId()).isEqualTo(1L);
            assertThat(gateway.findById(1L)).map(UserType::getName).contains("Cliente");
            assertThat(gateway.findAll()).extracting(UserType::getName).containsExactly("Admin");
            assertThat(gateway.findByNameIgnoreCase("cliente")).isEmpty();
            gateway.deleteById(1L);
            verify(repository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("UserJpaGateway")
    class Users {

        @Mock
        private UserRepository userRepository;
        @Mock
        private UserTypeJpaRepository userTypeRepository;

        @Test
        @DisplayName("associa o tipo ao usuário e salva")
        void shouldAssign() {
            Customer customer = Customer.builder().name("Maria").build();
            UserTypeJpaEntity type = new UserTypeJpaEntity(3L, "Cliente VIP");
            when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
            when(userTypeRepository.getReferenceById(3L)).thenReturn(type);

            new UserJpaGateway(userRepository, userTypeRepository).assignUserType(10L, 3L);

            assertThat(customer.getAssignedUserType()).isSameAs(type);
            verify(userRepository).saveAndFlush(customer);
        }

        @Test
        @DisplayName("lança usuário não encontrado se ele sumir antes da associação")
        void shouldThrowWhenUserIsMissing() {
            when(userRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new UserJpaGateway(userRepository, userTypeRepository).assignUserType(10L, 3L))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("repassa as consultas de existência")
        void shouldDelegateExistenceChecks() {
            UserJpaGateway gateway = new UserJpaGateway(userRepository, userTypeRepository);
            when(userRepository.existsById(10L)).thenReturn(true);
            when(userRepository.existsByAssignedUserTypeId(3L)).thenReturn(true);

            assertThat(gateway.existsById(10L)).isTrue();
            assertThat(gateway.existsByUserTypeId(3L)).isTrue();
        }
    }

    @Nested
    @DisplayName("RestaurantJpaGateway")
    class Restaurants {

        @Mock
        private RestaurantJpaRepository repository;

        private static RestaurantJpaEntity entity(Long id) {
            return new RestaurantJpaEntity(id, "Cantina", AddressEmbeddable.fromDomain(ADDRESS), "Italiana",
                    LocalTime.of(18, 0), LocalTime.of(2, 0), 1L);
        }

        @Test
        @DisplayName("salva convertendo endereço e horário nos dois sentidos")
        void shouldSave() {
            Restaurant restaurant = Restaurant.create("Cantina", ADDRESS, "Italiana",
                    new OpeningHours(LocalTime.of(18, 0), LocalTime.of(2, 0)), 1L);
            when(repository.save(any())).thenReturn(entity(7L));

            Restaurant saved = new RestaurantJpaGateway(repository).save(restaurant);

            ArgumentCaptor<RestaurantJpaEntity> captor = ArgumentCaptor.forClass(RestaurantJpaEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getAddress().getComplement()).isEqualTo("Loja 2");
            assertThat(captor.getValue().getClosesAt()).isEqualTo(LocalTime.of(2, 0));
            assertThat(saved.getId()).isEqualTo(7L);
            assertThat(saved.getAddress()).isEqualTo(ADDRESS);
            assertThat(saved.getOpeningHours().crossesMidnight()).isTrue();
        }

        @Test
        @DisplayName("lista ordenando por nome e id, e trocando filtro nulo por vazio")
        void shouldSearch() {
            when(repository.search(eq(""), eq("ital"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(entity(7L)), PageRequest.of(1, 5), 6));

            PageResult<Restaurant> result = new RestaurantJpaGateway(repository).findAll(null, "ital", new PageQuery(1, 5));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).search(eq(""), eq("ital"), captor.capture());
            assertThat(captor.getValue().getSort()).isEqualTo(Sort.by("name", "id"));
            assertThat(result.content()).extracting(Restaurant::getId).containsExactly(7L);
            assertThat(result.totalElements()).isEqualTo(6);
            assertThat(result.page()).isEqualTo(1);
        }

        @Test
        @DisplayName("repassa busca por id, existência e exclusão")
        void shouldDelegate() {
            RestaurantJpaGateway gateway = new RestaurantJpaGateway(repository);
            when(repository.findById(7L)).thenReturn(Optional.of(entity(7L)));
            when(repository.existsById(7L)).thenReturn(true);
            when(repository.existsByOwnerId(1L)).thenReturn(true);

            assertThat(gateway.findById(7L)).map(Restaurant::getName).contains("Cantina");
            assertThat(gateway.existsById(7L)).isTrue();
            assertThat(gateway.existsByOwnerId(1L)).isTrue();
            gateway.deleteById(7L);
            verify(repository).deleteById(7L);
        }
    }

    @Nested
    @DisplayName("MenuItemJpaGateway")
    class MenuItems {

        @Mock
        private MenuItemJpaRepository repository;

        private static MenuItemJpaEntity entity(Long id, String name) {
            return new MenuItemJpaEntity(id, 1L, name, "Prato", new BigDecimal("10.00"), true, "/p.jpg");
        }

        @Test
        @DisplayName("salva e converte de volta para o domínio")
        void shouldSave() {
            when(repository.save(any())).thenReturn(entity(5L, "Risoto"));

            MenuItem saved = new MenuItemJpaGateway(repository)
                    .save(MenuItem.create(1L, "Risoto", "Prato", BigDecimal.TEN, true, "/p.jpg"));

            assertThat(saved.getId()).isEqualTo(5L);
            assertThat(saved.isDineInOnly()).isTrue();
            assertThat(saved.getPrice()).isEqualTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("lista o cardápio do restaurante em ordem de nome")
        void shouldList() {
            when(repository.findByRestaurantId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(entity(5L, "Risoto")), PageRequest.of(0, 20), 1));

            PageResult<MenuItem> result = new MenuItemJpaGateway(repository).findAllByRestaurantId(1L, new PageQuery(0, 20));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findByRestaurantId(eq(1L), captor.capture());
            assertThat(captor.getValue().getSort()).isEqualTo(Sort.by("name", "id"));
            assertThat(result.content()).extracting(MenuItem::getName).containsExactly("Risoto");
        }

        @Test
        @DisplayName("repassa as buscas escopadas no restaurante e a exclusão")
        void shouldDelegate() {
            MenuItemJpaGateway gateway = new MenuItemJpaGateway(repository);
            when(repository.findByIdAndRestaurantId(5L, 1L)).thenReturn(Optional.of(entity(5L, "Risoto")));
            when(repository.findByRestaurantIdAndNameIgnoreCase(1L, "risoto")).thenReturn(Optional.of(entity(5L, "Risoto")));

            assertThat(gateway.findByIdAndRestaurantId(5L, 1L)).isPresent();
            assertThat(gateway.findByRestaurantIdAndNameIgnoreCase(1L, "risoto")).map(MenuItem::getId).contains(5L);
            gateway.deleteById(5L);
            verify(repository).deleteById(5L);
        }
    }
}
