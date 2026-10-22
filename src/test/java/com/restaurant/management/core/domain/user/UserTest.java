package com.restaurant.management.core.domain.user;

import com.restaurant.management.core.domain.exception.DomainValidationException;
import com.restaurant.management.core.domain.exception.IncompatibleUserTypeException;
import com.restaurant.management.core.domain.shared.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.restaurant.management.support.Fixtures.ADDRESS;
import static com.restaurant.management.support.Fixtures.CUSTOMER_DETAILS;
import static com.restaurant.management.support.Fixtures.CUSTOMER_TYPE;
import static com.restaurant.management.support.Fixtures.CUSTOM_TYPE;
import static com.restaurant.management.support.Fixtures.HASH;
import static com.restaurant.management.support.Fixtures.MODIFIED_AT;
import static com.restaurant.management.support.Fixtures.OWNER_DETAILS;
import static com.restaurant.management.support.Fixtures.OWNER_TYPE;
import static com.restaurant.management.support.Fixtures.customer;
import static com.restaurant.management.support.Fixtures.owner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User (domínio)")
class UserTest {

    private static User create(String name, String email, String login) {
        return User.create(name, email, login, HASH, null, CUSTOMER_DETAILS, CUSTOMER_TYPE);
    }

    @Nested
    @DisplayName("Criação")
    class Creation {

        @Test
        @DisplayName("apara os textos, põe o e-mail em minúsculas e nasce sem id")
        void shouldNormalize() {
            User user = User.create("  Maria  ", " Maria@Email.COM ", " maria ", HASH, ADDRESS,
                    CUSTOMER_DETAILS, CUSTOMER_TYPE);

            assertThat(user.getId()).isNull();
            assertThat(user.getName()).isEqualTo("Maria");
            assertThat(user.getEmail()).isEqualTo("maria@email.com");
            assertThat(user.getLogin()).isEqualTo("maria");
            assertThat(user.getPasswordHash()).isEqualTo(HASH);
            assertThat(user.getAddress()).isEqualTo(ADDRESS);
            assertThat(user.getProfile()).isEqualTo(UserProfile.CUSTOMER);
            assertThat(user.getDetails()).isEqualTo(CUSTOMER_DETAILS);
            assertThat(user.getUserType()).isEqualTo(CUSTOMER_TYPE);
            assertThat(user.getLastModifiedAt()).isNull();
        }

        @Test
        @DisplayName("deduz o perfil dos dados do perfil")
        void shouldDeriveProfileFromDetails() {
            User user = User.create("João", "joao@x.com", "joao", HASH, null, OWNER_DETAILS, OWNER_TYPE);

            assertThat(user.getProfile()).isEqualTo(UserProfile.RESTAURANT_OWNER);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "A"})
        @DisplayName("recusa nome vazio ou curto demais")
        void shouldRejectInvalidName(String name) {
            assertThatThrownBy(() -> create(name, "a@b.com", "login"))
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessageContaining("nome");
        }

