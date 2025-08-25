package com.hotelsa.backend.user.repository;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .name("Hotel Test")
                .address("Calle 123")
                .city("CiudadX")
                .country("PaisX")
                .phone("999999999")
                .description("Hotel de prueba")
                .build();

        hotel = hotelRepository.save(hotel);
    }

    @Test
    void existsByUsername_debeRetornarTrueCuandoUsuarioExiste() {
        User user = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("johndoe");

        assertThat(exists).isTrue();
    }

    @Test
    void findByUsername_debeRetornarUsuarioCuandoExiste() {
        User user = User.builder()
                .username("janedoe")
                .email("jane@example.com")
                .password("password123")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        userRepository.save(user);

        var encontrado = userRepository.findByUsername("janedoe");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("jane@example.com");
        assertThat(encontrado.get().getHotel().getName()).isEqualTo("Hotel Test");
    }

    @Test
    void findByUsername_debeRetornarVacioCuandoNoExiste() {
        var encontrado = userRepository.findByUsername("noexiste");

        assertThat(encontrado).isEmpty();
    }

    @Test
    void existsByUsername_debeRetornarFalseCuandoUsuarioNoExiste() {
        boolean exists = userRepository.existsByUsername("noexiste");

        assertThat(exists).isFalse();
    }

    @Test
    void debeGuardarUsuarioConHotel() {
        User user = User.builder()
                .username("empleado1")
                .email("empleado@example.com")
                .password("password")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        User guardado = userRepository.save(user);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getHotel().getId()).isEqualTo(hotel.getId());
    }

    @Test
    void findByHotelId_debeRetornarUsuariosDelHotel() {
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

        var usuarios = userRepository.findByHotelId(hotel.getId());

        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void findByHotelId_debeRetornarListaVaciaCuandoNoHayUsuarios() {
        var usuarios = userRepository.findByHotelId(hotel.getId());

        assertThat(usuarios).isEmpty();
    }

    @Test
    void findById_debeRetornarUsuarioCuandoExiste() {
        User user = User.builder()
                .username("usuarioTest")
                .email("usuario@test.com")
                .password("password123")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        User guardado = userRepository.save(user);

        var encontrado = userRepository.findById(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsername()).isEqualTo("usuarioTest");
    }

    @Test
    void findById_debeRetornarVacioCuandoNoExiste() {
        var encontrado = userRepository.findById(999L);

        assertThat(encontrado).isEmpty();
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = User.builder()
                .username("old_username")
                .email("old@mail.com")
                .password("old_password")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        user = userRepository.save(user);

        user.setUsername("new_username");
        user.setEmail("new@mail.com");
        user.setPassword("new_password");

        User updatedUser = userRepository.save(user);

        assertNotNull(updatedUser.getId());
        assertEquals("new_username", updatedUser.getUsername());
        assertEquals("new@mail.com", updatedUser.getEmail());
        assertEquals("new_password", updatedUser.getPassword());
    }
}
