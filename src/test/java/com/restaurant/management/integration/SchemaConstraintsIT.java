package com.restaurant.management.integration;

import com.restaurant.management.domain.model.Customer;
import com.restaurant.management.domain.model.RestaurantOwner;
import com.restaurant.management.domain.repository.CustomerRepository;
import com.restaurant.management.domain.repository.RestaurantOwnerRepository;
import com.restaurant.management.domain.repository.UserRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Teste de integração contra um PostgreSQL real (Testcontainers).
 * <p>
 * Cobre o que antes só era demonstrado manualmente com {@code docker compose} + {@code psql}:
 * que a migração do Flyway aplica o schema e que as restrições de unicidade
 * (e-mail, login, CPF, CNPJ) são efetivamente impostas pelo banco.
 * <p>
 * Sufixo {@code IT}: roda em {@code mvn verify} (maven-failsafe-plugin), não em {@code mvn test}.
 * Requer Docker; sem ele, a classe inteira é ignorada.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Integração — migração Flyway e constraints de unicidade (PostgreSQL real)")
class SchemaConstraintsIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantOwnerRepository restaurantOwnerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        // deleteAll() (não -InBatch) para que o Hibernate remova a linha filha e a linha
        // pai da herança JOINED; evita órfãs em users entre um teste e outro.
        userRepository.deleteAll();
    }

    private static Customer customer(String email, String login, String cpf) {
        return Customer.builder()
                .name("Cliente Teste")
                .email(email)
                .login(login)
                .password("$2a$12$hashhashhashhashhashhh")
                .cpf(cpf)
                .build();
    }

    private static RestaurantOwner owner(String email, String login, String cnpj) {
        return RestaurantOwner.builder()
                .name("Dono Teste")
                .email(email)
                .login(login)
                .password("$2a$12$hashhashhashhashhashhh")
                .restaurantName("Restaurante Teste")
                .cnpj(cnpj)
                .build();
    }

    @Test
    @DisplayName("Todas as migrações do Flyway foram aplicadas e criaram as tabelas do domínio")
    void flywayMigrationIsApplied() {
        MigrationInfo current = flyway.info().current();
        assertThat(current).isNotNull();
        assertThat(current.getState().isApplied()).isTrue();
        assertThat(flyway.info().pending()).isEmpty();

        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("customers")).isTrue();
        assertThat(tableExists("restaurant_owners")).isTrue();

        Integer nameIndex = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pg_indexes WHERE tablename = 'users' AND indexname = 'idx_users_name'",
                Integer.class);
        assertThat(nameIndex).isEqualTo(1);
    }

    @Test
    @DisplayName("E-mail duplicado é recusado pela constraint uk_users_email")
    void emailUniquenessIsEnforced() {
        customerRepository.saveAndFlush(customer("dup@email.com", "login.a", "111.111.111-11"));

        assertThatThrownBy(() ->
                customerRepository.saveAndFlush(customer("dup@email.com", "login.b", "222.222.222-22")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Login duplicado é recusado pela constraint uk_users_login")
    void loginUniquenessIsEnforced() {
        customerRepository.saveAndFlush(customer("a@email.com", "mesmo.login", "333.333.333-33"));

        assertThatThrownBy(() ->
                customerRepository.saveAndFlush(customer("b@email.com", "mesmo.login", "444.444.444-44")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("CPF duplicado é recusado pela constraint uk_customers_cpf")
    void cpfUniquenessIsEnforced() {
        customerRepository.saveAndFlush(customer("c1@email.com", "login.c1", "555.555.555-55"));

        assertThatThrownBy(() ->
                customerRepository.saveAndFlush(customer("c2@email.com", "login.c2", "555.555.555-55")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("CNPJ duplicado é recusado pela constraint uk_restaurant_owners_cnpj")
    void cnpjUniquenessIsEnforced() {
        restaurantOwnerRepository.saveAndFlush(owner("o1@email.com", "login.o1", "12.345.678/0001-90"));

        assertThatThrownBy(() ->
                restaurantOwnerRepository.saveAndFlush(owner("o2@email.com", "login.o2", "12.345.678/0001-90")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("CPF nulo é opcional: vários clientes sem CPF coexistem")
    void nullCpfIsOptionalAndNonUnique() {
        customerRepository.saveAndFlush(customer("n1@email.com", "login.n1", null));
        customerRepository.saveAndFlush(customer("n2@email.com", "login.n2", null));

        assertThat(customerRepository.count()).isEqualTo(2);
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                Integer.class, table);
        return count != null && count == 1;
    }
}
