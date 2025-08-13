package com.hotelsa.backend.user.repository;

import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByUsername_debeRetornarTrueCuandoUsuarioExiste() {
        // Arrange
        User user = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername("johndoe");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void findByUsername_debeRetornarUsuarioCuandoExiste() {
        // Arrange
        User user = User.builder()
                .username("janedoe")
                .email("jane@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Act
        var encontrado = userRepository.findByUsername("janedoe");

        // Assert
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findByUsername_debeRetornarVacioCuandoNoExiste() {
        // Act
        var encontrado = userRepository.findByUsername("noexiste");

        // Assert
        assertThat(encontrado).isEmpty();
    }
}
