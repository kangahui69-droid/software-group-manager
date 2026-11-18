package com.softwaregroup.project.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/** Exercises the checked-in business schema against a disposable MySQL container in CI. */
@Testcontainers
@EnabledIfEnvironmentVariable(named = "CI_TESTCONTAINERS", matches = "true")
class CiMySqlContainerTest {
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.42")
            .withDatabaseName("ci_container_test")
            .withUsername("ci_user")
            .withPassword("ci_test_password");

    @Test
    void baselineSchemaSupportsBusinessWrites() throws Exception {
        Path baseline = Path.of("..", "db-migrations", "sql", "V1__baseline_schema.sql")
                .toAbsolutePath().normalize();
        assertThat(Files.isRegularFile(baseline)).as("business schema baseline must exist").isTrue();

        mysql.copyFileToContainer(MountableFile.forHostPath(baseline), "/tmp/business-schema.sql");
        org.testcontainers.containers.Container.ExecResult imported = mysql.execInContainer("sh", "-c",
                "MYSQL_PWD=\"$MYSQL_PASSWORD\" mysql -u\"$MYSQL_USER\" \"$MYSQL_DATABASE\" < /tmp/business-schema.sql");
        assertThat(imported.getExitCode()).as(imported.getStderr()).isZero();

        try (Connection connection = DriverManager.getConnection(
                mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement statement = connection.createStatement()) {
            try (ResultSet tables = statement.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                assertThat(tables.next()).isTrue();
                assertThat(tables.getInt(1)).isEqualTo(45);
            }
            assertThat(statement.executeUpdate("INSERT INTO user (username, password, name, email, role, status) "
                    + "VALUES ('ci_container_user', 'test-hash', 'CI User', 'ci@example.invalid', 'MEMBER', 1)"))
                    .isEqualTo(1);
            try (ResultSet users = statement.executeQuery(
                    "SELECT COUNT(*) FROM user WHERE username = 'ci_container_user'")) {
                assertThat(users.next()).isTrue();
                assertThat(users.getInt(1)).isEqualTo(1);
            }
        }
    }
}
