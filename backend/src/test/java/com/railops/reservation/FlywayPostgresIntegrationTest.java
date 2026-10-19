package com.railops.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import java.sql.DriverManager;
import java.util.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "RAILOPS_TEST_DB_URL", matches = ".+")
class FlywayPostgresIntegrationTest {
    private final List<String> schemas = new ArrayList<>();
    private String url() { return System.getenv("RAILOPS_TEST_DB_URL"); }
    private String user() { return System.getenv().getOrDefault("RAILOPS_TEST_DB_USERNAME", "railops"); }
    private String password() { return System.getenv().getOrDefault("RAILOPS_TEST_DB_PASSWORD", "railops"); }
    private String schema() { String name = "railops_migration_" + UUID.randomUUID().toString().replace("-", ""); schemas.add(name); return name; }
    private org.flywaydb.core.api.configuration.FluentConfiguration configure(String schema) {
        return Flyway.configure().dataSource(url(), user(), password()).schemas(schema).defaultSchema(schema).locations("classpath:db/migration");
    }
    @AfterEach void cleanup() throws Exception {
        try (var c = DriverManager.getConnection(url(), user(), password()); var s = c.createStatement()) {
            for (String schema : schemas) {
                if (!schema.matches("railops_migration_[a-f0-9]{32}")) throw new IllegalStateException("Unexpected schema");
                s.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
            }
        }
    }
    @Test void newDatabaseMigratesAndSecondStartupIsNoOp() {
        String schema = schema();
        assertThat(configure(schema).load().migrate().migrationsExecuted).isEqualTo(2);
        assertThat(configure(schema).load().migrate().migrationsExecuted).isZero();
        assertThat(configure(schema).load().info().current().getVersion().toString()).isEqualTo("2");
    }
    @Test void legacyBaselinePreservesExistingUserAndAddsAccountAndAuditSchema() throws Exception {
        String schema = schema();
        configure(schema).target("1").load().migrate();
        try (var c = DriverManager.getConnection(url(), user(), password()); var s = c.createStatement()) {
            s.execute("DROP TABLE " + schema + ".flyway_schema_history");
            s.execute("INSERT INTO " + schema + ".users(email,password,name,role,status,created_at,updated_at) VALUES ('legacy@test.local','hash','Legacy','USER','ACTIVE',now(),now())");
        }
        configure(schema).baselineOnMigrate(true).baselineVersion("1").load().migrate();
        try (var c = DriverManager.getConnection(url(), user(), password()); var s = c.createStatement(); var r = s.executeQuery("SELECT name, token_version FROM " + schema + ".users WHERE email='legacy@test.local'")) {
            assertThat(r.next()).isTrue(); assertThat(r.getString(1)).isEqualTo("Legacy"); assertThat(r.getLong(2)).isZero();
        }
    }
}
