package com.restaurant.management.service;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.domain.model.Address;
import com.restaurant.management.domain.model.RestaurantOwner;
import com.restaurant.management.domain.repository.RestaurantOwnerRepository;
import com.restaurant.management.dto.request.AddressRequest;
import com.restaurant.management.dto.request.CreateRestaurantOwnerRequest;
import com.restaurant.management.dto.request.UpdateRestaurantOwnerRequest;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.dto.response.RestaurantOwnerResponse;
import com.restaurant.management.exception.CnpjAlreadyExistsException;
import com.restaurant.management.exception.LoginAlreadyExistsException;
import com.restaurant.management.core.application.exception.UserOwnsRestaurantsException;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.usertype.UserTypeGateway;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários — RestaurantOwnerServiceImpl")
class RestaurantOwnerServiceTest {

    @Mock
    private RestaurantOwnerRepository restaurantOwnerRepository;

    @Mock
    private UserUniquenessValidator uniquenessValidator;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserTypeGateway userTypeGateway;

    @Mock
    private RestaurantGateway restaurantGateway;

    @InjectMocks
    private RestaurantOwnerServiceImpl ownerService;

    private RestaurantOwner sampleOwner;
    private AddressRequest sampleAddressRequest;

    @BeforeEach
    void setUp() {
        Address address = Address.builder()
                .street("Av. Paulista")
                .number("1000")
                .city("São Paulo")
                .state("SP")
                .zipCode("01310-100")
                .build();

        sampleOwner = RestaurantOwner.builder()
                .name("Carlos Eduardo")
                .email("carlos@bistro.com")
                .login("carlos.eduardo")
                .password("$2a$10$encodedPassword")
                .restaurantName("Bistrô Francês")
                .cnpj("12.345.678/0001-90")
                .address(address)
                .build();
        sampleOwner.setId(1L);

        sampleAddressRequest = new AddressRequest(
                "Av. Paulista", "1000", "Conjunto 50", "São Paulo", "SP", "01310-100"
        );
    }

    @Nested
    @DisplayName("Criação de Dono de Restaurante")
    class CreateTests {

