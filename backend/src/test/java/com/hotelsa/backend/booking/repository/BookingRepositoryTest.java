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

    // ==================== DASHBOARD TESTS ====================

    @Test
    void countByStatus_debeContarCorrectamentePorEstadoPENDING() {
        // Crear bookings con diferentes estados
        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 1))
                .checkOutDate(LocalDate.of(2025, 12, 3))
                .status(BookingStatus.PENDING)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 5))
                .checkOutDate(LocalDate.of(2025, 12, 7))
                .status(BookingStatus.PENDING)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 10))
                .checkOutDate(LocalDate.of(2025, 12, 12))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        int count = bookingRepository.countByStatus(BookingStatus.PENDING);

        assertEquals(2, count);
    }

    @Test
    void countByStatus_debeContarCorrectamentePorEstadoCONFIRMED() {
        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 1))
                .checkOutDate(LocalDate.of(2025, 12, 3))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        int count = bookingRepository.countByStatus(BookingStatus.CONFIRMED);

        assertEquals(1, count);
    }

    @Test
    void countByStatus_debeContarCorrectamentePorEstadoCHECKED_IN() {
        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 1))
                .checkOutDate(LocalDate.of(2025, 12, 3))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 5))
                .checkOutDate(LocalDate.of(2025, 12, 7))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 10))
                .checkOutDate(LocalDate.of(2025, 12, 12))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        int count = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);

        assertEquals(3, count);
    }

    @Test
    void countByStatus_debeRetornarCeroCuandoNoHayReservasConEseEstado() {
        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 12, 1))
                .checkOutDate(LocalDate.of(2025, 12, 3))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        int count = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        assertEquals(0, count);
    }

    @Test
    void countActiveGuestsToday_debeContarHuespedesConCHECKED_INHoy() {
        LocalDate today = LocalDate.now();

        // Guest 1 - CHECKED_IN y dentro del rango (hoy)
        Guest guest1 = guestRepository.save(
                Guest.builder()
                        .fullName("Guest 1")
                        .documentType("DNI")
                        .documentNumber("11111111")
                        .email("guest1@example.com")
                        .phone("555-0001")
                        .address("Calle 1")
                        .previousCancellations(0)
                        .totalBookingsClient(0)
                        .hotelId(hotel.getId())
                        .build()
        );

        bookingRepository.save(Booking.builder()
                .guest(guest1)
                .room(room)
                .checkInDate(today.minusDays(2))
                .checkOutDate(today.plusDays(2))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        // Guest 2 - CHECKED_IN y dentro del rango (hoy)
        Guest guest2 = guestRepository.save(
                Guest.builder()
                        .fullName("Guest 2")
                        .documentType("DNI")
                        .documentNumber("22222222")
                        .email("guest2@example.com")
                        .phone("555-0002")
                        .address("Calle 2")
                        .previousCancellations(0)
                        .totalBookingsClient(0)
                        .hotelId(hotel.getId())
                        .build()
        );

        bookingRepository.save(Booking.builder()
                .guest(guest2)
                .room(room)
                .checkInDate(today.minusDays(1))
                .checkOutDate(today.plusDays(3))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        // Guest 3 - CHECKED_IN pero fuera del rango (pasado)
        Guest guest3 = guestRepository.save(
                Guest.builder()
                        .fullName("Guest 3")
                        .documentType("DNI")
                        .documentNumber("33333333")
                        .email("guest3@example.com")
                        .phone("555-0003")
                        .address("Calle 3")
                        .previousCancellations(0)
                        .totalBookingsClient(0)
                        .hotelId(hotel.getId())
                        .build()
        );

        bookingRepository.save(Booking.builder()
                .guest(guest3)
                .room(room)
                .checkInDate(today.minusDays(10))
                .checkOutDate(today.minusDays(5))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        // Guest 4 - Estado diferente a CHECKED_IN
        Guest guest4 = guestRepository.save(
                Guest.builder()
                        .fullName("Guest 4")
                        .documentType("DNI")
                        .documentNumber("44444444")
                        .email("guest4@example.com")
                        .phone("555-0004")
                        .address("Calle 4")
                        .previousCancellations(0)
                        .totalBookingsClient(0)
                        .hotelId(hotel.getId())
                        .build()
        );

        bookingRepository.save(Booking.builder()
                .guest(guest4)
                .room(room)
                .checkInDate(today.minusDays(1))
                .checkOutDate(today.plusDays(1))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        int count = bookingRepository.countActiveGuestsToday(today);

        assertEquals(2, count);
    }

    @Test
    void countActiveGuestsToday_debeContarUnSoloGuestSiTieneDosBookingsCHECKED_IN() {
        LocalDate today = LocalDate.now();

        // Mismo guest con 2 bookings CHECKED_IN
        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(today.minusDays(2))
                .checkOutDate(today.plusDays(1))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(today.minusDays(1))
                .checkOutDate(today.plusDays(2))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        int count = bookingRepository.countActiveGuestsToday(today);

        // Debe contar solo 1 porque es DISTINCT guest.id
        assertEquals(1, count);
    }

    @Test
    void countActiveGuestsToday_debeRetornarCeroCuandoNoHayGuestesActivos() {
        LocalDate today = LocalDate.now();

        // Booking fuera del rango de hoy
        bookingRepository.save(Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(today.minusDays(10))
                .checkOutDate(today.minusDays(5))
                .status(BookingStatus.CHECKED_IN)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build());

        int count = bookingRepository.countActiveGuestsToday(today);

        assertEquals(0, count);
    }
}
