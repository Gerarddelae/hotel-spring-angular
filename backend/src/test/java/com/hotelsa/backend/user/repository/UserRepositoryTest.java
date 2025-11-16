package com.hotelsa.backend.user.repository;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.tenant.TenantContext;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // ✅ SOLUCIÓN: Necesitas inyectar HotelRepository para crear el hotel
    @Autowired
    private HotelRepository hotelRepository;

    private Long tenantId;

    @BeforeEach
    void setUp() {
        // ✅ PASO 1: Crea y guarda el hotel PRIMERO
        // Sin esto, la foreign key rechaza cualquier usuario con hotel_id
        Hotel hotel = Hotel.builder()
                .name("Hotel Test")
                .address("Calle Test 123")
                .city("Ciudad Test")
                .country("País Test")
                .phone("123456789")
                .description("Hotel de prueba")
                .build();

        hotel = hotelRepository.save(hotel);

        // ✅ PASO 2: Usa el ID real del hotel guardado
        // No uses un ID hardcodeado (100L) que no existe
        tenantId = hotel.getId();

        // ✅ PASO 3: Configura el contexto de tenant
        TenantContext.setCurrentTenant(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void existsByUsername_debeRetornarTrueCuandoUsuarioExiste() {
        // ✅ Ahora tenantId es un ID válido de un hotel que existe
        User user = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .role(Role.USER)
                .hotelId(tenantId) // ✅ Este ID existe en la BD
                .build();

        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("johndoe");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_debeRetornarFalseCuandoUsuarioNoExiste() {
        boolean exists = userRepository.existsByUsername("noexiste");
        assertThat(exists).isFalse();
    }

    @Test
    void findByUsername_debeRetornarUsuarioCuandoExiste() {
        User user = User.builder()
                .username("janedoe")
                .email("jane@example.com")
                .password("password123")
                .role(Role.USER)
                .hotelId(tenantId) // ✅ ID válido
                .build();

        userRepository.save(user);

        var encontrado = userRepository.findByUsername("janedoe");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("jane@example.com");
        assertThat(encontrado.get().getHotelId()).isEqualTo(tenantId);
    }

    @Test
    void findByUsername_debeRetornarVacioCuandoNoExiste() {
        var encontrado = userRepository.findByUsername("noexiste");
        assertThat(encontrado).isEmpty();
    }

    @Test
    void debeGuardarUsuarioConTenantId() {
        User user = User.builder()
                .username("empleado1")
                .email("empleado@example.com")
                .password("password")
                .role(Role.USER)
                .hotelId(tenantId) // ✅ ID válido
                .build();

        User guardado = userRepository.save(user);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getHotelId()).isEqualTo(tenantId);
    }

    @Test
    void findAll_debeRetornarUsuariosDelTenantActual() {
        User user1 = User.builder()
                .username("user1")
                .email("user1@mail.com")
                .password("password")
                .role(Role.USER)
                .hotelId(tenantId) // ✅ ID válido
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@mail.com")
                .password("password")
                .role(Role.USER)
                .hotelId(tenantId) // ✅ ID válido
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        var usuarios = userRepository.findAll();

        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void findAll_debeRetornarListaVaciaCuandoNoHayUsuarios() {
        var usuarios = userRepository.findAll();
        assertThat(usuarios).isEmpty();
    }

    @Test
    void findById_debeRetornarUsuarioCuandoExiste() {
        User user = User.builder()
                .username("usuarioTest")
                .email("usuario@test.com")
                .password("password123")
                .role(Role.USER)
                .hotelId(tenantId) // ✅ ID válido
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
                .hotelId(tenantId) // ✅ ID válido
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