        @Test
        @DisplayName("Deve associar o tipo de usuário \"Dono de Restaurante\" ao cadastrar")
        void shouldAssignDefaultUserType_WhenCreating() {
            when(userTypeGateway.findByNameIgnoreCase(com.restaurant.management.core.domain.usertype.UserType.RESTAURANT_OWNER))
                    .thenReturn(Optional.of(com.restaurant.management.core.domain.usertype.UserType.restore(7L, "Dono de Restaurante")));
            when(restaurantOwnerRepository.save(any(RestaurantOwner.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = ownerService.create(new CreateRestaurantOwnerRequest(
                    "Carlos Eduardo", "carlos@bistro.com", "carlos.eduardo", "Senha@123",
                    null, "Bistrô Francês", null
            ));

            ArgumentCaptor<RestaurantOwner> captor = ArgumentCaptor.forClass(RestaurantOwner.class);
            verify(restaurantOwnerRepository).save(captor.capture());
            assertThat(captor.getValue().getAssignedUserType().getId()).isEqualTo(7L);
            assertThat(response.assignedUserType().name()).isEqualTo("Dono de Restaurante");
        }

        @Test
        @DisplayName("Deve cadastrar sem tipo de usuário quando o tipo padrão não existir mais")
        void shouldCreateWithoutUserType_WhenDefaultIsMissing() {
            when(userTypeGateway.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
            when(restaurantOwnerRepository.save(any(RestaurantOwner.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = ownerService.create(new CreateRestaurantOwnerRequest(
                    "Carlos Eduardo", "carlos@bistro.com", "carlos.eduardo", "Senha@123",
                    null, "Bistrô Francês", null
            ));

            assertThat(response.assignedUserType()).isNull();
        }

        @Test
        @DisplayName("Deve cadastrar dono de restaurante com sucesso")
        void shouldCreateOwner_WhenDataIsValid() {
            CreateRestaurantOwnerRequest request = new CreateRestaurantOwnerRequest(
                    "Carlos Eduardo", "carlos@bistro.com", "carlos.eduardo", "Senha@123",
                    sampleAddressRequest, "Bistrô Francês", "12.345.678/0001-90"
            );

            when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$10$encodedPassword");
            when(restaurantOwnerRepository.save(any(RestaurantOwner.class))).thenReturn(sampleOwner);

            RestaurantOwnerResponse response = ownerService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Carlos Eduardo");
            assertThat(response.userType()).isEqualTo(UserType.RESTAURANT_OWNER);
            assertThat(response.restaurantName()).isEqualTo("Bistrô Francês");
            verify(uniquenessValidator).ensureEmailAvailable("carlos@bistro.com", null);
            verify(uniquenessValidator).ensureLoginAvailable("carlos.eduardo", null);
            verify(restaurantOwnerRepository).save(any(RestaurantOwner.class));
        }

        @Test
        @DisplayName("Deve lançar LoginAlreadyExistsException quando login já existir")
        void shouldThrowLoginAlreadyExistsException_WhenLoginExists() {
            CreateRestaurantOwnerRequest request = new CreateRestaurantOwnerRequest(
                    "Carlos Eduardo", "novo@bistro.com", "carlos.eduardo", "Senha@123",
                    null, "Bistrô Francês", "12.345.678/0001-90"
            );

            doThrow(new LoginAlreadyExistsException("carlos.eduardo"))
                    .when(uniquenessValidator).ensureLoginAvailable("carlos.eduardo", null);

            assertThatThrownBy(() -> ownerService.create(request))
                    .isInstanceOf(LoginAlreadyExistsException.class)
                    .hasMessageContaining("carlos.eduardo");

            verify(restaurantOwnerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CnpjAlreadyExistsException quando CNPJ já estiver em uso")
        void shouldThrowCnpjAlreadyExistsException_WhenCnpjIsDuplicate() {
            CreateRestaurantOwnerRequest request = new CreateRestaurantOwnerRequest(
                    "Carlos Eduardo", "novo@bistro.com", "novo.login", "Senha@123",
                    null, "Bistrô Francês", "12.345.678/0001-90"
            );

            when(restaurantOwnerRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.of(sampleOwner));

            assertThatThrownBy(() -> ownerService.create(request))
                    .isInstanceOf(CnpjAlreadyExistsException.class)
                    .hasMessageContaining("12.345.678/0001-90");

            verify(restaurantOwnerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualização e Preservação de Endereço")
    class UpdateTests {

        @Test
        @DisplayName("Deve preservar o endereço existente quando address vier nulo")
        void shouldPreserveAddress_WhenAddressIsNullInUpdate() {
            UpdateRestaurantOwnerRequest request = new UpdateRestaurantOwnerRequest(
                    "Carlos Atualizado", "carlos@bistro.com", "carlos.eduardo",
                    null, "Bistrô Francês Novo", "12.345.678/0001-90"
            );

            when(restaurantOwnerRepository.findById(1L)).thenReturn(Optional.of(sampleOwner));
            when(restaurantOwnerRepository.saveAndFlush(any(RestaurantOwner.class))).thenAnswer(inv -> inv.getArgument(0));

            RestaurantOwnerResponse response = ownerService.update(1L, request);

            assertThat(response.name()).isEqualTo("Carlos Atualizado");
            assertThat(response.address()).isNotNull();
            assertThat(response.address().street()).isEqualTo("Av. Paulista");
        }
    }

    @Nested
    @DisplayName("Busca e Exclusão")
    class FindAndDeleteTests {

        @Test
        @DisplayName("Deve buscar dono por ID com sucesso")
        void shouldFindById_WhenOwnerExists() {
            when(restaurantOwnerRepository.findById(1L)).thenReturn(Optional.of(sampleOwner));

            RestaurantOwnerResponse response = ownerService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.restaurantName()).isEqualTo("Bistrô Francês");
        }

        @Test
        @DisplayName("Deve listar donos de forma paginada, filtrando pelo nome")
        void shouldListOwnersPaged_WhenNameIsProvided() {
            when(restaurantOwnerRepository.findByNameContainingIgnoreCase(eq("Carlos"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleOwner)));

            PageResponse<RestaurantOwnerResponse> result = ownerService.findAll("Carlos", PageRequest.of(0, 20));

            assertThat(result.content()).hasSize(1);
            assertThat(result.last()).isTrue();
            verify(restaurantOwnerRepository).findByNameContainingIgnoreCase(eq("Carlos"), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve excluir dono com sucesso")
        void shouldDeleteOwner_WhenExists() {
            when(restaurantOwnerRepository.findById(1L)).thenReturn(Optional.of(sampleOwner));

            ownerService.delete(1L);

            verify(restaurantOwnerRepository).delete(sampleOwner);
        }

        @Test
        @DisplayName("Deve recusar excluir dono que é dono de restaurantes")
        void shouldRefuseDelete_WhenUserOwnsRestaurants() {
            when(restaurantOwnerRepository.findById(1L)).thenReturn(Optional.of(sampleOwner));
            when(restaurantGateway.existsByOwnerId(1L)).thenReturn(true);

            assertThatThrownBy(() -> ownerService.delete(1L))
                    .isInstanceOf(UserOwnsRestaurantsException.class);
            verify(restaurantOwnerRepository, never()).delete(any());
        }
    }
}
