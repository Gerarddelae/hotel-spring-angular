package com.hotelsa.backend.room.repository;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = hotelRepository.save(
                Hotel.builder()
                        .name("Hotel Test")
                        .address("Calle 123")
                        .city("CiudadX")
                        .country("PaisX")
                        .phone("999999999")
                        .description("Hotel de prueba")
                        .build()
        );

        // Simular tenant actual
        TenantContext.setCurrentTenant(hotel.getId());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void existsByNumber_debeRetornarTrueCuandoHabitacionExiste() {
        Room room = Room.builder()
                .number("101")
                .type(RoomType.SINGLE)
                .status(RoomStatus.AVAILABLE)
                .floor(1)
                .capacity(2)
                .pricePerNight(100.0)
                .hotelId(hotel.getId())
                .build();

        roomRepository.save(room);

        boolean exists = roomRepository.existsByNumber("101");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNumber_debeRetornarFalseCuandoHabitacionNoExiste() {
        boolean exists = roomRepository.existsByNumber("999");
        assertThat(exists).isFalse();
    }

    @Test
    void findAll_debeRetornarSoloHabitacionesDelTenantActual() {
        Room room1 = Room.builder()
                .number("201")
                .type(RoomType.DOUBLE)
                .status(RoomStatus.AVAILABLE)
                .floor(2)
                .capacity(2)
                .pricePerNight(150.0)
                .hotelId(hotel.getId())
                .build();

        Room room2 = Room.builder()
                .number("202")
                .type(RoomType.SUITE)
                .status(RoomStatus.AVAILABLE)
                .floor(2)
                .capacity(4)
                .pricePerNight(300.0)
                .hotelId(hotel.getId())
                .build();

        roomRepository.save(room1);
        roomRepository.save(room2);

        List<Room> rooms = roomRepository.findAll();

        assertThat(rooms).hasSize(2);
        assertThat(rooms).extracting(Room::getNumber)
                .containsExactlyInAnyOrder("201", "202");
    }

    @Test
    void findById_debeRetornarHabitacionCuandoExiste() {
        Room room = Room.builder()
                .number("301")
                .type(RoomType.SINGLE)
                .status(RoomStatus.AVAILABLE)
                .floor(3)
                .capacity(1)
                .pricePerNight(120.0)
                .hotelId(hotel.getId())
                .build();

        Room guardada = roomRepository.save(room);

        var encontrada = roomRepository.findById(guardada.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNumber()).isEqualTo("301");
    }

    @Test
    void findById_debeRetornarVacioCuandoNoExiste() {
        var encontrada = roomRepository.findById(999L);
        assertThat(encontrada).isEmpty();
    }

    @Test
    void debeGuardarHabitacionConTenant() {
        Room room = Room.builder()
                .number("401")
                .type(RoomType.SINGLE)
                .status(RoomStatus.AVAILABLE)
                .floor(4)
                .capacity(2)
                .pricePerNight(180.0)
                .hotelId(hotel.getId())
                .build();

        Room guardada = roomRepository.save(room);

        assertNotNull(guardada.getId());
        assertEquals(hotel.getId(), guardada.getHotelId());
    }

    @Test
    void shouldUpdateRoomSuccessfully() {
        Room room = Room.builder()
                .number("501")
                .type(RoomType.SINGLE)
                .status(RoomStatus.AVAILABLE)
                .floor(5)
                .capacity(2)
                .pricePerNight(200.0)
                .hotelId(hotel.getId())
                .build();

        room = roomRepository.save(room);

        room.setNumber("502");
        room.setType(RoomType.DOUBLE);
        room.setPricePerNight(250.0);

        Room updatedRoom = roomRepository.save(room);

        assertNotNull(updatedRoom.getId());
        assertEquals("502", updatedRoom.getNumber());
        assertEquals(RoomType.DOUBLE, updatedRoom.getType());
        assertEquals(250.0, updatedRoom.getPricePerNight(), 0.001);
    }

    @Test
    void findAll_debeFiltrarPorTipo() {
        Room room1 = Room.builder()
                .number("601")
                .type(RoomType.SINGLE)
                .status(RoomStatus.AVAILABLE)
                .floor(6)
                .capacity(1)
                .pricePerNight(100.0)
                .hotelId(hotel.getId())
                .build();

        Room room2 = Room.builder()
                .number("602")
                .type(RoomType.DOUBLE)
                .status(RoomStatus.AVAILABLE)
                .floor(6)
                .capacity(2)
                .pricePerNight(200.0)
                .hotelId(hotel.getId())
                .build();

        Room room3 = Room.builder()
                .number("603")
                .type(RoomType.DOUBLE)
                .status(RoomStatus.OCCUPIED)
                .floor(6)
                .capacity(2)
                .pricePerNight(210.0)
                .hotelId(hotel.getId())
                .build();

        roomRepository.save(room1);
        roomRepository.save(room2);
        roomRepository.save(room3);

        List<Room> all = roomRepository.findAll().stream()
                .filter(r -> r.getType() == RoomType.DOUBLE)
                .toList();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Room::getNumber)
                .containsExactlyInAnyOrder("602", "603");
    }
}
