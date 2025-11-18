package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.addon.model.Addon;
import com.hotelsa.backend.addon.repository.AddonRepository;
import com.hotelsa.backend.addon.mapper.AddonMapper;
import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.exception.BookingAddonNotFoundException;
import com.hotelsa.backend.booking.exception.BookingNotFoundException;
import com.hotelsa.backend.booking.mapper.BookingMapper;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.bookingaddon.entity.BookingAddon;
import com.hotelsa.backend.bookingaddon.entity.BookingAddonId;
import com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.guest.repository.GuestRepository;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.auth.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private GuestRepository guestRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private AuthService authService;

    @Mock private AddonRepository addonRepository;
    @Mock private BookingAddonRepository bookingAddonRepository;
    @Mock private AddonMapper addonMapper;

    @InjectMocks private BookingService bookingService;

    private Hotel hotel;
    private Guest guest;
    private Room room;
    private Booking booking;
    private BookingRequestDTO bookingRequestDTO;
    private BookingResponseDTO bookingResponseDTO;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(10L);
        hotel.setName("Test Hotel");

        guest = Guest.builder()
                .id(1L)
                .fullName("Cliente Uno")
                .documentType("DNI")
                .documentNumber("11111111")
                .email("cliente@ejemplo.com")
                .phone("600000000")
                .address("Calle")
                .previousCancellations(0)
                .totalBookingsClient(0)
                .hotelId(hotel.getId())
                .build();

        room = Room.builder()
                .id(2L)
                .hotelId(hotel.getId())
                .number("201")
                .build();

        booking = Booking.builder()
                .id(5L)
                .hotelId(hotel.getId())
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.of(2025, 1, 10))
                .checkOutDate(LocalDate.of(2025, 1, 15))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .build();

        bookingRequestDTO = BookingRequestDTO.builder()
                .guestId(guest.getId())
                .roomId(room.getId())
                .checkInDate(LocalDate.of(2025, 2, 1))
                .checkOutDate(LocalDate.of(2025, 2, 5))
                .status(BookingStatus.PENDING)
                .createdBy("user")
                .bookingLeadTime(LocalDate.now())
                .build();

        bookingResponseDTO = BookingResponseDTO.builder()
                .id(5L)
                .hotelId(hotel.getId())
                .guestId(guest.getId())
                .roomId(room.getId())
                .build();

        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());
    }

    @Test
    void createBooking_ShouldCreateBookingSuccessfully() {
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));
        when(guestRepository.findById(guest.getId())).thenReturn(Optional.of(guest));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(bookingMapper.fromRequestDto(bookingRequestDTO)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.fromEntity(booking)).thenReturn(bookingResponseDTO);

        BookingResponseDTO res = bookingService.create(bookingRequestDTO);

        assertNotNull(res);
        assertEquals(hotel.getId(), res.getHotelId());
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBooking_ShouldThrow_WhenCheckOutNotAfterCheckIn() {
        BookingRequestDTO badDto = BookingRequestDTO.builder()
                .guestId(guest.getId())
                .roomId(room.getId())
                .checkInDate(LocalDate.of(2025, 3, 10))
                .checkOutDate(LocalDate.of(2025, 3, 10))
                .createdBy("user")
                .bookingLeadTime(LocalDate.now())
                .build();

        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));
        when(guestRepository.findById(guest.getId())).thenReturn(Optional.of(guest));
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        assertThrows(IllegalArgumentException.class, () -> bookingService.create(badDto));
    }

    @Test
    void updateBooking_ShouldThrow_WhenBookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.update(999L, bookingRequestDTO));
    }

    @Test
    void getBookingsBetween_ShouldThrow_WhenEndBeforeStart() {
        LocalDate start = LocalDate.of(2025, 5, 10);
        LocalDate end = LocalDate.of(2025, 5, 1);

        assertThrows(IllegalArgumentException.class, () -> bookingService.getBookingsBetween(start, end));
    }

    @Test
    void addAddonsToBooking_ShouldThrow_WhenSomeAddonDoesNotExist() {
        Long bookingId = booking.getId();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        List<com.hotelsa.backend.booking.dto.BookingAddonRequest> reqs = List.of(
                com.hotelsa.backend.booking.dto.BookingAddonRequest.builder().addonId(100L).quantity(1).build(),
                com.hotelsa.backend.booking.dto.BookingAddonRequest.builder().addonId(101L).quantity(2).build()
        );

        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());
        when(addonRepository.findByIdIn(List.of(100L, 101L))).thenReturn(List.of(new Addon())); // size mismatch

        assertThrows(com.hotelsa.backend.addon.exception.AddonNotFoundException.class,
                () -> bookingService.addAddonsToBooking(bookingId, reqs));
    }

    @Test
    void addAddonsToBooking_ShouldCreateBookingAddonLinks_WhenValid() {
        Long bookingId = booking.getId();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        com.hotelsa.backend.addon.model.Addon a = new com.hotelsa.backend.addon.model.Addon();
        a.setId(200L);
        a.setHotelId(hotel.getId());
        a.setPrice(50);

        List<com.hotelsa.backend.booking.dto.BookingAddonRequest> reqs = List.of(
                com.hotelsa.backend.booking.dto.BookingAddonRequest.builder().addonId(a.getId()).quantity(2).build()
        );

        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());
        when(addonRepository.findByIdIn(List.of(a.getId()))).thenReturn(List.of(a));
        when(bookingAddonRepository.existsByIdBookingIdAndIdAddonIdAndHotelId(bookingId, a.getId(), hotel.getId()))
                .thenReturn(false);

        // Simular guardado del link
        when(bookingAddonRepository.save(any(BookingAddon.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.fromEntity(booking)).thenReturn(bookingResponseDTO);

        BookingResponseDTO response = bookingService.addAddonsToBooking(bookingId, reqs);

        assertNotNull(response);
        verify(bookingAddonRepository).save(any(BookingAddon.class));
    }

    @Test
    void updateAddonQuantity_ShouldThrow_WhenLinkIsDeleted() {
        Long bookingId = booking.getId();
        Long addonId = 300L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingAddonId id = new BookingAddonId(bookingId, addonId);
        BookingAddon link = BookingAddon.builder().id(id).hotelId(hotel.getId()).deleted(true).quantity(1).build();
        when(bookingAddonRepository.findByIdAndHotelId(id, hotel.getId())).thenReturn(Optional.of(link));
        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());

        assertThrows(BookingAddonNotFoundException.class, () -> bookingService.updateAddonQuantity(bookingId, addonId, 5));
    }

    @Test
    void removeAddonFromBooking_ShouldSoftDeleteLinkSuccessfully() {
        Long bookingId = booking.getId();
        Long addonId = 400L;
        BookingAddonId id = new BookingAddonId(bookingId, addonId);
        BookingAddon link = BookingAddon.builder().id(id).hotelId(hotel.getId()).deleted(false).quantity(1).build();

        when(bookingAddonRepository.findByIdAndHotelId(id, hotel.getId())).thenReturn(Optional.of(link));
        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());

        bookingService.removeAddonFromBooking(bookingId, addonId);

        assertTrue(link.isDeleted());
        verify(bookingAddonRepository).save(link);
    }
}
