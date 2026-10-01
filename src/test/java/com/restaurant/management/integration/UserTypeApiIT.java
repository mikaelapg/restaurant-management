package com.restaurant.management.integration;

import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaEntity;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaRepository;
import org.junit.jupiter.api.AfterEach;
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
 * Integração de ponta a ponta do CRUD de tipos de usuário: HTTP, casos de uso,
 * gateway JPA, migration Flyway e PostgreSQL real, tudo junto.
 * <p>
 * Os testes unitários provam cada peça isolada; este prova que as peças se encaixam.
 * Roda em {@code mvn verify} e precisa de Docker.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Integração — CRUD de tipos de usuário (PostgreSQL real)")
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

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    private static String body(String name) {
        return "{\"name\": \"" + name + "\"}";
    }

    @Test
    @DisplayName("ciclo completo: cria, busca, lista, renomeia e exclui")
    void shouldRunFullCrud() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Cliente")))
                .andExpect(status().isCreated());
        Long id = repository.findByNameIgnoreCase("Cliente").orElseThrow().getId();

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cliente"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Administrador")))
                .andExpect(status().isCreated());
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Administrador"))
                .andExpect(jsonPath("$[1].name").value("Cliente"));

        mockMvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body("Consumidor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consumidor"));
        assertThat(repository.findById(id).orElseThrow().getName()).isEqualTo("Consumidor");

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("nome repetido com outra capitalização responde 409")
    void shouldRejectDuplicateIgnoringCase() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("Cliente")))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("CLIENTE")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("o índice do banco barra duplicatas mesmo se o caso de uso for contornado")
    void shouldEnforceUniquenessInDatabase() {
        repository.saveAndFlush(new UserTypeJpaEntity(null, "Cliente"));

        assertThatThrownBy(() -> repository.saveAndFlush(new UserTypeJpaEntity(null, "cliente")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
