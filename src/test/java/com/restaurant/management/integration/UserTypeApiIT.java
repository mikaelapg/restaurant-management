package com.restaurant.management.integration;

import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.core.domain.usertype.UserType;
import com.restaurant.management.infrastructure.persistence.user.UserJpaRepository;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaEntity;
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
 * Integração de ponta a ponta do CRUD de tipos de usuário e da associação com usuários:
 * HTTP, casos de uso, gateway JPA, migrations Flyway e PostgreSQL real, tudo junto.
 * <p>
 * Os testes unitários provam cada peça isolada; este prova que as peças se encaixam.
 * Roda em {@code mvn verify} e precisa de Docker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Integração — tipos de usuário (PostgreSQL real)")
class UserTypeApiIT {

    private static final String BASE = "/api/v1/user-types";

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
    private UserTypeJpaRepository repository;

    @Autowired
    private UserJpaRepository userRepository;

    /**
     * Antes de cada teste: tira os usuários e os tipos criados pela API, e devolve os nomes
     * originais aos tipos de sistema, que as migrations cadastram e nunca saem.
     */
    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
        repository.deleteAll(repository.findAll().stream().filter(type -> type.getProfile() == null).toList());
        repository.findAll().forEach(type -> {
            type.setName(type.getProfile() == UserProfile.RESTAURANT_OWNER ? UserType.RESTAURANT_OWNER : UserType.CUSTOMER);
            repository.save(type);
        });
    }

    private static String body(String name) {
        return "{\"name\": \"" + name + "\"}";
    }

    private Long idOf(String name) {
        return repository.findByNameIgnoreCase(name).orElseThrow().getId();
    }

    @Test
    @DisplayName("as migrations cadastram \"Dono de Restaurante\" e \"Cliente\" como tipos de sistema")
    void shouldSeedSystemTypes() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cliente"))
                .andExpect(jsonPath("$[0].systemType").value(true))
                .andExpect(jsonPath("$[0].profile").value("CUSTOMER"))
                .andExpect(jsonPath("$[1].name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$[1].profile").value("RESTAURANT_OWNER"));
    }

    @Test
    @DisplayName("ciclo completo de um tipo criado pela API: cria, busca, lista, renomeia e exclui")
    void shouldRunFullCrud() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Gerente")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.systemType").value(false));
        Long id = idOf("Gerente");

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gerente"));
        mockMvc.perform(get(BASE))
                .andExpect(jsonPath("$[1].name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$[2].name").value("Gerente"));

        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body("Supervisor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Supervisor"));
        assertThat(repository.findById(id).orElseThrow().getName()).isEqualTo("Supervisor");

        mockMvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get(BASE + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("nome repetido com outra capitalização responde 409")
    void shouldRejectDuplicateIgnoringCase() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("CLIENTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-conflict"));
    }

    @Test
    @DisplayName("tipo de sistema pode ser renomeado, continua ligado ao perfil e não pode ser excluído")
    void shouldProtectSystemTypes() throws Exception {
        Long clienteId = idOf("Cliente");

        mockMvc.perform(put(BASE + "/" + clienteId).contentType(MediaType.APPLICATION_JSON).content(body("Consumidor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile").value("CUSTOMER"));

        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Maria", "email": "maria@it.com", "login": "maria.it", "password": "Senha@123"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType.name").value("Consumidor"));

        mockMvc.perform(delete(BASE + "/" + clienteId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/system-user-type"));
    }

    @Test
    @DisplayName("o índice do banco barra duplicatas mesmo se o caso de uso for contornado")
    void shouldEnforceUniquenessInDatabase() {
        repository.saveAndFlush(new UserTypeJpaEntity(null, "Gerente"));

        assertThatThrownBy(() -> repository.saveAndFlush(new UserTypeJpaEntity(null, "gerente")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("associação: troca para tipo comum, tipo em uso não sai e tipo de outro perfil é recusado")
    void shouldAssociateUsersWithUserTypes() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Maria", "email": "maria@it.com", "login": "maria.it", "password": "Senha@123"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType.id").value(idOf("Cliente")));
        Long customerId = userRepository.findByLoginIgnoreCase("maria.it").orElseThrow().getId();

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Cliente VIP")))
                .andExpect(status().isCreated());
        Long vipId = idOf("Cliente VIP");

        mockMvc.perform(put("/api/v1/users/" + customerId + "/user-type")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userTypeId\": " + vipId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType.name").value("Cliente VIP"));

        mockMvc.perform(delete(BASE + "/" + vipId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/user-type-in-use"));

        mockMvc.perform(put("/api/v1/users/" + customerId + "/user-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTypeId\": " + idOf("Dono de Restaurante") + "}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/incompatible-user-type"));
    }
}
