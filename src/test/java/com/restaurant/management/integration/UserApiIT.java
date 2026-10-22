package com.restaurant.management.integration;

import com.restaurant.management.infrastructure.persistence.user.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integração de ponta a ponta do cadastro de usuários (clientes e donos) e do login,
 * com PostgreSQL real: a herança JPA, o hash BCrypt e as regras dos casos de uso juntos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Integração — clientes, donos e login (PostgreSQL real)")
class UserApiIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private static final String CUSTOMER = """
            {"name": "Maria Oliveira", "email": "Maria@Email.com", "login": "maria.oliveira",
             "password": "Senha@456", "cpf": "123.456.789-00", "dateOfBirth": "1995-07-15",
             "address": {"street": "Rua das Flores", "number": "123", "city": "São Paulo",
                         "state": "SP", "zipCode": "01001-000"}}
            """;

    private static final String OWNER = """
            {"name": "João Silva", "email": "joao@restaurante.com", "login": "joao.silva",
             "password": "Senha@123", "restaurantName": "Grupo Silva", "cnpj": "12.345.678/0001-90"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    private Long idOf(String login) {
        return userRepository.findByLoginIgnoreCase(login).orElseThrow().getId();
    }

    @Test
    @DisplayName("cliente: cadastra, busca, atualiza mantendo o endereço, troca a senha, faz login e exclui")
    void shouldRunCustomerLifecycle() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(CUSTOMER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.userType.name").value("Cliente"))
                .andExpect(jsonPath("$.lastModifiedAt").exists());
        Long id = idOf("maria.oliveira");
        assertThat(userRepository.findById(id).orElseThrow().getPassword()).startsWith("$2a$12$");

        mockMvc.perform(put("/api/v1/customers/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Maria A. Oliveira", "email": "maria@email.com", "login": "maria.oliveira"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria A. Oliveira"))
                .andExpect(jsonPath("$.address.street").value("Rua das Flores"))
                .andExpect(jsonPath("$.cpf").doesNotExist());

        mockMvc.perform(patch("/api/v1/customers/" + id + "/password").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "Senha@456", "newPassword": "Nova@7890", "confirmNewPassword": "Nova@7890"}
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"MARIA.OLIVEIRA\", \"password\": \"Nova@7890\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile").value("CUSTOMER"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"maria.oliveira\", \"password\": \"Senha@456\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/customers/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/users/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("dono nasce com o tipo \"Dono de Restaurante\"; rotas de um perfil não enxergam o outro")
    void shouldSeparateProfiles() throws Exception {
        mockMvc.perform(post("/api/v1/restaurant-owners").contentType(MediaType.APPLICATION_JSON).content(OWNER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType.name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"));
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(CUSTOMER))
                .andExpect(status().isCreated());
        Long ownerId = idOf("joao.silva");

        mockMvc.perform(get("/api/v1/customers/" + ownerId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/restaurant-owners")).andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/users").param("name", "silva"))
                .andExpect(jsonPath("$.content[0].profile").value("RESTAURANT_OWNER"));
        mockMvc.perform(get("/api/v1/users").param("profile", "CUSTOMER"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].login").value("maria.oliveira"));
    }

    @Test
    @DisplayName("e-mail repetido (sem diferenciar maiúsculas), CPF repetido e senha atual errada são recusados")
    void shouldRejectConflicts() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content(CUSTOMER))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(CUSTOMER.replace("maria.oliveira", "outra").replace("123.456.789-00", "987.654.321-00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/email-conflict"));
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(CUSTOMER.replace("Maria@Email.com", "outra@email.com").replace("maria.oliveira", "outra")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://restaurant-api.com/errors/cpf-conflict"));

        mockMvc.perform(patch("/api/v1/customers/" + idOf("maria.oliveira") + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "Errada@000", "newPassword": "Nova@7890", "confirmNewPassword": "Nova@7890"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }
}
