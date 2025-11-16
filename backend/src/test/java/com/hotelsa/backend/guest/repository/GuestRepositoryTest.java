package com.hotelsa.backend.guest.repository;

import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class GuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

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
    void existsByDocumentNumber_debeRetornarTrueCuandoExiste() {
        Guest guest = Guest.builder()
                .fullName("John Doe")
                .documentNumber("123456789")
                .email("johndoe@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .documentType("DNI")
                .previousCancellations(0)
                .totalBookingsClient(5)
                .hotel(hotel)
                .hotelId(hotel.getId())
                .build();

        guestRepository.save(guest);

        boolean exists = guestRepository.existsByDocumentNumber("123456789");

        assertTrue(exists);
    }

    @Test
    void existsByDocumentNumber_debeRetornarFalseCuandoNoExiste() {
        boolean exists = guestRepository.existsByDocumentNumber("987654321");
        assertFalse(exists);
    }

    @Test
    void findByEmail_debeRetornarGuestCuandoExiste() {
        Guest guest = Guest.builder()
                .fullName("Jane Doe")
                .documentNumber("987654321")
                .email("janedoe@example.com")
                .phone("0987654321")
                .address("456 Elm St")
                .documentType("Passport")
                .previousCancellations(1)
                .totalBookingsClient(3)
                .hotel(hotel)
                .hotelId(hotel.getId())
                .build();

        guestRepository.save(guest);

        Optional<Guest> found = guestRepository.findByEmail("janedoe@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void findByEmail_debeRetornarVacioCuandoNoExiste() {
        Optional<Guest> found = guestRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_debeRetornarSoloGuestsDelTenantActual() {
        Guest guest1 = Guest.builder()
                .fullName("Alice")
                .documentNumber("111111111")
                .email("alice@example.com")
                .phone("1111111111")
                .address("789 Oak St")
                .documentType("ID")
                .previousCancellations(0)
                .totalBookingsClient(2)
                .hotel(hotel)
                .hotelId(hotel.getId())
                .build();

        Guest guest2 = Guest.builder()
                .fullName("Bob")
                .documentNumber("222222222")
                .email("bob@example.com")
                .phone("2222222222")
                .address("101 Pine St")
                .documentType("ID")
                .previousCancellations(1)
                .totalBookingsClient(4)
                .hotel(hotel)
                .hotelId(hotel.getId())
                .build();

        guestRepository.save(guest1);
        guestRepository.save(guest2);

        List<Guest> guests = guestRepository.findAll();

        assertThat(guests).hasSize(2);
        assertThat(guests).extracting(Guest::getFullName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }
}
