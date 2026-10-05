package com.restaurant.management.core.application.restaurant;

import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.exception.UserNotFoundException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.domain.restaurant.OpeningHours;
import com.restaurant.management.core.domain.restaurant.Restaurant;
import com.restaurant.management.core.domain.shared.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Casos de uso de restaurante")
class RestaurantUseCasesTest {

    private static final Address ADDRESS = new Address("Rua Augusta", "1500", null, "São Paulo", "SP", "01305-100");
    private static final OpeningHours HOURS = new OpeningHours(LocalTime.of(11, 0), LocalTime.of(23, 0));

    @Mock
    private RestaurantGateway gateway;

    @Mock
    private UserGateway userGateway;

    private static RestaurantCommand command(Long ownerId) {
        return new RestaurantCommand("Cantina", ADDRESS, "Italiana", HOURS, ownerId);
    }

    private static Restaurant saved(Long id) {
        return Restaurant.restore(id, "Cantina", ADDRESS, "Italiana", HOURS, 1L);
    }

    @Nested
    @DisplayName("Criar")
    class Create {

        @Test
        @DisplayName("salva quando o dono existe")
        void shouldSave() {
            when(userGateway.existsById(1L)).thenReturn(true);
            when(gateway.save(any())).thenReturn(saved(10L));

            Restaurant result = new CreateRestaurantUseCase(gateway, userGateway).execute(command(1L));

            ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
            verify(gateway).save(captor.capture());
            assertThat(captor.getValue().getId()).isNull();
            assertThat(captor.getValue().getOwnerId()).isEqualTo(1L);
            assertThat(result.getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("lança usuário não encontrado e não salva quando o dono não existe")
        void shouldRejectMissingOwner() {
            when(userGateway.existsById(9L)).thenReturn(false);

            assertThatThrownBy(() -> new CreateRestaurantUseCase(gateway, userGateway).execute(command(9L)))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("9");
            verify(gateway, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar e listar")
    class Read {

        @Test
        @DisplayName("busca por id")
        void shouldFind() {
            when(gateway.findById(10L)).thenReturn(Optional.of(saved(10L)));

            assertThat(new FindRestaurantByIdUseCase(gateway).execute(10L).getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("lança não encontrado para id inexistente")
        void shouldThrowWhenMissing() {
            when(gateway.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new FindRestaurantByIdUseCase(gateway).execute(99L))
                    .isInstanceOf(RestaurantNotFoundException.class);
        }

        @Test
        @DisplayName("lista repassando filtros aparados e trocando nulo por vazio")
        void shouldList() {
            PageQuery query = new PageQuery(0, 20);
            PageResult<Restaurant> page = new PageResult<>(List.of(saved(10L)), 0, 20, 1);
            when(gateway.findAll("Cantina", "", query)).thenReturn(page);

            assertThat(new ListRestaurantsUseCase(gateway).execute(" Cantina ", null, query)).isSameAs(page);
        }
    }

    @Nested
    @DisplayName("Atualizar")
    class Update {

        @Test
        @DisplayName("salva os dados novos mantendo o id")
        void shouldUpdate() {
            when(gateway.findById(10L)).thenReturn(Optional.of(saved(10L)));
            when(userGateway.existsById(2L)).thenReturn(true);
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Restaurant result = new UpdateRestaurantUseCase(gateway, userGateway)
                    .execute(10L, new RestaurantCommand("Cantina Nova", ADDRESS, "Massas", HOURS, 2L));

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo("Cantina Nova");
            assertThat(result.getOwnerId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("lança não encontrado quando o restaurante não existe")
        void shouldThrowWhenRestaurantIsMissing() {
            when(gateway.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new UpdateRestaurantUseCase(gateway, userGateway).execute(99L, command(1L)))
                    .isInstanceOf(RestaurantNotFoundException.class);
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("lança usuário não encontrado quando o novo dono não existe")
        void shouldThrowWhenOwnerIsMissing() {
            when(gateway.findById(10L)).thenReturn(Optional.of(saved(10L)));
            when(userGateway.existsById(9L)).thenReturn(false);

            assertThatThrownBy(() -> new UpdateRestaurantUseCase(gateway, userGateway).execute(10L, command(9L)))
                    .isInstanceOf(UserNotFoundException.class);
            verify(gateway, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Excluir")
    class Delete {

        @Test
        @DisplayName("exclui quando existe")
        void shouldDelete() {
            when(gateway.existsById(10L)).thenReturn(true);

            new DeleteRestaurantUseCase(gateway).execute(10L);

            verify(gateway).deleteById(10L);
        }

        @Test
        @DisplayName("lança não encontrado e não exclui quando não existe")
        void shouldThrowWhenMissing() {
            when(gateway.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> new DeleteRestaurantUseCase(gateway).execute(99L))
                    .isInstanceOf(RestaurantNotFoundException.class);
            verify(gateway, never()).deleteById(any());
        }
    }
}
