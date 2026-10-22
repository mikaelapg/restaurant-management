package com.restaurant.management.integration;

import com.restaurant.management.core.domain.user.UserProfile;
import com.restaurant.management.infrastructure.persistence.user.CustomerJpaEntity;
import com.restaurant.management.infrastructure.persistence.user.CustomerJpaRepository;
import com.restaurant.management.infrastructure.persistence.user.RestaurantOwnerJpaEntity;
import com.restaurant.management.infrastructure.persistence.user.RestaurantOwnerJpaRepository;
import com.restaurant.management.infrastructure.persistence.user.UserJpaEntity;
import com.restaurant.management.infrastructure.persistence.user.UserJpaRepository;
import com.restaurant.management.infrastructure.persistence.usertype.UserTypeJpaRepository;
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
 * Teste de integração contra um PostgreSQL real (Testcontainers): as migrations do
 * Flyway aplicam o schema, o Hibernate o valida, e as restrições de unicidade e de
 * integridade são impostas pelo próprio banco, mesmo sem passar pelos casos de uso.
 * <p>
 * Sufixo {@code IT}: roda em {@code mvn verify} (maven-failsafe-plugin), não em {@code mvn test}.
 * Requer Docker; sem ele, a classe inteira é ignorada.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = NONE)
@DisplayName("Integração — migrations Flyway e constraints do banco (PostgreSQL real)")
class SchemaConstraintsIT {

    private static final String HASH = "$2a$12$hashhashhashhashhashhh";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CustomerJpaRepository customerRepository;

    @Autowired
    private RestaurantOwnerJpaRepository ownerRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private UserTypeJpaRepository userTypeRepository;

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

    private static <T extends UserJpaEntity> T common(T entity, String email, String login) {
        entity.setName("Usuário Teste");
        entity.setEmail(email);
        entity.setLogin(login);
        entity.setPassword(HASH);
        return entity;
    }

    private static CustomerJpaEntity customer(String email, String login, String cpf) {
        CustomerJpaEntity entity = common(new CustomerJpaEntity(), email, login);
        entity.setCpf(cpf);
        return entity;
    }

    private static RestaurantOwnerJpaEntity owner(String email, String login, String cnpj) {
        RestaurantOwnerJpaEntity entity = common(new RestaurantOwnerJpaEntity(), email, login);
        entity.setCnpj(cnpj);
        return entity;
    }

    @Test
    @DisplayName("todas as migrations foram aplicadas e criaram as tabelas do domínio")
    void flywayMigrationsAreApplied() {
        MigrationInfo current = flyway.info().current();
        assertThat(current).isNotNull();
        assertThat(current.getVersion().getVersion()).isEqualTo("6");
        assertThat(flyway.info().pending()).isEmpty();

        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("customers")).isTrue();
        assertThat(tableExists("restaurant_owners")).isTrue();
        assertThat(tableExists("user_types")).isTrue();
        assertThat(tableExists("restaurants")).isTrue();
        assertThat(tableExists("menu_items")).isTrue();
    }

    @Test
    @DisplayName("a V6 deixa um tipo de sistema por perfil, e o banco não aceita um segundo")
    void systemTypesAreLinkedToProfiles() {
        assertThat(userTypeRepository.findByProfile(UserProfile.RESTAURANT_OWNER))
                .get().extracting(type -> type.getName()).isEqualTo("Dono de Restaurante");
        assertThat(userTypeRepository.findByProfile(UserProfile.CUSTOMER))
                .get().extracting(type -> type.getName()).isEqualTo("Cliente");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO user_types (name, profile) VALUES ('Outro Cliente', 'CUSTOMER')"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO user_types (name, profile) VALUES ('Admin', 'ADMIN')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("e-mail e login duplicados são recusados pelas constraints de users")
    void emailAndLoginUniquenessIsEnforced() {
        customerRepository.saveAndFlush(customer("dup@email.com", "login.a", null));

        assertThatThrownBy(() -> customerRepository.saveAndFlush(customer("dup@email.com", "login.b", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> ownerRepository.saveAndFlush(owner("outro@email.com", "login.a", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("CPF e CNPJ duplicados são recusados; nulos podem se repetir")
    void documentUniquenessIsEnforced() {
        customerRepository.saveAndFlush(customer("c1@email.com", "login.c1", "555.555.555-55"));
        assertThatThrownBy(() -> customerRepository.saveAndFlush(customer("c2@email.com", "login.c2", "555.555.555-55")))
                .isInstanceOf(DataIntegrityViolationException.class);

        ownerRepository.saveAndFlush(owner("o1@email.com", "login.o1", "12.345.678/0001-90"));
        assertThatThrownBy(() -> ownerRepository.saveAndFlush(owner("o2@email.com", "login.o2", "12.345.678/0001-90")))
                .isInstanceOf(DataIntegrityViolationException.class);

        customerRepository.saveAndFlush(customer("n1@email.com", "login.n1", null));
        customerRepository.saveAndFlush(customer("n2@email.com", "login.n2", null));
        assertThat(customerRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("a herança JOINED grava o discriminador e a data da última alteração")
    void inheritanceAndTimestampAreWritten() {
        Long id = ownerRepository.saveAndFlush(owner("dono@email.com", "dono", null)).getId();

        String discriminator = jdbcTemplate.queryForObject(
                "SELECT user_type FROM users WHERE id = ?", String.class, id);
        assertThat(discriminator).isEqualTo("RESTAURANT_OWNER");
        assertThat(userRepository.findById(id)).get()
                .isInstanceOf(RestaurantOwnerJpaEntity.class)
                .extracting(UserJpaEntity::getLastModifiedAt).isNotNull();
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                Integer.class, table);
        return count != null && count == 1;
    }
}