        @Test
        @DisplayName("recusa nome com mais de 255 caracteres")
        void shouldRejectLongName() {
            assertThatThrownBy(() -> create("a".repeat(256), "a@b.com", "login"))
                    .isInstanceOf(DomainValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"sem-arroba", "a@b@c", "com espaco@x.com"})
        @DisplayName("recusa e-mail sem o formato mínimo")
        void shouldRejectInvalidEmail(String email) {
            assertThatThrownBy(() -> create("Maria", email, "login"))
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessageContaining("e-mail");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"ab"})
        @DisplayName("recusa login vazio ou curto demais")
        void shouldRejectInvalidLogin(String login) {
            assertThatThrownBy(() -> create("Maria", "a@b.com", login))
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessageContaining("login");
        }

        @Test
        @DisplayName("exige o hash da senha")
        void shouldRequirePasswordHash() {
            assertThatThrownBy(() -> User.create("Maria", "a@b.com", "maria", " ", null, CUSTOMER_DETAILS, null))
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessageContaining("senha");
        }

        @Test
        @DisplayName("exige os dados do perfil")
        void shouldRequireDetails() {
            assertThatThrownBy(() -> User.create("Maria", "a@b.com", "maria", HASH, null, null, null))
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessageContaining("perfil");
        }

        @Test
        @DisplayName("recusa nascer com o tipo de sistema de outro perfil")
        void shouldRejectIncompatibleType() {
            assertThatThrownBy(() -> User.create("Maria", "a@b.com", "maria", HASH, null, CUSTOMER_DETAILS, OWNER_TYPE))
                    .isInstanceOf(IncompatibleUserTypeException.class);
        }

        @Test
        @DisplayName("restaurar exige id e mantém a data da última alteração")
        void shouldRestore() {
            assertThatThrownBy(() -> User.restore(null, "Maria", "a@b.com", "maria", HASH, null,
                    CUSTOMER_DETAILS, null, null))
                    .isInstanceOf(NullPointerException.class);

            assertThat(customer(7L).getId()).isEqualTo(7L);
            assertThat(customer(7L).getLastModifiedAt()).isEqualTo(MODIFIED_AT);
        }
    }

    @Nested
    @DisplayName("Alterações")
    class Changes {

        @Test
        @DisplayName("atualizar dados mantém id, senha e tipo")
        void shouldUpdateData() {
            User current = owner(1L);
            Address newAddress = new Address("Av. Paulista", "1000", null, "São Paulo", "SP", "01310-100");

            User updated = current.updateData("João P. Silva", "NOVO@x.com", "joao.p", newAddress,
                    new RestaurantOwnerDetails("Outro Grupo", null));

            assertThat(updated.getId()).isEqualTo(1L);
            assertThat(updated.getName()).isEqualTo("João P. Silva");
            assertThat(updated.getEmail()).isEqualTo("novo@x.com");
            assertThat(updated.getAddress()).isEqualTo(newAddress);
            assertThat(updated.getPasswordHash()).isEqualTo(current.getPasswordHash());
            assertThat(updated.getUserType()).isEqualTo(OWNER_TYPE);
            assertThat(((RestaurantOwnerDetails) updated.getDetails()).cnpj()).isNull();
        }

        @Test
        @DisplayName("atualizar dados não troca o perfil")
        void shouldNotChangeProfile() {
            assertThatThrownBy(() -> owner(1L).updateData("João", "j@x.com", "joao", null, CUSTOMER_DETAILS))
                    .isInstanceOf(DomainValidationException.class)
                    .hasMessageContaining("perfil");
        }

        @Test
        @DisplayName("trocar a senha só muda o hash")
        void shouldChangePassword() {
            User updated = customer(1L).changePassword("novo-hash");

            assertThat(updated.getPasswordHash()).isEqualTo("novo-hash");
            assertThat(updated.getEmail()).isEqualTo("maria@email.com");
        }

        @Test
        @DisplayName("aceita tipo comum em qualquer perfil e tipo de sistema do mesmo perfil")
        void shouldAssignCompatibleTypes() {
            assertThat(customer(1L).assignType(CUSTOM_TYPE).getUserType()).isEqualTo(CUSTOM_TYPE);
            assertThat(owner(1L, CUSTOM_TYPE).assignType(OWNER_TYPE).getUserType()).isEqualTo(OWNER_TYPE);
        }

        @Test
        @DisplayName("recusa tipo de sistema de outro perfil, dizendo qual")
        void shouldRejectIncompatibleAssignment() {
            assertThatThrownBy(() -> owner(1L).assignType(CUSTOMER_TYPE))
                    .isInstanceOf(IncompatibleUserTypeException.class)
                    .hasMessageContaining("Cliente")
                    .hasMessageContaining("RESTAURANT_OWNER");
        }

        @Test
        @DisplayName("exige um tipo ao associar")
        void shouldRequireType() {
            assertThatThrownBy(() -> customer(1L).assignType(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Pode ser dono de restaurante")
    class CanOwnRestaurants {

        @Test
        @DisplayName("só com o tipo de sistema dos donos")
        void shouldDependOnOwnerType() {
            assertThat(owner(1L).canOwnRestaurants()).isTrue();
            assertThat(owner(1L, CUSTOM_TYPE).canOwnRestaurants()).isFalse();
            assertThat(owner(1L, null).canOwnRestaurants()).isFalse();
            assertThat(customer(1L).canOwnRestaurants()).isFalse();
        }
    }

    @Test
    @DisplayName("igualdade pelo id")
    void shouldCompareById() {
        assertThat(customer(1L)).isEqualTo(customer(1L)).isNotEqualTo(customer(2L)).isNotEqualTo("x");
        assertThat(customer(1L).hashCode()).isEqualTo(customer(2L).hashCode());
        User unsaved = create("Maria", "a@b.com", "maria");
        assertThat(unsaved).isEqualTo(unsaved).isNotEqualTo(create("Maria", "a@b.com", "maria"));
    }
}
