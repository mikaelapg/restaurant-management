package com.restaurant.management.infrastructure.persistence.user;

import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaEntity;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.restaurant.management.support.Fixtures.ADDRESS;
import static com.restaurant.management.support.Fixtures.HASH;
import static com.restaurant.management.support.Fixtures.MODIFIED_AT;
import static com.restaurant.management.support.Fixtures.OWNER_DETAILS;
import static com.restaurant.management.support.Fixtures.OWNER_TYPE;
import static com.restaurant.management.support.Fixtures.customer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do gateway de usuários e da tradução entre o {@code User} de domínio
 * (composição) e a hierarquia JPA (herança), com os repositórios mockados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserJpaGateway")
class UserJpaGatewayTest {

    @Mock
    private UserJpaRepository userRepository;
    @Mock
    private CustomerJpaRepository customerRepository;
    @Mock
    private RestaurantOwnerJpaRepository ownerRepository;
    @Mock
    private UserTypeJpaRepository userTypeRepository;

    private UserJpaGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new UserJpaGateway(userRepository, customerRepository, ownerRepository, userTypeRepository);
    }

    private static CustomerJpaEntity customerEntity(Long id) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(id);
        entity.setName("Maria Oliveira");
        entity.setEmail("maria@email.com");
        entity.setLogin("maria.oliveira");
        entity.setPassword(HASH);
        entity.setLastModifiedAt(MODIFIED_AT);
        entity.setCpf("123.456.789-00");
        entity.setDateOfBirth(LocalDate.of(1995, 7, 15));
        entity.setUserType(new UserTypeJpaEntity(2L, "Cliente", UserProfile.CUSTOMER));
        return entity;
    }

    private static RestaurantOwnerJpaEntity ownerEntity(Long id) {
        RestaurantOwnerJpaEntity entity = new RestaurantOwnerJpaEntity();
        entity.setId(id);
        entity.setName("João Silva");
        entity.setEmail("joao@restaurante.com");
        entity.setLogin("joao.silva");
        entity.setPassword(HASH);
        entity.setLastModifiedAt(MODIFIED_AT);
        entity.setAddress(UserAddressEmbeddable.fromDomain(ADDRESS));
        entity.setRestaurantName("Grupo Silva");
        entity.setCnpj("12.345.678/0001-90");
        entity.setUserType(new UserTypeJpaEntity(1L, "Dono de Restaurante", UserProfile.RESTAURANT_OWNER));
        return entity;
    }

    @Test
    @DisplayName("salva um dono como RestaurantOwnerJpaEntity, com referência gerenciada ao tipo")
    void shouldSaveOwner() {
        UserTypeJpaEntity reference = new UserTypeJpaEntity(1L, "Dono de Restaurante", UserProfile.RESTAURANT_OWNER);
        when(userTypeRepository.getReferenceById(1L)).thenReturn(reference);
        when(userRepository.saveAndFlush(any())).thenReturn(ownerEntity(1L));

        User saved = gateway.save(User.create("João Silva", "joao@restaurante.com", "joao.silva", HASH, ADDRESS,
                OWNER_DETAILS, OWNER_TYPE));

        ArgumentCaptor<UserJpaEntity> captor = ArgumentCaptor.forClass(UserJpaEntity.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(RestaurantOwnerJpaEntity.class);
        RestaurantOwnerJpaEntity entity = (RestaurantOwnerJpaEntity) captor.getValue();
        assertThat(entity.getCnpj()).isEqualTo("12.345.678/0001-90");
        assertThat(entity.getPassword()).isEqualTo(HASH);
        assertThat(entity.getAddress().getStreet()).isEqualTo("Rua Augusta");
        assertThat(entity.getUserType()).isSameAs(reference);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getProfile()).isEqualTo(UserProfile.RESTAURANT_OWNER);
        assertThat(saved.getAddress()).isEqualTo(ADDRESS);
        assertThat(saved.canOwnRestaurants()).isTrue();
        assertThat(saved.getLastModifiedAt()).isEqualTo(MODIFIED_AT);
    }

    @Test
    @DisplayName("salva um cliente sem tipo e sem endereço como CustomerJpaEntity")
    void shouldSaveCustomerWithoutTypeAndAddress() {
        when(userRepository.saveAndFlush(any())).thenReturn(customerEntity(2L));

        User saved = gateway.save(customer(2L, null));

        ArgumentCaptor<UserJpaEntity> captor = ArgumentCaptor.forClass(UserJpaEntity.class);
        verify(userRepository).saveAndFlush(captor.capture());
        CustomerJpaEntity entity = (CustomerJpaEntity) captor.getValue();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getCpf()).isEqualTo("123.456.789-00");
        assertThat(entity.getAddress()).isNull();
        assertThat(entity.getUserType()).isNull();

        assertThat(saved.getDetails()).isEqualTo(new CustomerDetails("123.456.789-00", LocalDate.of(1995, 7, 15)));
        assertThat(saved.getAddress()).isNull();
    }

    @Test
    @DisplayName("lista pelo repositório do perfil pedido, ou pelo da raiz, ordenando por nome e id")
    void shouldListByProfile() {
        PageQuery query = new PageQuery(0, 10);
        when(customerRepository.findByNameContainingIgnoreCase(eq("ma"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(customerEntity(2L)), PageRequest.of(0, 10), 1));
        when(ownerRepository.findByNameContainingIgnoreCase(eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ownerEntity(1L)), PageRequest.of(0, 10), 1));
        when(userRepository.findByNameContainingIgnoreCase(eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ownerEntity(1L), customerEntity(2L)), PageRequest.of(0, 10), 2));

        PageResult<User> customers = gateway.findAll("ma", UserProfile.CUSTOMER, query);
        PageResult<User> owners = gateway.findAll(null, UserProfile.RESTAURANT_OWNER, query);
        PageResult<User> all = gateway.findAll("", null, query);

        assertThat(customers.content()).extracting(User::getProfile).containsExactly(UserProfile.CUSTOMER);
        assertThat(owners.content()).extracting(User::getId).containsExactly(1L);
        assertThat(all.totalElements()).isEqualTo(2);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByNameContainingIgnoreCase(eq(""), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by("name", "id"));
    }

    @Test
    @DisplayName("repassa as buscas por id, e-mail, login, CPF e CNPJ")
    void shouldDelegateLookups() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerEntity(1L)));
        when(userRepository.findByEmailIgnoreCase("maria@email.com")).thenReturn(Optional.of(customerEntity(2L)));
        when(userRepository.findByLoginIgnoreCase("joao.silva")).thenReturn(Optional.of(ownerEntity(1L)));
        when(customerRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(customerEntity(2L)));
        when(ownerRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.empty());

        assertThat(gateway.findById(1L)).map(User::getDetails)
                .contains(new RestaurantOwnerDetails("Grupo Silva", "12.345.678/0001-90"));
        assertThat(gateway.findByEmail("maria@email.com")).map(User::getId).contains(2L);
        assertThat(gateway.findByLogin("joao.silva")).map(User::getId).contains(1L);
        assertThat(gateway.findByCpf("123.456.789-00")).isPresent();
        assertThat(gateway.findByCnpj("12.345.678/0001-90")).isEmpty();
    }

    @Test
    @DisplayName("repassa existência e exclusão")
    void shouldDelegateExistenceAndDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsByUserTypeId(3L)).thenReturn(true);

        assertThat(gateway.existsById(1L)).isTrue();
        assertThat(gateway.existsByUserTypeId(3L)).isTrue();
        gateway.deleteById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("recusa traduzir uma subclasse JPA desconhecida")
    void shouldRejectUnknownSubclass() {
        UserJpaEntity unknown = mock(UserJpaEntity.class);

        assertThatThrownBy(() -> UserJpaMapper.toDomain(unknown)).isInstanceOf(IllegalStateException.class);
    }
}
