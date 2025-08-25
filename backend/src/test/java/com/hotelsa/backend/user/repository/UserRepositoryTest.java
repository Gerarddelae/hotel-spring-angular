package com.hotelsa.backend.user.repository;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
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

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel crearHotelDePrueba() {
        Hotel hotel = Hotel.builder()
                .name("Hotel Test")
                .address("Calle 123")
                .city("CiudadX")
                .country("PaisX")
                .phone("999999999")
                .description("Hotel de prueba")
                .build();
        return hotelRepository.save(hotel);
    }

    @Test
    void existsByUsername_debeRetornarTrueCuandoUsuarioExiste() {
        // Arrange
        Hotel hotel = crearHotelDePrueba();

        User user = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .role(Role.USER)
                .hotel(hotel)
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
        Hotel hotel = crearHotelDePrueba();

        User user = User.builder()
                .username("janedoe")
                .email("jane@example.com")
                .password("password123")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        userRepository.save(user);

        // Act
        var encontrado = userRepository.findByUsername("janedoe");

        // Assert
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("jane@example.com");
        assertThat(encontrado.get().getHotel().getName()).isEqualTo("Hotel Test");
    }

    @Test
    void findByUsername_debeRetornarVacioCuandoNoExiste() {
        // Act
        var encontrado = userRepository.findByUsername("noexiste");

        // Assert
        assertThat(encontrado).isEmpty();
    }

    @Test
    void existsByUsername_debeRetornarFalseCuandoUsuarioNoExiste() {
        // Act
        boolean exists = userRepository.existsByUsername("noexiste");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void debeGuardarUsuarioConHotel() {
        // Arrange
        Hotel hotel = crearHotelDePrueba();

        User user = User.builder()
                .username("empleado1")
                .email("empleado@example.com")
                .password("password")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        // Act
        User guardado = userRepository.save(user);

        // Assert
        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getHotel().getId()).isEqualTo(hotel.getId());
    }

    @Test
    void findByHotelId_debeRetornarUsuariosDelHotel() {
        // Arrange
        Hotel hotel = crearHotelDePrueba();

        User user1 = User.builder()
                .username("user1")
                .email("user1@mail.com")
                .password("password")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@mail.com")
                .password("password")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // Act
        var usuarios = userRepository.findByHotelId(hotel.getId());

        // Assert
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void findByHotelId_debeRetornarListaVaciaCuandoNoHayUsuarios() {
        // Arrange
        Hotel hotel = crearHotelDePrueba();

        // Act
        var usuarios = userRepository.findByHotelId(hotel.getId());

        // Assert
        assertThat(usuarios).isEmpty();
    }

    @Test
    void findById_debeRetornarUsuarioCuandoExiste() {
        // Arrange
        Hotel hotel = crearHotelDePrueba();

        User user = User.builder()
                .username("usuarioTest")
                .email("usuario@test.com")
                .password("password123")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        User guardado = userRepository.save(user);

        // Act
        var encontrado = userRepository.findById(guardado.getId());

        // Assert
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsername()).isEqualTo("usuarioTest");
    }

    @Test
    void findById_debeRetornarVacioCuandoNoExiste() {
        // Act
        var encontrado = userRepository.findById(999L);

        // Assert
        assertThat(encontrado).isEmpty();
    }
}
