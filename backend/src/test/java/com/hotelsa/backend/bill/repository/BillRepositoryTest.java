package com.hotelsa.backend.bill.repository;

import com.hotelsa.backend.bill.model.Bill;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.booking.repository.BookingRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class BillRepositoryTest {

    @Autowired
    private BillRepository billRepository;

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
    void findByHotelId_debeRetornarFacturasDelHotelActual() {
        Booking booking1 = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        Booking booking2 = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(3))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);

        Bill bill1 = Bill.builder()
                .booking(booking1)
                .totalAmount(BigDecimal.valueOf(100.0))
                .status(com.hotelsa.backend.bill.enums.BillStatus.UNPAID)
                .hotelId(hotel.getId())
                .build();

        Bill bill2 = Bill.builder()
                .booking(booking2)
                .totalAmount(BigDecimal.valueOf(200.0))
                .status(com.hotelsa.backend.bill.enums.BillStatus.PAID)
                .hotelId(hotel.getId())
                .build();

        billRepository.save(bill1);
        billRepository.save(bill2);

        List<Bill> resultado = billRepository.findByHotelId(hotel.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Bill::getTotalAmount)
                .containsExactlyInAnyOrder(bill1.getTotalAmount(), bill2.getTotalAmount());
    }

    @Test
    void findAll_debeRetornarSoloFacturasDelTenantActual() {
        Booking booking = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        booking = bookingRepository.save(booking);

        Bill bill = Bill.builder()
                .booking(booking)
                .totalAmount(BigDecimal.valueOf(150.0))
                .status(com.hotelsa.backend.bill.enums.BillStatus.UNPAID)
                .hotelId(hotel.getId())
                .build();

        billRepository.save(bill);

        List<Bill> bills = billRepository.findAll();

        assertThat(bills).hasSize(1);
        assertEquals(hotel.getId(), bills.get(0).getHotelId());
    }

    @Test
    void findById_debeRetornarFacturaCuandoExiste() {
        Booking booking = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        booking = bookingRepository.save(booking);

        Bill bill = Bill.builder()
                .booking(booking)
                .totalAmount(BigDecimal.valueOf(300.0))
                .status(com.hotelsa.backend.bill.enums.BillStatus.PAID)
                .hotelId(hotel.getId())
                .build();

        Bill guardada = billRepository.save(bill);

        var encontrada = billRepository.findById(guardada.getId());

        assertThat(encontrada).isPresent();
        assertEquals(guardada.getId(), encontrada.get().getId());
    }

    @Test
    void findById_debeRetornarVacioCuandoNoExiste() {
        var encontrada = billRepository.findById(999L);
        assertThat(encontrada).isEmpty();
    }

    @Test
    void debeGuardarFacturaConTenant() {
        Booking booking = Booking.builder()
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.PENDING)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .hotelId(hotel.getId())
                .build();

        booking = bookingRepository.save(booking);

        Bill bill = Bill.builder()
                .booking(booking)
                .totalAmount(BigDecimal.valueOf(50.0))
                .status(com.hotelsa.backend.bill.enums.BillStatus.UNPAID)
                .hotelId(hotel.getId())
                .build();

        Bill guardada = billRepository.save(bill);

        assertNotNull(guardada.getId());
        assertEquals(hotel.getId(), guardada.getHotelId());
    }
}
