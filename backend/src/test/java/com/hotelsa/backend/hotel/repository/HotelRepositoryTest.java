package com.hotelsa.backend.hotel.repository;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    private Hotel crearHotelDePrueba(String nombre) {
        Hotel hotel = Hotel.builder()
                .name(nombre)
                .address("Calle 123")
                .city("CiudadX")
                .country("PaisX")
                .phone("999999999")
                .description("Hotel de prueba")
                .build();
        return hotelRepository.save(hotel);
    }

    private User crearUsuarioParaHotel(Hotel hotel, String username) {
        User user = User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        // ✅ Usar helper method para mantener la relación bidireccional
        hotel.addUser(user);

        return userRepository.save(user);
    }

    @Test
    void debeGuardarYRecuperarHotel() {
        Hotel hotel = crearHotelDePrueba("Hotel Test");
        Hotel encontrado = hotelRepository.findById(hotel.getId()).orElseThrow();

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getName()).isEqualTo("Hotel Test");
        assertThat(encontrado.getCity()).isEqualTo("CiudadX");
    }

    @Test
    void debeRetornarTodosLosHoteles() {
        crearHotelDePrueba("Hotel 1");
        crearHotelDePrueba("Hotel 2");

        List<Hotel> hoteles = hotelRepository.findAll();
        assertThat(hoteles).hasSize(2);
    }

    @Test
    void debeMantenerRelacionConUsuarios() {
        Hotel hotel = crearHotelDePrueba("Hotel Relación");
        crearUsuarioParaHotel(hotel, "empleado1");
        crearUsuarioParaHotel(hotel, "empleado2");

        Hotel encontrado = hotelRepository.findById(hotel.getId()).orElseThrow();

        assertThat(encontrado.getUsers()).hasSize(2);
        assertThat(encontrado.getUsers())
                .extracting("username")
                .containsExactlyInAnyOrder("empleado1", "empleado2");
    }

    @Test
    void debeRetornarVacioCuandoNoExisteHotel() {
        var hotel = hotelRepository.findById(999L);
        assertThat(hotel).isEmpty();
    }

    @Test
    void debeGuardarHotelSinUsuarios() {
        Hotel hotel = crearHotelDePrueba("Hotel Sin Usuarios");
        Hotel encontrado = hotelRepository.findById(hotel.getId()).orElseThrow();

        assertThat(encontrado.getUsers()).isEmpty();
    }
}
