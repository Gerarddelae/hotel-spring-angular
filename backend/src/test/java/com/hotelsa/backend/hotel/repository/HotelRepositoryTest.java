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

    // ✅ Crea un hotel sin usuarios
    private Hotel crearHotelDePrueba(String nombre) {
        Hotel hotel = Hotel.builder()
                .name(nombre)
                .address("Calle 123")
                .city("CiudadX")
                .country("PaisX")
                .phone("999999999")
                .description("Hotel de prueba")
                .build();

        // ✅ Guarda el hotel primero para que tenga un ID asignado por la BD
        return hotelRepository.save(hotel);
    }

    // ✅ Crea un usuario y lo asocia correctamente a un hotel
    private User crearUsuarioParaHotel(Hotel hotel, String username) {
        // PASO 1: Crea el usuario usando hotelId (NO el objeto hotel)
        // 🔑 SOLUCIÓN CRÍTICA: Tu User tiene dos formas de relacionarse con Hotel:
        // 1. @ManyToOne hotel (con insertable=false, updatable=false) ❌ No actualiza BD
        // 2. hotelId en BaseEntity (campo real que se mapea a hotel_id) ✅ Este SÍ funciona
        User user = User.builder()
                .username(username)
                .email(username + "@example.com")
                .password("password123")
                .role(Role.USER)
                .hotelId(hotel.getId())  // ✅ SOLUCIÓN: Usa hotelId, NO hotel
                .build();

        // PASO 2: ✅ Agrega el usuario a la colección del hotel
        // Esto mantiene la consistencia bidireccional en memoria
        hotel.getUsers().add(user);

        // ⚠️ POR QUÉ NO FUNCIONA .hotel(hotel):
        // En tu clase User, el @ManyToOne tiene:
        // @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
        //                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        // Esto significa que aunque establezcas user.hotel = hotel,
        // Hibernate NO escribirá nada en la columna hotel_id.
        //
        // BaseEntity tiene el campo real:
        // @Column(name = "hotel_id", nullable = false)
        // private Long hotelId;  <- Este SÍ se persiste

        // PASO 3: ✅ Guarda SOLO el usuario
        // Ahora user.hotelId tiene el valor correcto
        return userRepository.save(user);

        // 💡 FLUJO CORRECTO:
        // 1. Hotel ya guardado (tiene ID) ✅
        // 2. User creado con .hotelId(hotel.getId()) ✅
        // 3. Se agrega a hotel.users para consistencia ✅
        // 4. Se guarda user ✅
        // 5. INSERT con hotel_id válido ✅
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
        // PASO 1: Crea y guarda el hotel (ahora tiene ID)
        Hotel hotel = crearHotelDePrueba("Hotel Relación");

        // PASO 2: Crea usuarios asociados al hotel
        // Cada llamada hace:
        // - Crea user con builder
        // - Asocia bidireccionalmente con hotel.addUser()
        // - Guarda SOLO el user (no el hotel de nuevo)
        crearUsuarioParaHotel(hotel, "empleado1");
        crearUsuarioParaHotel(hotel, "empleado2");

        // PASO 3: Recupera el hotel de la BD
        // ⚠️ IMPORTANTE: Como no guardamos el hotel después de addUser(),
        // la lista hotel.users en memoria puede estar desactualizada.
        // Por eso volvemos a consultar desde la BD.
        Hotel encontrado = hotelRepository.findById(hotel.getId()).orElseThrow();

        // PASO 4: Verifica que la relación se guardó correctamente
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