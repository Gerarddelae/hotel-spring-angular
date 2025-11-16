package com.hotelsa.backend.booking.repository;

import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.guest.repository.GuestRepository;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import com.hotelsa.backend.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel hotel;
    private Guest guest;
    private Room room;

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

        guest = guestRepository.save(
                Guest.builder()
                        .fullName("Juan Perez")
                        .documentType("DNI")
                        .documentNumber("12345678")
                        .email("juan@example.com")
                        .phone("555-0101")
                        .address("Calle Falsa 123")
                        .previousCancellations(0)
                        .totalBookingsClient(0)
                        .hotelId(hotel.getId())
                        .build()
        );

        room = roomRepository.save(
                Room.builder()
                        .number("101")
                        .type(RoomType.SINGLE)
                        .status(RoomStatus.AVAILABLE)
                        .floor(1)
                        .capacity(2)
                        .pricePerNight(100.0)
                        .hotelId(hotel.getId())
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void findByCheckOutDate_debeRetornarReservasParaLaFecha() {
        LocalDate checkIn = LocalDate.of(2025, 1, 1);
        LocalDate checkOut = LocalDate.of(2025, 1, 5);

        Booking reserva = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        bookingRepository.save(reserva);

        List<Booking> resultado = bookingRepository.findByCheckOutDate(checkOut);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCheckOutDate()).isEqualTo(checkOut);
    }

    @Test
    void findByCheckOutDateBefore_debeRetornarSoloReservasAnteriores() {
        LocalDate cutoff = LocalDate.of(2025, 6, 1);

        Booking early = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 5, 20))
                .checkOutDate(LocalDate.of(2025, 5, 25))
                .status(BookingStatus.PENDING)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        Booking late = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 6, 10))
                .checkOutDate(LocalDate.of(2025, 6, 15))
                .status(BookingStatus.PENDING)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        bookingRepository.save(early);
        bookingRepository.save(late);

        List<Booking> resultado = bookingRepository.findByCheckOutDateBefore(cutoff);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCheckOutDate()).isBefore(cutoff);
    }

    @Test
    void findByCheckInDate_debeRetornarReservasPorFechaDeEntrada() {
        LocalDate fecha = LocalDate.of(2025, 3, 10);

        Booking reserva = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(fecha)
                .checkOutDate(fecha.plusDays(3))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        bookingRepository.save(reserva);

        List<Booking> resultado = bookingRepository.findByCheckInDate(fecha);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCheckInDate()).isEqualTo(fecha);
    }

    @Test
    void findByGuestId_debeRetornarSoloReservasDelHuesped() {
        // Crear otro huésped y reserva para él
        Guest otherGuest = guestRepository.save(
                Guest.builder()
                        .fullName("Maria Gomez")
                        .documentType("DNI")
                        .documentNumber("87654321")
                        .email("maria@example.com")
                        .phone("555-0202")
                        .address("Avenida 1")
                        .previousCancellations(0)
                        .totalBookingsClient(0)
                        .hotelId(hotel.getId())
                        .build()
        );

        Booking b1 = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 7, 1))
                .checkOutDate(LocalDate.of(2025, 7, 3))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        Booking b2 = Booking.builder()
                .guest(otherGuest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 8, 1))
                .checkOutDate(LocalDate.of(2025, 8, 2))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        bookingRepository.save(b1);
        bookingRepository.save(b2);

        List<Booking> resultado = bookingRepository.findByGuestId(guest.getId());

        assertThat(resultado).hasSize(1);
        assertEquals(guest.getId(), resultado.get(0).getGuest().getId());
    }

    @Test
    void findByRoomIdAndStatus_debeRetornarSoloReservasQueCoincidenHabitacionYEstado() {
        Booking confirmed = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 9, 1))
                .checkOutDate(LocalDate.of(2025, 9, 5))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        Booking cancelled = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 9, 10))
                .checkOutDate(LocalDate.of(2025, 9, 12))
                .status(BookingStatus.CANCELLED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        bookingRepository.save(confirmed);
        bookingRepository.save(cancelled);

        List<Booking> resultado = bookingRepository.findByRoomIdAndStatus(room.getId(), BookingStatus.CONFIRMED);

        assertThat(resultado).hasSize(1);
        assertEquals(BookingStatus.CONFIRMED, resultado.get(0).getStatus());
        assertEquals(room.getId(), resultado.get(0).getRoom().getId());
    }

    @Test
    void findByCheckInDateBetween_debeRetornarReservasEnRangoInclusivo() {
        LocalDate start = LocalDate.of(2025, 10, 1);
        LocalDate end = LocalDate.of(2025, 10, 31);

        Booking inside = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 10, 15))
                .checkOutDate(LocalDate.of(2025, 10, 20))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        Booking onStart = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(start)
                .checkOutDate(start.plusDays(2))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        Booking outside = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 11, 1))
                .checkOutDate(LocalDate.of(2025, 11, 3))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        bookingRepository.save(inside);
        bookingRepository.save(onStart);
        bookingRepository.save(outside);

        List<Booking> resultado = bookingRepository.findByCheckInDateBetween(start, end);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(b -> b.getCheckInDate())
                .containsExactlyInAnyOrder(inside.getCheckInDate(), onStart.getCheckInDate());
    }

    @Test
    void debeGuardarReservaConTenant() {
        Booking reserva = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 1))
                .checkOutDate(LocalDate.of(2025, 12, 5))
                .status(BookingStatus.PENDING)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        Booking guardada = bookingRepository.save(reserva);

        assertNotNull(guardada.getId());
        assertEquals(hotel.getId(), guardada.getHotelId());
    }
}
