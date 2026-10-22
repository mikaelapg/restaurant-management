package com.restaurant.management.core.application.user;

import com.restaurant.management.core.application.auth.AuthenticateUserUseCase;
import com.restaurant.management.core.application.exception.CnpjAlreadyExistsException;
import com.restaurant.management.core.application.exception.CpfAlreadyExistsException;
import com.restaurant.management.core.application.exception.EmailAlreadyExistsException;
import com.restaurant.management.core.application.exception.InvalidCredentialsException;
import com.restaurant.management.core.application.exception.InvalidPasswordException;
import com.restaurant.management.core.application.exception.LoginAlreadyExistsException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.exception.UserOwnsRestaurantsException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.usertype.UserTypeGateway;
import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.user.CustomerDetails;
import com.restaurant.management.core.domain.user.RestaurantOwnerDetails;
import com.restaurant.management.core.domain.user.User;
import com.restaurant.management.core.domain.user.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.restaurant.management.support.Fixtures.ADDRESS;
import static com.restaurant.management.support.Fixtures.CUSTOMER_DETAILS;
import static com.restaurant.management.support.Fixtures.CUSTOMER_TYPE;
import static com.restaurant.management.support.Fixtures.HASH;
import static com.restaurant.management.support.Fixtures.OWNER_DETAILS;
import static com.restaurant.management.support.Fixtures.OWNER_TYPE;
import static com.restaurant.management.support.Fixtures.PASSWORD;
import static com.restaurant.management.support.Fixtures.customer;
import static com.restaurant.management.support.Fixtures.owner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes dos casos de uso de usuário com gateways e hash de senha mockados. Nada de
 * Spring nem de banco: só as regras.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Casos de uso de usuário")
class UserUseCasesTest {

    @Mock
    private UserGateway gateway;

    @Mock
    private UserTypeGateway userTypeGateway;

    @Mock
    private RestaurantGateway restaurantGateway;

    @Mock
    private PasswordHasher passwordHasher;

    private static CreateUserCommand createCustomer() {
        return new CreateUserCommand("Maria Oliveira", "Maria@Email.com", "maria.oliveira", PASSWORD, null,
                CUSTOMER_DETAILS);
    }

    @Nested
    @DisplayName("Cadastrar")
    class Create {

        private CreateUserUseCase useCase() {
            return new CreateUserUseCase(gateway, userTypeGateway, passwordHasher);
        }

        @Test
        @DisplayName("grava o hash da senha e associa o tipo de sistema do perfil")
        void shouldCreateWithHashAndDefaultType() {
            when(userTypeGateway.findByProfile(UserProfile.CUSTOMER)).thenReturn(Optional.of(CUSTOMER_TYPE));
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.findByEmail("maria@email.com")).thenReturn(Optional.empty());
            when(gateway.findByLogin("maria.oliveira")).thenReturn(Optional.empty());
            when(gateway.findByCpf("123.456.789-00")).thenReturn(Optional.empty());
            when(gateway.save(any())).thenReturn(customer(5L));

            User result = useCase().execute(createCustomer());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(gateway).save(captor.capture());
            User toSave = captor.getValue();
            assertThat(toSave.getId()).isNull();
            assertThat(toSave.getPasswordHash()).isEqualTo(HASH);
            assertThat(toSave.getEmail()).isEqualTo("maria@email.com");
            assertThat(toSave.getUserType()).isEqualTo(CUSTOMER_TYPE);
            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("dono nasce com o tipo \\"Dono de Restaurante\\" e pode ser responsável por restaurantes")
        void shouldCreateOwnerAbleToOwnRestaurants() {
            when(userTypeGateway.findByProfile(UserProfile.RESTAURANT_OWNER)).thenReturn(Optional.of(OWNER_TYPE));
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            User result = useCase().execute(new CreateUserCommand("João Silva", "joao@x.com", "joao", PASSWORD,
                    ADDRESS, OWNER_DETAILS));

            assertThat(result.canOwnRestaurants()).isTrue();
            verify(gateway).findByCnpj("12.345.678/0001-90");
        }

