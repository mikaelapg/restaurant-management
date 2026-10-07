package com.restaurant.management.integration;

import com.restaurant.management.domain.repository.UserRepository;
import com.restaurant.management.infrastructure.persistence.menuitem.MenuItemJpaEntity;
import com.restaurant.management.infrastructure.persistence.menuitem.MenuItemJpaRepository;
import com.restaurant.management.infrastructure.persistence.restaurant.RestaurantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integração de ponta a ponta do cardápio contra um PostgreSQL real: itens aninhados
 * ao restaurante, unicidade de nome por restaurante e exclusão em cascata.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Integração — CRUD do cardápio (PostgreSQL real)")
class MenuItemApiIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuItemJpaRepository menuItemRepository;

    @Autowired
    private RestaurantJpaRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    private Long cantina;
    private Long sushiBar;

    @BeforeEach
    void setUp() throws Exception {
        menuItemRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        mockMvc.perform(post("/api/v1/restaurant-owners").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Giulia Rossi", "email": "giulia@it.com", "login": "giulia.it", "password": "Senha@123"}
                        """))
                .andExpect(status().isCreated());
        Long ownerId = userRepository.findByLoginIgnoreCase("giulia.it").orElseThrow().getId();
        cantina = createRestaurant("Cantina", ownerId);
        sushiBar = createRestaurant("Sushi Bar", ownerId);
    }

    private Long createRestaurant(String name, Long ownerId) throws Exception {
        mockMvc.perform(post("/api/v1/restaurants").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "%s",
                         "address": {"street": "Rua A", "number": "1", "city": "São Paulo", "state": "SP", "zipCode": "01000-000"},
                         "cuisineType": "Variada", "openingHours": {"opensAt": "11:00", "closesAt": "23:00"},
                         "ownerId": %d}
                        """.formatted(name, ownerId)))
                .andExpect(status().isCreated());
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getName().equals(name)).findFirst().orElseThrow().getId();
    }

    private static String item(String name, String price) {
        return """
                {"name": "%s", "description": "Prato da casa", "price": %s, "dineInOnly": false,
                 "photoPath": "/images/pratos/%s.jpg"}
                """.formatted(name, price, name.toLowerCase());
    }

    private String base(Long restaurantId) {
        return "/api/v1/restaurants/" + restaurantId + "/menu-items";
    }

    private Long itemId(Long restaurantId, String name) {
        return menuItemRepository.findByRestaurantIdAndNameIgnoreCase(restaurantId, name).orElseThrow().getId();
    }

    @Test
    @DisplayName("ciclo completo: cria, lista, atualiza e exclui, com preço gravado em duas casas")
    void shouldRunFullCrud() throws Exception {
        mockMvc.perform(post(base(cantina)).contentType(MediaType.APPLICATION_JSON).content(item("Risoto", "68.9")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(68.90));
        Long id = itemId(cantina, "Risoto");
        assertThat(menuItemRepository.findById(id).orElseThrow().getPrice()).isEqualTo(new BigDecimal("68.90"));

        mockMvc.perform(post(base(cantina)).contentType(MediaType.APPLICATION_JSON).content(item("Lasanha", "59")))
                .andExpect(status().isCreated());
        mockMvc.perform(get(base(cantina)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Lasanha"))
                .andExpect(jsonPath("$.content[1].name").value("Risoto"));

        mockMvc.perform(put(base(cantina) + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(item("Risoto de funghi", "72.50")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Risoto de funghi"));

        mockMvc.perform(delete(base(cantina) + "/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get(base(cantina) + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("nome é único por restaurante, não no sistema todo")
    void shouldScopeUniquenessToRestaurant() throws Exception {
        mockMvc.perform(post(base(cantina)).contentType(MediaType.APPLICATION_JSON).content(item("Risoto", "10")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(base(cantina)).contentType(MediaType.APPLICATION_JSON).content(item("RISOTO", "10")))
                .andExpect(status().isConflict());
        mockMvc.perform(post(base(sushiBar)).contentType(MediaType.APPLICATION_JSON).content(item("Risoto", "10")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("item de um restaurante não é acessível pelo caminho de outro")
    void shouldNotLeakAcrossRestaurants() throws Exception {
        mockMvc.perform(post(base(cantina)).contentType(MediaType.APPLICATION_JSON).content(item("Risoto", "10")))
                .andExpect(status().isCreated());
        Long id = itemId(cantina, "Risoto");

        mockMvc.perform(get(base(sushiBar) + "/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(delete(base(sushiBar) + "/" + id)).andExpect(status().isNotFound());
        assertThat(menuItemRepository.existsById(id)).isTrue();
    }

    @Test
    @DisplayName("excluir o restaurante exclui o cardápio dele")
    void shouldCascadeOnRestaurantDelete() throws Exception {
        mockMvc.perform(post(base(cantina)).contentType(MediaType.APPLICATION_JSON).content(item("Risoto", "10")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/restaurants/" + cantina)).andExpect(status().isNoContent());

        assertThat(menuItemRepository.count()).isZero();
        mockMvc.perform(get(base(cantina))).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("o banco recusa preço não positivo e nome repetido no mesmo restaurante")
    void shouldEnforceConstraintsInDatabase() {
        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(
                new MenuItemJpaEntity(null, cantina, "Suco", "Laranja", BigDecimal.ZERO, false, null)))
                .isInstanceOf(DataIntegrityViolationException.class);

        menuItemRepository.saveAndFlush(new MenuItemJpaEntity(null, cantina, "Suco", "Laranja", BigDecimal.ONE, false, null));
        assertThatThrownBy(() -> menuItemRepository.saveAndFlush(
                new MenuItemJpaEntity(null, cantina, "SUCO", "Uva", BigDecimal.ONE, false, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
