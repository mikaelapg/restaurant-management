package com.restaurant.management.service;

import com.restaurant.management.domain.enums.UserType;
import com.restaurant.management.domain.model.Address;
import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.repository.CustomerRepository;
import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.dto.request.AddressRequest;
import com.restaurant.management.dto.request.ChangePasswordRequest;
import com.restaurant.management.dto.request.CreateCustomerRequest;
import com.restaurant.management.dto.request.UpdateCustomerRequest;
import com.restaurant.management.dto.response.CustomerResponse;
import com.restaurant.management.exception.EmailAlreadyExistsException;
import com.restaurant.management.exception.InvalidPasswordException;
import com.restaurant.management.exception.LoginAlreadyExistsException;
import com.restaurant.management.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários — CustomerService")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

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
        @DisplayName("Deve cadastrar cliente com sucesso quando dados forem válidos")
        void shouldCreateCustomer_WhenDataIsValid() {
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Maria Silva", "maria@email.com", "maria.silva", "Senha@123",
                    sampleAddressRequest, "123.456.789-00", LocalDate.of(1995, 5, 20)
            );

            when(userRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());
            when(userRepository.findByLogin("maria.silva")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$10$encodedPassword");
            when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

            CustomerResponse response = customerService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Maria Silva");
            assertThat(response.userType()).isEqualTo(UserType.CUSTOMER);
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Deve lançar EmailAlreadyExistsException quando e-mail já estiver em uso")
        void shouldThrowEmailAlreadyExistsException_WhenEmailIsDuplicate() {
            CreateCustomerRequest request = new CreateCustomerRequest(
                    "Maria Silva", "maria@email.com", "maria.silva", "Senha@123",
                    null, "123.456.789-00", LocalDate.of(1995, 5, 20)
            );

            when(userRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(sampleCustomer));

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

            when(userRepository.findByEmail("novo@email.com")).thenReturn(Optional.empty());
            when(userRepository.findByLogin("maria.silva")).thenReturn(Optional.of(sampleCustomer));

            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(LoginAlreadyExistsException.class)
                    .hasMessageContaining("maria.silva");

            verify(customerRepository, never()).save(any());
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
        @DisplayName("Deve listar todos os clientes quando nome for nulo")
        void shouldListAllCustomers_WhenNameIsNull() {
            when(customerRepository.findAll()).thenReturn(List.of(sampleCustomer));

            List<CustomerResponse> result = customerService.findAll(null);

            assertThat(result).hasSize(1);
            verify(customerRepository).findAll();
        }

        @Test
        @DisplayName("Deve filtrar clientes por nome quando parâmetro informado")
        void shouldFilterCustomers_WhenNameIsProvided() {
            when(customerRepository.findByNameContainingIgnoreCase("Maria")).thenReturn(List.of(sampleCustomer));

            List<CustomerResponse> result = customerService.findAll("Maria");

            assertThat(result).hasSize(1);
            verify(customerRepository).findByNameContainingIgnoreCase("Maria");
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
            when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

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
    }
}
