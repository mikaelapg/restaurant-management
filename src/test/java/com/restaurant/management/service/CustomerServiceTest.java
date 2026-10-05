package com.restaurant.management.service;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.domain.model.Address;
import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.repository.CustomerRepository;
import com.restaurant.management.dto.request.AddressRequest;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateCustomerRequest;
import com.restaurant.management.dto.request.UpdateCustomerRequest;
import com.restaurant.management.dto.response.CustomerResponse;
import com.restaurant.management.dto.response.PageResponse;
import com.restaurant.management.exception.CpfAlreadyExistsException;
import com.restaurant.management.exception.EmailAlreadyExistsException;
import com.restaurant.management.exception.InvalidPasswordException;
import com.restaurant.management.exception.LoginAlreadyExistsException;
import com.restaurant.management.exception.UserNotFoundException;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários — CustomerServiceImpl")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserUniquenessValidator uniquenessValidator;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserTypeGateway userTypeGateway;

    @Mock
    private RestaurantGateway restaurantGateway;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer sampleCustomer;
    private AddressRequest sampleAddressRequest;

    @BeforeEach
    void setUp() {
        Address address = Address.builder()
                .street("Rua das Flores")
                .number("123")
                .city("São Paulo")
                .state("SP")
                .zipCode("01001-000")
                .build();

        sampleCustomer = Customer.builder()
                .name("Maria Silva")
                .email("maria@email.com")
                .login("maria.silva")
                .password("$2a$10$encodedPassword")
                .cpf("123.456.789-00")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .address(address)
                .build();
        sampleCustomer.setId(1L);

        sampleAddressRequest = new AddressRequest(
                "Rua das Flores", "123", "Apto 4B", "São Paulo", "SP", "01001-000"
        );
    }

    @Nested
    @DisplayName("Criação de Cliente")
    class CreateTests {

        @Test
        @DisplayName("Deve associar o tipo de usuário \"Cliente\" ao cadastrar")
        void shouldAssignDefaultUserType_WhenCreating() {
            when(userTypeGateway.findByNameIgnoreCase(com.restaurant.management.core.domain.usertype.UserType.CUSTOMER))
                    .thenReturn(Optional.of(com.restaurant.management.core.domain.usertype.UserType.restore(7L, "Cliente")));
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = customerService.create(new CreateCustomerRequest(
                    "Maria Silva", "maria@email.com", "maria.silva", "Senha@123",
                    null, null, null
            ));

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            assertThat(captor.getValue().getAssignedUserType().getId()).isEqualTo(7L);
            assertThat(response.assignedUserType().name()).isEqualTo("Cliente");
        }

        @Test
        @DisplayName("Deve cadastrar sem tipo de usuário quando o tipo padrão não existir mais")
        void shouldCreateWithoutUserType_WhenDefaultIsMissing() {
            when(userTypeGateway.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = customerService.create(new CreateCustomerRequest(
                    "Maria Silva", "maria@email.com", "maria.silva", "Senha@123",
                    null, null, null
            ));

            assertThat(response.assignedUserType()).isNull();
        }

        @Test
        @DisplayName("Deve cadastrar cliente com sucesso quando dados forem válidos")
        void shouldCreateCustomer_WhenDataIsValid() {
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Maria Silva", "maria@email.com", "maria.silva", "Senha@123",
                    sampleAddressRequest, "123.456.789-00", LocalDate.of(1995, 5, 20)
            );

            when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$10$encodedPassword");
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

            CustomerResponse response = customerService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Maria Silva");
            assertThat(response.userType()).isEqualTo(UserType.CUSTOMER);
            verify(uniquenessValidator).ensureEmailAvailable("maria@email.com", null);
            verify(uniquenessValidator).ensureLoginAvailable("maria.silva", null);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar EmailAlreadyExistsException quando e-mail já estiver em uso")
        void shouldThrowEmailAlreadyExistsException_WhenEmailIsDuplicate() {
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Maria Silva", "maria@email.com", "maria.silva", "Senha@123",
                    null, "123.456.789-00", LocalDate.of(1995, 5, 20)
            );

            doThrow(new EmailAlreadyExistsException("maria@email.com"))
                    .when(uniquenessValidator).ensureEmailAvailable("maria@email.com", null);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("maria@email.com");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar LoginAlreadyExistsException quando login já estiver em uso")
        void shouldThrowLoginAlreadyExistsException_WhenLoginIsDuplicate() {
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Maria Silva", "novo@email.com", "maria.silva", "Senha@123",
                    null, "123.456.789-00", LocalDate.of(1995, 5, 20)
            );

            doThrow(new LoginAlreadyExistsException("maria.silva"))
                    .when(uniquenessValidator).ensureLoginAvailable("maria.silva", null);

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(LoginAlreadyExistsException.class)
                    .hasMessageContaining("maria.silva");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar CpfAlreadyExistsException quando CPF já estiver em uso")
        void shouldThrowCpfAlreadyExistsException_WhenCpfIsDuplicate() {
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Maria Silva", "novo@email.com", "novo.login", "Senha@123",
                    null, "123.456.789-00", LocalDate.of(1995, 5, 20)
            );

            when(customerRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(sampleCustomer));

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(CpfAlreadyExistsException.class)
                    .hasMessageContaining("123.456.789-00");

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Não deve checar duplicidade de CPF quando o campo vier ausente")
        void shouldSkipCpfCheck_WhenCpfIsNull() {
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Maria Silva", "maria@email.com", "maria.silva", "Senha@123",
                    null, null, LocalDate.of(1995, 5, 20)
            );

            when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$10$encodedPassword");
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

            customerService.create(request);

            verify(customerRepository, never()).findByCpf(any());
        }
    }

    @Nested
    @DisplayName("Busca e Listagem")
    class FindTests {

        @Test
        @DisplayName("Deve retornar cliente por ID com sucesso")
        void shouldReturnCustomerById_WhenFound() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

            CustomerResponse response = customerService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("maria@email.com");
        }

        @Test
        @DisplayName("Deve lançar UserNotFoundException quando ID não existir")
        void shouldThrowUserNotFoundException_WhenIdNotFound() {
            when(customerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.findById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Deve listar todos os clientes, paginados, quando nome for nulo")
        void shouldListAllCustomers_WhenNameIsNull() {
            when(customerRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleCustomer)));

            PageResponse<CustomerResponse> result = customerService.findAll(null, PageRequest.of(0, 20));

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            verify(customerRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve filtrar clientes por nome, paginados, quando parâmetro informado")
        void shouldFilterCustomers_WhenNameIsProvided() {
            when(customerRepository.findByNameContainingIgnoreCase(eq("Maria"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(sampleCustomer)));

            PageResponse<CustomerResponse> result = customerService.findAll("Maria", PageRequest.of(0, 20));

            assertThat(result.content()).hasSize(1);
            verify(customerRepository).findByNameContainingIgnoreCase(eq("Maria"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Atualização de Dados")
    class UpdateTests {

        @Test
        @DisplayName("Deve preservar o endereço existente quando address vier nulo no update")
        void shouldPreserveExistingAddress_WhenAddressIsNullInUpdateRequest() {
            UpdateCustomerRequest request = new UpdateCustomerRequest(
                    "Maria Atualizada", "maria@email.com", "maria.silva",
                    null, "123.456.789-00", LocalDate.of(1995, 5, 20)
            );

            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(customerRepository.saveAndFlush(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

            CustomerResponse response = customerService.update(1L, request);

            assertThat(response.name()).isEqualTo("Maria Atualizada");
            assertThat(response.address()).isNotNull();
            assertThat(response.address().street()).isEqualTo("Rua das Flores");
        }
    }

    @Nested
    @DisplayName("Troca de Senha")
    class ChangePasswordTests {

        @Test
        @DisplayName("Deve trocar senha com sucesso quando dados forem válidos")
        void shouldChangePassword_WhenDataIsValid() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "Senha@123", "NovaSenha@456", "NovaSenha@456"
            );

            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(passwordEncoder.matches("Senha@123", "$2a$10$encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("NovaSenha@456")).thenReturn("$2a$10$newHashedPassword");

            customerService.changePassword(1L, request);

            verify(customerRepository).save(sampleCustomer);
            assertThat(sampleCustomer.getPassword()).isEqualTo("$2a$10$newHashedPassword");
        }

        @Test
        @DisplayName("Deve lançar InvalidPasswordException quando senha atual estiver incorreta")
        void shouldThrowInvalidPasswordException_WhenCurrentPasswordIsWrong() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "SenhaErrada", "NovaSenha@456", "NovaSenha@456"
            );

            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(passwordEncoder.matches("SenhaErrada", "$2a$10$encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> customerService.changePassword(1L, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessage("A senha atual informada está incorreta.");
        }

        @Test
        @DisplayName("Deve lançar InvalidPasswordException quando nova senha e confirmação forem diferentes")
        void shouldThrowInvalidPasswordException_WhenNewPasswordDoesNotMatchConfirmation() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "Senha@123", "NovaSenha@456", "Diferente@789"
            );

            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(passwordEncoder.matches("Senha@123", "$2a$10$encodedPassword")).thenReturn(true);

            assertThatThrownBy(() -> customerService.changePassword(1L, request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessage("A nova senha e a confirmação não correspondem.");
        }
    }

    @Nested
    @DisplayName("Exclusão de Cliente")
    class DeleteTests {

        @Test
        @DisplayName("Deve excluir cliente com sucesso quando ID existir")
        void shouldDeleteCustomer_WhenIdExists() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

            customerService.delete(1L);

            verify(customerRepository).delete(sampleCustomer);
        }

        @Test
        @DisplayName("Deve recusar excluir cliente que é dono de restaurantes")
        void shouldRefuseDelete_WhenUserOwnsRestaurants() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
            when(restaurantGateway.existsByOwnerId(1L)).thenReturn(true);

            assertThatThrownBy(() -> customerService.delete(1L))
                    .isInstanceOf(UserOwnsRestaurantsException.class);
            verify(customerRepository, never()).delete(any());
        }
    }
}
