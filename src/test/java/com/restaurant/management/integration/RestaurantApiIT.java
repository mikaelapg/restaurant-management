package com.restaurant.management.integration;

import com.restaurant.management.infrastructure.persistence.user.UserJpaRepository;
import com.restaurant.management.infrastructure.persistence.restaurant.RestaurantJpaRepository;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
 * Integração de ponta a ponta do CRUD de restaurantes contra um PostgreSQL real:
 * migration, FK para o dono, mapeamento de horário e endereço, filtros e paginação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Integração — CRUD de restaurantes (PostgreSQL real)")
class RestaurantApiIT {

    private static final String BASE = "/api/v1/restaurants";

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
    private RestaurantJpaRepository restaurantRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private UserTypeJpaRepository userTypeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId;

    @BeforeEach
    void setUp() throws Exception {
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
        userTypeRepository.deleteAll(userTypeRepository.findAll().stream().filter(t -> t.getProfile() == null).toList());
        mockMvc.perform(post("/api/v1/restaurant-owners").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Giulia Rossi", "email": "giulia@it.com", "login": "giulia.it", "password": "Senha@123"}
                        """))
                .andExpect(status().isCreated());
        ownerId = userRepository.findByLoginIgnoreCase("giulia.it").orElseThrow().getId();
    }

    private String body(String name, String cuisineType, Long owner) {
        return """
                {
                  "name": "%s",
                  "address": {"street": "Rua Augusta", "number": "1500", "city": "São Paulo",
                              "state": "SP", "zipCode": "01305-100"},
                  "cuisineType": "%s",
                  "openingHours": {"opensAt": "18:00", "closesAt": "02:00"},
                  "ownerId": %d
                }
                """.formatted(name, cuisineType, owner);
    }

    private Long create(String name, String cuisineType) throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body(name, cuisineType, ownerId)))
                .andExpect(status().isCreated());
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getName().equals(name)).findFirst().orElseThrow().getId();
    }

    @Test
    @DisplayName("ciclo completo: cria, busca, atualiza e exclui")
    void shouldRunFullCrud() throws Exception {
        Long id = create("Cantina da Nonna", "Italiana");

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openingHours.opensAt").value("18:00"))
                .andExpect(jsonPath("$.openingHours.closesAt").value("02:00"))
                .andExpect(jsonPath("$.address.complement").doesNotExist())
                .andExpect(jsonPath("$.ownerId").value(ownerId));

        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Cantina Nova", "Massas", ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cantina Nova"));
        assertThat(restaurantRepository.findById(id).orElseThrow().getCuisineType()).isEqualTo("Massas");

        mockMvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get(BASE + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("lista em ordem alfabética, filtra por tipo de cozinha e pagina")
    void shouldFilterAndPaginate() throws Exception {
        create("Sushi Bar", "Japonesa");
        create("Cantina", "Italiana");
        create("Pizzaria", "Italiana");

        mockMvc.perform(get(BASE).param("cuisineType", "ITAL").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Cantina"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get(BASE).param("name", "sushi"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("dono inexistente responde 404 e nada é gravado")
    void shouldRejectMissingOwner() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("X", "Y", 999_999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-not-found"));
        assertThat(restaurantRepository.count()).isZero();
    }

    @Test
    @DisplayName("cliente não pode ser dono de restaurante: 422 e nada é gravado")
    void shouldRejectCustomerAsOwner() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Maria", "email": "maria@it.com", "login": "maria.it", "password": "Senha@123"}
                        """))
                .andExpect(status().isCreated());
        Long customerId = userRepository.findByLoginIgnoreCase("maria.it").orElseThrow().getId();

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("X", "Y", customerId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/invalid-restaurant-owner"));
        assertThat(restaurantRepository.count()).isZero();

        Long id = create("Cantina", "Italiana");
        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Cantina", "Italiana", customerId)))
                .andExpect(status().isUnprocessableEntity());
        assertThat(restaurantRepository.findById(id).orElseThrow().getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("dono com restaurantes não pode perder o tipo de dono")
    void shouldRefuseTypeChangeOfOwnerWithRestaurants() throws Exception {
        create("Cantina", "Italiana");
        mockMvc.perform(post("/api/v1/user-types").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Gerente\"}"))
                .andExpect(status().isCreated());
        Long managerTypeId = userTypeRepository.findByNameIgnoreCase("Gerente").orElseThrow().getId();

        mockMvc.perform(put("/api/v1/users/" + ownerId + "/user-type").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTypeId\": " + managerTypeId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-change-not-allowed"));
    }

    @Test
    @DisplayName("dono de restaurante não pode ser excluído enquanto tiver restaurantes")
    void shouldRefuseDeletingOwnerWithRestaurants() throws Exception {
        Long id = create("Cantina", "Italiana");

        mockMvc.perform(delete("/api/v1/restaurant-owners/" + ownerId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-owns-restaurants"));

        mockMvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/restaurant-owners/" + ownerId)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("o banco recusa horário de abertura igual ao de fechamento")
    void shouldEnforceOpeningHoursCheckInDatabase() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO restaurants (name, cuisine_type, opens_at, closes_at, owner_id,
                                         street, number, city, state, zip_code)
                VALUES ('X', 'Y', '10:00', '10:00', ?, 'Rua', '1', 'SP', 'SP', '0')
                """, ownerId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
