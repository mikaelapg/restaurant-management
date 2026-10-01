package com.restaurant.management.core.application.usertype;

import com.restaurant.management.core.application.exception.UserTypeAlreadyExistsException;
import com.restaurant.management.core.application.exception.UserTypeNotFoundException;
import com.restaurant.management.core.domain.usertype.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes dos casos de uso com o gateway mockado.
 * <p>
 * Como os casos de uso dependem só da interface {@link UserTypeGateway}, o teste
 * entrega um mock no construtor e controla o que o "banco" responde. Nada de Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Casos de uso de tipo de usuário")
class UserTypeUseCasesTest {

    @Mock
    private UserTypeGateway gateway;

    private static UserType saved(Long id, String name) {
        return UserType.restore(id, name);
    }

    @Nested
    @DisplayName("Criar")
    class Create {

        @Test
        @DisplayName("salva quando o nome está livre")
        void shouldSaveWhenNameIsFree() {
            when(gateway.findByNameIgnoreCase("Cliente")).thenReturn(Optional.empty());
            when(gateway.save(any())).thenReturn(saved(1L, "Cliente"));

            UserType result = new CreateUserTypeUseCase(gateway).execute("  Cliente ");

            ArgumentCaptor<UserType> captor = ArgumentCaptor.forClass(UserType.class);
            verify(gateway).save(captor.capture());
            assertThat(captor.getValue().getId()).isNull();
            assertThat(captor.getValue().getName()).isEqualTo("Cliente");
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("recusa nome que já existe, sem salvar")
        void shouldRejectDuplicateName() {
            when(gateway.findByNameIgnoreCase("cliente")).thenReturn(Optional.of(saved(1L, "Cliente")));

            assertThatThrownBy(() -> new CreateUserTypeUseCase(gateway).execute("cliente"))
                    .isInstanceOf(UserTypeAlreadyExistsException.class)
                    .hasMessageContaining("cliente");
            verify(gateway, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar por id")
    class FindById {

        @Test
        @DisplayName("devolve o tipo encontrado")
        void shouldReturnFound() {
            when(gateway.findById(1L)).thenReturn(Optional.of(saved(1L, "Cliente")));

            assertThat(new FindUserTypeByIdUseCase(gateway).execute(1L).getName()).isEqualTo("Cliente");
        }

        @Test
        @DisplayName("lança não encontrado quando o id não existe")
        void shouldThrowWhenMissing() {
            when(gateway.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new FindUserTypeByIdUseCase(gateway).execute(99L))
                    .isInstanceOf(UserTypeNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Listar")
    class ListAll {

        @Test
        @DisplayName("devolve o que o gateway devolve")
        void shouldReturnAll() {
            when(gateway.findAll()).thenReturn(List.of(saved(2L, "Cliente"), saved(1L, "Dono de Restaurante")));

            assertThat(new ListUserTypesUseCase(gateway).execute())
                    .extracting(UserType::getName)
                    .containsExactly("Cliente", "Dono de Restaurante");
        }
    }

    @Nested
    @DisplayName("Atualizar")
    class Update {

        @Test
        @DisplayName("renomeia mantendo o id")
        void shouldRename() {
            when(gateway.findById(1L)).thenReturn(Optional.of(saved(1L, "Cliente")));
            when(gateway.findByNameIgnoreCase("Consumidor")).thenReturn(Optional.empty());
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            UserType result = new UpdateUserTypeUseCase(gateway).execute(1L, "Consumidor");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Consumidor");
        }

        @Test
        @DisplayName("aceita o próprio nome, mesmo mudando só maiúsculas")
        void shouldAllowSameNameOnSameType() {
            when(gateway.findById(1L)).thenReturn(Optional.of(saved(1L, "cliente")));
            when(gateway.findByNameIgnoreCase("Cliente")).thenReturn(Optional.of(saved(1L, "cliente")));
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            assertThat(new UpdateUserTypeUseCase(gateway).execute(1L, "Cliente").getName()).isEqualTo("Cliente");
        }

        @Test
        @DisplayName("recusa nome que pertence a outro tipo")
        void shouldRejectNameOfAnotherType() {
            when(gateway.findById(1L)).thenReturn(Optional.of(saved(1L, "Cliente")));
            when(gateway.findByNameIgnoreCase("Dono de Restaurante"))
                    .thenReturn(Optional.of(saved(2L, "Dono de Restaurante")));

            assertThatThrownBy(() -> new UpdateUserTypeUseCase(gateway).execute(1L, "Dono de Restaurante"))
                    .isInstanceOf(UserTypeAlreadyExistsException.class);
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("lança não encontrado quando o id não existe")
        void shouldThrowWhenMissing() {
            when(gateway.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new UpdateUserTypeUseCase(gateway).execute(99L, "Qualquer"))
                    .isInstanceOf(UserTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Excluir")
    class Delete {

        @Test
        @DisplayName("exclui quando o tipo existe")
        void shouldDelete() {
            when(gateway.findById(1L)).thenReturn(Optional.of(saved(1L, "Cliente")));

            new DeleteUserTypeUseCase(gateway).execute(1L);

            verify(gateway).deleteById(1L);
        }

        @Test
        @DisplayName("lança não encontrado e não exclui nada quando o id não existe")
        void shouldThrowWhenMissing() {
            when(gateway.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new DeleteUserTypeUseCase(gateway).execute(99L))
                    .isInstanceOf(UserTypeNotFoundException.class);
            verify(gateway, never()).deleteById(any());
        }
    }
}
