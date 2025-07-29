package com.hotelsa.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)  // Importar el bean del contenedor
public class PostgresConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testThatConnectionWorks() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            assertThat(valid).isTrue(); // Verifica conexión válida
        }
    }
}
