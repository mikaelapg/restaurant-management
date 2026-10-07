package com.restaurant.management.core.application.menuitem;

import com.restaurant.management.core.application.exception.MenuItemAlreadyExistsException;
import com.restaurant.management.core.application.exception.MenuItemNotFoundException;
import com.restaurant.management.core.application.exception.RestaurantNotFoundException;
import com.restaurant.management.core.application.pagination.PageQuery;
import com.restaurant.management.core.application.pagination.PageResult;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.domain.menuitem.MenuItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Casos de uso do cardápio")
class MenuItemUseCasesTest {

    private static final Long RESTAURANT = 1L;

    @Mock
    private MenuItemGateway gateway;

    @Mock
    private RestaurantGateway restaurantGateway;

    private static MenuItemCommand command(String name) {
        return new MenuItemCommand(name, "Arroz arbóreo", new BigDecimal("68.90"), false, null);
    }

    private static MenuItem saved(Long id, String name) {
        return MenuItem.restore(id, RESTAURANT, name, "Arroz arbóreo", new BigDecimal("68.90"), false, null);
    }

    @Nested
    @DisplayName("Criar")
    class Create {

        @Test
        @DisplayName("salva quando o restaurante existe e o nome está livre")
        void shouldSave() {
            when(restaurantGateway.existsById(RESTAURANT)).thenReturn(true);
            when(gateway.findByRestaurantIdAndNameIgnoreCase(RESTAURANT, "Risoto")).thenReturn(Optional.empty());
            when(gateway.save(any())).thenReturn(saved(5L, "Risoto"));

            MenuItem result = new CreateMenuItemUseCase(gateway, restaurantGateway).execute(RESTAURANT, command(" Risoto "));

            ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
            verify(gateway).save(captor.capture());
            assertThat(captor.getValue().getId()).isNull();
            assertThat(captor.getValue().getRestaurantId()).isEqualTo(RESTAURANT);
            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("lança restaurante não encontrado")
        void shouldRejectMissingRestaurant() {
            when(restaurantGateway.existsById(9L)).thenReturn(false);

            assertThatThrownBy(() -> new CreateMenuItemUseCase(gateway, restaurantGateway).execute(9L, command("Risoto")))
                    .isInstanceOf(RestaurantNotFoundException.class);
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("lança conflito quando o restaurante já tem item com o nome")
        void shouldRejectDuplicate() {
            when(restaurantGateway.existsById(RESTAURANT)).thenReturn(true);
            when(gateway.findByRestaurantIdAndNameIgnoreCase(RESTAURANT, "Risoto"))
                    .thenReturn(Optional.of(saved(5L, "RISOTO")));

            assertThatThrownBy(() -> new CreateMenuItemUseCase(gateway, restaurantGateway).execute(RESTAURANT, command("Risoto")))
                    .isInstanceOf(MenuItemAlreadyExistsException.class);
            verify(gateway, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar e listar")
    class Read {

        @Test
        @DisplayName("busca o item dentro do restaurante")
        void shouldFind() {
            when(gateway.findByIdAndRestaurantId(5L, RESTAURANT)).thenReturn(Optional.of(saved(5L, "Risoto")));

            assertThat(new FindMenuItemByIdUseCase(gateway).execute(RESTAURANT, 5L).getName()).isEqualTo("Risoto");
        }

        @Test
        @DisplayName("lança não encontrado quando o item não é deste restaurante")
        void shouldThrowWhenMissing() {
            when(gateway.findByIdAndRestaurantId(5L, 2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new FindMenuItemByIdUseCase(gateway).execute(2L, 5L))
                    .isInstanceOf(MenuItemNotFoundException.class)
                    .hasMessageContaining("restaurante 2");
        }

        @Test
        @DisplayName("lista o cardápio do restaurante")
        void shouldList() {
            PageQuery query = new PageQuery(0, 20);
            PageResult<MenuItem> page = new PageResult<>(List.of(saved(5L, "Risoto")), 0, 20, 1);
            when(restaurantGateway.existsById(RESTAURANT)).thenReturn(true);
            when(gateway.findAllByRestaurantId(RESTAURANT, query)).thenReturn(page);

            assertThat(new ListMenuItemsUseCase(gateway, restaurantGateway).execute(RESTAURANT, query)).isSameAs(page);
        }

        @Test
        @DisplayName("listar cardápio de restaurante inexistente lança não encontrado")
        void shouldThrowWhenListingMissingRestaurant() {
            when(restaurantGateway.existsById(9L)).thenReturn(false);

            assertThatThrownBy(() -> new ListMenuItemsUseCase(gateway, restaurantGateway).execute(9L, new PageQuery(0, 20)))
                    .isInstanceOf(RestaurantNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Atualizar")
    class Update {

        @Test
        @DisplayName("permite manter o próprio nome, mudando só a capitalização")
        void shouldAllowOwnName() {
            when(gateway.findByIdAndRestaurantId(5L, RESTAURANT)).thenReturn(Optional.of(saved(5L, "risoto")));
            when(gateway.findByRestaurantIdAndNameIgnoreCase(RESTAURANT, "Risoto")).thenReturn(Optional.of(saved(5L, "risoto")));
            when(gateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            MenuItem result = new UpdateMenuItemUseCase(gateway).execute(RESTAURANT, 5L, command("Risoto"));

            assertThat(result.getName()).isEqualTo("Risoto");
            assertThat(result.getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("lança conflito quando o nome pertence a outro item")
        void shouldRejectNameOfAnotherItem() {
            when(gateway.findByIdAndRestaurantId(5L, RESTAURANT)).thenReturn(Optional.of(saved(5L, "Risoto")));
            when(gateway.findByRestaurantIdAndNameIgnoreCase(RESTAURANT, "Lasanha")).thenReturn(Optional.of(saved(6L, "Lasanha")));

            assertThatThrownBy(() -> new UpdateMenuItemUseCase(gateway).execute(RESTAURANT, 5L, command("Lasanha")))
                    .isInstanceOf(MenuItemAlreadyExistsException.class);
            verify(gateway, never()).save(any());
        }

        @Test
        @DisplayName("lança não encontrado quando o item não existe no restaurante")
        void shouldThrowWhenMissing() {
            when(gateway.findByIdAndRestaurantId(5L, RESTAURANT)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new UpdateMenuItemUseCase(gateway).execute(RESTAURANT, 5L, command("Risoto")))
                    .isInstanceOf(MenuItemNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Excluir")
    class Delete {

        @Test
        @DisplayName("exclui quando o item é do restaurante")
        void shouldDelete() {
            when(gateway.findByIdAndRestaurantId(5L, RESTAURANT)).thenReturn(Optional.of(saved(5L, "Risoto")));

            new DeleteMenuItemUseCase(gateway).execute(RESTAURANT, 5L);

            verify(gateway).deleteById(5L);
        }

        @Test
        @DisplayName("não exclui item de outro restaurante")
        void shouldNotDeleteFromAnotherRestaurant() {
            when(gateway.findByIdAndRestaurantId(5L, 2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> new DeleteMenuItemUseCase(gateway).execute(2L, 5L))
                    .isInstanceOf(MenuItemNotFoundException.class);
            verify(gateway, never()).deleteById(any());
        }
    }
}
