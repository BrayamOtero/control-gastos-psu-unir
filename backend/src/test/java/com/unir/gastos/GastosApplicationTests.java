package com.unir.gastos;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:file:./build/test-data/gastos")
class GastosApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoadsWithFileDatabase() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:file:");
        }
    }

    @Test
    void fileDatabaseRetainsDataAfterReopening(@TempDir Path temporaryDirectory) throws SQLException {
        var databaseUrl = "jdbc:h2:file:" + temporaryDirectory.resolve("gastos");

        try (var connection = DriverManager.getConnection(databaseUrl, "sa", "");
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE persistence_probe (id INTEGER PRIMARY KEY)");
            statement.executeUpdate("INSERT INTO persistence_probe (id) VALUES (1)");
        }

        try (var connection = DriverManager.getConnection(databaseUrl, "sa", "");
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT id FROM persistence_probe")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getInt("id")).isEqualTo(1);
            assertThat(result.next()).isFalse();
        }
    }

}