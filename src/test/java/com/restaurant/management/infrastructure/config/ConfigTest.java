package com.restaurant.management.infrastructure.config;

import com.restaurant.management.core.application.menuitem.MenuItemGateway;
import com.restaurant.management.core.application.restaurant.RestaurantGateway;
import com.restaurant.management.core.application.user.PasswordHasher;
import com.restaurant.management.core.application.user.UserGateway;
import com.restaurant.management.core.application.usertype.UserTypeGateway;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * As classes de configuração só montam os casos de uso com os gateways. Este teste
 * confere que cada fábrica devolve um caso de uso pronto, sem subir o Spring; a montagem
 * real, com os beans de verdade, é provada pelos testes de integração.
 */
@DisplayName("Configuração dos casos de uso")
class ConfigTest {

    private final UserGateway users = mock(UserGateway.class);
    private final UserTypeGateway userTypes = mock(UserTypeGateway.class);
    private final RestaurantGateway restaurants = mock(RestaurantGateway.class);
    private final MenuItemGateway menuItems = mock(MenuItemGateway.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);

    @Test
    @DisplayName("usuários e autenticação")
    void shouldBuildUserUseCases() {
        UserConfig config = new UserConfig();

        assertThat(config.createUserUseCase(users, userTypes, hasher)).isNotNull();
        assertThat(config.updateUserUseCase(users)).isNotNull();
        assertThat(config.findUserByIdUseCase(users)).isNotNull();
        assertThat(config.listUsersUseCase(users)).isNotNull();
        assertThat(config.changePasswordUseCase(users, hasher)).isNotNull();
        assertThat(config.deleteUserUseCase(users, restaurants)).isNotNull();
        assertThat(config.authenticateUserUseCase(users, hasher)).isNotNull();
    }

    @Test
    @DisplayName("tipos de usuário")
    void shouldBuildUserTypeUseCases() {
        UserTypeConfig config = new UserTypeConfig();

        assertThat(config.createUserTypeUseCase(userTypes)).isNotNull();
        assertThat(config.findUserTypeByIdUseCase(userTypes)).isNotNull();
        assertThat(config.listUserTypesUseCase(userTypes)).isNotNull();
        assertThat(config.updateUserTypeUseCase(userTypes)).isNotNull();
        assertThat(config.deleteUserTypeUseCase(userTypes, users)).isNotNull();
        assertThat(config.assignUserTypeUseCase(users, userTypes, restaurants)).isNotNull();
    }

    @Test
    @DisplayName("restaurantes e cardápio")
    void shouldBuildRestaurantAndMenuUseCases() {
        RestaurantConfig restaurantConfig = new RestaurantConfig();
        MenuItemConfig menuConfig = new MenuItemConfig();

        assertThat(restaurantConfig.createRestaurantUseCase(restaurants, users)).isNotNull();
        assertThat(restaurantConfig.findRestaurantByIdUseCase(restaurants)).isNotNull();
        assertThat(restaurantConfig.listRestaurantsUseCase(restaurants)).isNotNull();
        assertThat(restaurantConfig.updateRestaurantUseCase(restaurants, users)).isNotNull();
        assertThat(restaurantConfig.deleteRestaurantUseCase(restaurants)).isNotNull();

        assertThat(menuConfig.createMenuItemUseCase(menuItems, restaurants)).isNotNull();
        assertThat(menuConfig.findMenuItemByIdUseCase(menuItems)).isNotNull();
        assertThat(menuConfig.listMenuItemsUseCase(menuItems, restaurants)).isNotNull();
        assertThat(menuConfig.updateMenuItemUseCase(menuItems)).isNotNull();
        assertThat(menuConfig.deleteMenuItemUseCase(menuItems)).isNotNull();
    }

    @Test
    @DisplayName("documentação OpenAPI com o schema de erro RFC 7807")
    void shouldBuildOpenApi() {
        OpenAPI openApi = new OpenApiConfig().restaurantManagementOpenAPI();

        assertThat(openApi.getInfo().getTitle()).contains("Restaurantes");
        assertThat(openApi.getComponents().getSchemas()).containsKey("ProblemDetail");
        assertThat(openApi.getServers()).singleElement().extracting(server -> server.getUrl()).isEqualTo("/");
    }
}