        @Test
        @DisplayName("sem o tipo de sistema cadastrado, o usuário nasce sem tipo")
        void shouldCreateWithoutTypeWhenSystemTypeIsMissing() {
            when(userTypeGateway.findByProfile(UserProfile.CUSTOMER)).thenReturn(Optional.empty());
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            assertThat(useCase().execute(createCustomer()).getUserType()).isNull();
        }

        @Test
        @DisplayName("recusa senha fraca antes de gerar hash")
        void shouldRejectWeakPassword() {
            CreateUserCommand command = new CreateUserCommand("Maria", "m@x.com", "maria", "fraca", null,
                    CUSTOMER_DETAILS);

            assertThatThrownBy(() -> useCase().execute(command)).isInstanceOf(DomainValidationException.class);
            verify(passwordHasher, never()).hash(anyString());
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("recusa e-mail de outro usuário")
        void shouldRejectTakenEmail() {
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.findByEmail("maria@email.com")).thenReturn(Optional.of(customer(9L)));

            assertThatThrownBy(() -> useCase().execute(createCustomer()))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("maria@email.com");
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("recusa login de outro usuário")
        void shouldRejectTakenLogin() {
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.findByLogin("maria.oliveira")).thenReturn(Optional.of(customer(9L)));

            assertThatThrownBy(() -> useCase().execute(createCustomer()))
                    .isInstanceOf(LoginAlreadyExistsException.class);
        }

        @Test
        @DisplayName("recusa CPF de outro cliente")
        void shouldRejectTakenCpf() {
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.findByCpf("123.456.789-00")).thenReturn(Optional.of(customer(9L)));

            assertThatThrownBy(() -> useCase().execute(createCustomer()))
                    .isInstanceOf(CpfAlreadyExistsException.class);
        }

        @Test
        @DisplayName("recusa CNPJ de outro dono")
        void shouldRejectTakenCnpj() {
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.of(owner(9L)));

            assertThatThrownBy(() -> useCase().execute(new CreateUserCommand("João Silva", "joao@x.com", "joao",
                    PASSWORD, null, OWNER_DETAILS)))
                    .isInstanceOf(CnpjAlreadyExistsException.class);
        }

        @Test
        @DisplayName("não confere CPF nem CNPJ ausentes")
        void shouldSkipMissingDocuments() {
            when(passwordHasher.hash(PASSWORD)).thenReturn(HASH);
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            useCase().execute(new CreateUserCommand("Maria", "m@x.com", "maria", PASSWORD, null,
                    new CustomerDetails(null, null)));
            useCase().execute(new CreateUserCommand("João", "j@x.com", "joao", PASSWORD, null,
                    new RestaurantOwnerDetails(null, null)));

            verify(gateway, never()).findByCpf(any());
            verify(gateway, never()).findByCnpj(any());
        }
    }

    @Nested
    @DisplayName("Atualizar")
    class Update {

        private UpdateUserUseCase useCase() {
            return new UpdateUserUseCase(gateway);
        }

        @Test
        @DisplayName("mantém o endereço quando ele não é informado e aceita os próprios dados")
        void shouldUpdateKeepingAddress() {
            when(gateway.findById(1L)).thenReturn(Optional.of(owner(1L)));
            when(gateway.findByEmail("joao@restaurante.com")).thenReturn(Optional.of(owner(1L)));
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            User result = useCase().execute(1L, new UpdateUserCommand("João P. Silva", "joao@restaurante.com",
                    "joao.p", null, new RestaurantOwnerDetails("Grupo Novo", "12.345.678/0001-90")));

            assertThat(result.getName()).isEqualTo("João P. Silva");
            assertThat(result.getAddress()).isEqualTo(ADDRESS);
            assertThat(result.getUserType()).isEqualTo(OWNER_TYPE);
        }

        @Test
        @DisplayName("responde não encontrado para id de outro perfil")
        void shouldRejectOtherProfile() {
            when(gateway.findById(1L)).thenReturn(Optional.of(owner(1L)));

            assertThatThrownBy(() -> useCase().execute(1L, new UpdateUserCommand("Maria", "m@x.com", "maria", null,
                    CUSTOMER_DETAILS)))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("1");
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("responde não encontrado para id inexistente")
        void shouldRejectMissing() {
            when(gateway.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase().execute(99L, new UpdateUserCommand("Maria", "m@x.com", "maria",
                    null, CUSTOMER_DETAILS)))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("recusa e-mail que pertence a outro usuário")
        void shouldRejectEmailOfAnotherUser() {
            when(gateway.findById(1L)).thenReturn(Optional.of(customer(1L)));
            when(gateway.findByEmail("outro@x.com")).thenReturn(Optional.of(customer(2L)));

            assertThatThrownBy(() -> useCase().execute(1L, new UpdateUserCommand("Maria", "outro@x.com", "maria",
                    null, CUSTOMER_DETAILS)))
                    .isInstanceOf(EmailAlreadyExistsException.class);
            verify(gateway, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar e listar")
    class Read {

        @Test
        @DisplayName("busca de qualquer perfil ou só do perfil pedido")
        void shouldFindByIdAndProfile() {
            when(gateway.findById(1L)).thenReturn(Optional.of(owner(1L)));
            FindUserByIdUseCase useCase = new FindUserByIdUseCase(gateway);

            assertThat(useCase.execute(1L).getId()).isEqualTo(1L);
            assertThat(useCase.execute(1L, UserProfile.RESTAURANT_OWNER).getId()).isEqualTo(1L);
            assertThatThrownBy(() -> useCase.execute(1L, UserProfile.CUSTOMER))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("lista repassando o nome aparado e trocando nulo por vazio")
        void shouldList() {
            PageQuery query = new PageQuery(0, 20);
            PageResult<User> page = new PageResult<>(List.of(customer(1L)), 0, 20, 1);
            when(gateway.findAll("Maria", UserProfile.CUSTOMER, query)).thenReturn(page);
            when(gateway.findAll("", null, query)).thenReturn(page);
            ListUsersUseCase useCase = new ListUsersUseCase(gateway);

            assertThat(useCase.execute("  Maria ", UserProfile.CUSTOMER, query).content()).hasSize(1);
            assertThat(useCase.execute(null, null, query).totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Trocar a senha")
    class ChangePassword {

        private ChangePasswordUseCase useCase() {
            return new ChangePasswordUseCase(gateway, passwordHasher);
        }

        @Test
        @DisplayName("grava o hash da nova senha quando a atual confere")
        void shouldChangePassword() {
            when(gateway.findById(1L)).thenReturn(Optional.of(customer(1L)));
            when(passwordHasher.matches(PASSWORD, HASH)).thenReturn(true);
            when(passwordHasher.hash("NovaSenha@456")).thenReturn("novo-hash");

            useCase().execute(1L, UserProfile.CUSTOMER, PASSWORD, "NovaSenha@456", "NovaSenha@456");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(gateway).save(captor.capture());
            assertThat(captor.getValue().getPasswordHash()).isEqualTo("novo-hash");
        }

        @Test
        @DisplayName("recusa quando a senha atual está errada")
        void shouldRejectWrongCurrentPassword() {
            when(gateway.findById(1L)).thenReturn(Optional.of(customer(1L)));
            when(passwordHasher.matches("Errada@123", HASH)).thenReturn(false);

            assertThatThrownBy(() -> useCase().execute(1L, null, "Errada@123", "NovaSenha@456", "NovaSenha@456"))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("atual");
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("recusa quando a confirmação não confere")
        void shouldRejectMismatchedConfirmation() {
            when(gateway.findById(1L)).thenReturn(Optional.of(customer(1L)));
            when(passwordHasher.matches(PASSWORD, HASH)).thenReturn(true);

            assertThatThrownBy(() -> useCase().execute(1L, null, PASSWORD, "NovaSenha@456", "Outra@456"))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("confirmação");
        }

        @Test
        @DisplayName("recusa nova senha fraca")
        void shouldRejectWeakNewPassword() {
            when(gateway.findById(1L)).thenReturn(Optional.of(customer(1L)));
            when(passwordHasher.matches(PASSWORD, HASH)).thenReturn(true);

            assertThatThrownBy(() -> useCase().execute(1L, null, PASSWORD, "fraca", "fraca"))
                    .isInstanceOf(DomainValidationException.class);
        }

        @Test
        @DisplayName("responde não encontrado para id de outro perfil")
        void shouldRejectOtherProfile() {
            when(gateway.findById(1L)).thenReturn(Optional.of(customer(1L)));

            assertThatThrownBy(() -> useCase().execute(1L, UserProfile.RESTAURANT_OWNER, PASSWORD, "a", "a"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Excluir")
    class Delete {

        private DeleteUserUseCase useCase() {
            return new DeleteUserUseCase(gateway, restaurantGateway);
        }

        @Test
        @DisplayName("exclui quem não é dono de restaurante")
        void shouldDelete() {
            when(gateway.findById(1L)).thenReturn(Optional.of(customer(1L)));

            useCase().execute(1L, UserProfile.CUSTOMER);

            verify(gateway).deleteById(1L);
        }

        @Test
        @DisplayName("recusa excluir o responsável por restaurantes")
        void shouldRefuseOwnerOfRestaurants() {
            when(gateway.findById(1L)).thenReturn(Optional.of(owner(1L)));
            when(restaurantGateway.existsByOwnerId(1L)).thenReturn(true);

            assertThatThrownBy(() -> useCase().execute(1L, UserProfile.RESTAURANT_OWNER))
                    .isInstanceOf(UserOwnsRestaurantsException.class);
            verify(gateway, never()).deleteById(any());
        }

        @Test
        @DisplayName("responde não encontrado para id de outro perfil")
        void shouldRejectOtherProfile() {
            when(gateway.findById(1L)).thenReturn(Optional.of(owner(1L)));

            assertThatThrownBy(() -> useCase().execute(1L, UserProfile.CUSTOMER))
                    .isInstanceOf(UserNotFoundException.class);
            verify(gateway, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Autenticar")
    class Authenticate {

        private AuthenticateUserUseCase useCase() {
            return new AuthenticateUserUseCase(gateway, passwordHasher);
        }

        @Test
        @DisplayName("devolve o usuário quando login e senha conferem")
        void shouldAuthenticate() {
            when(gateway.findByLogin("joao.silva")).thenReturn(Optional.of(owner(1L)));
            when(passwordHasher.matches(PASSWORD, HASH)).thenReturn(true);

            assertThat(useCase().execute(" joao.silva ", PASSWORD).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("mesmo erro para senha errada e para login inexistente")
        void shouldRejectInvalidCredentials() {
            when(gateway.findByLogin("joao.silva")).thenReturn(Optional.of(owner(1L)));
            when(passwordHasher.matches("Errada@1", HASH)).thenReturn(false);
            when(gateway.findByLogin("ninguem")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase().execute("joao.silva", "Errada@1"))
                    .isInstanceOf(InvalidCredentialsException.class);
            assertThatThrownBy(() -> useCase().execute("ninguem", PASSWORD))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("recusa login ou senha ausentes sem consultar o banco")
        void shouldRejectBlankCredentials() {
            assertThatThrownBy(() -> useCase().execute(" ", PASSWORD)).isInstanceOf(InvalidCredentialsException.class);
            assertThatThrownBy(() -> useCase().execute(null, PASSWORD)).isInstanceOf(InvalidCredentialsException.class);
            assertThatThrownBy(() -> useCase().execute("joao", null)).isInstanceOf(InvalidCredentialsException.class);
            verify(gateway, never()).findByLogin(any());
        }
    }
}
