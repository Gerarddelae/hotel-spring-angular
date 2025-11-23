package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private RoomRepository roomRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        roomRepository = Mockito.mock(RoomRepository.class);
        authService = Mockito.mock(AuthService.class);

        // mocks for unused dependencies
        var hotelRepository = Mockito.mock(HotelRepository.class);
        var guestRepository = Mockito.mock(com.hotelsa.backend.guest.repository.GuestRepository.class);
        var bookingMapper = Mockito.mock(com.hotelsa.backend.booking.mapper.BookingMapper.class);
        var addonRepository = Mockito.mock(com.hotelsa.backend.addon.repository.AddonRepository.class);
        var bookingAddonRepository = Mockito.mock(com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository.class);

        // Instanciar el servicio bajo prueba con mocks
        var bookingService = new com.hotelsa.backend.booking.service.BookingService(
                hotelRepository,
                bookingRepository,
                guestRepository,
                roomRepository,
                bookingMapper,
                authService,
                addonRepository,
                bookingAddonRepository
        );

        // Guardar instancia en campo local para los tests (si se requiere)
        // Pero los tests llaman bookingService a través de la variable local creada arriba.

        when(authService.getCurrentHotelId()).thenReturn(10L);
    }

    @Test
    void roomOccupiedIsNotAvailableWhenActiveBookingExists() {
        Long roomId = 3L;
        Room room = new Room();
        room.setId(roomId);
        room.setHotelId(10L);
        room.setStatus(RoomStatus.OCCUPIED);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong()
        )).thenReturn(true);

        // Crear instancia para invocar método
        var bookingService = new com.hotelsa.backend.booking.service.BookingService(
                Mockito.mock(HotelRepository.class),
                bookingRepository,
                Mockito.mock(com.hotelsa.backend.guest.repository.GuestRepository.class),
                roomRepository,
                Mockito.mock(com.hotelsa.backend.booking.mapper.BookingMapper.class),
                authService,
                Mockito.mock(com.hotelsa.backend.addon.repository.AddonRepository.class),
                Mockito.mock(com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository.class)
        );

        boolean available = bookingService.isRoomAvailable(roomId, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        assertFalse(available, "Room OCCUPIED with an active overlapping booking should not be available");

        verify(roomRepository, times(1)).findById(roomId);
        verify(bookingRepository, times(1)).existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong());
    }

    @Test
    void roomOccupiedButNoActiveBookingIsAvailable() {
        Long roomId = 6L;
        Room room = new Room();
        room.setId(roomId);
        room.setHotelId(10L);
        room.setStatus(RoomStatus.OCCUPIED);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong()
        )).thenReturn(false);

        var bookingService = new com.hotelsa.backend.booking.service.BookingService(
                Mockito.mock(HotelRepository.class),
                bookingRepository,
                Mockito.mock(com.hotelsa.backend.guest.repository.GuestRepository.class),
                roomRepository,
                Mockito.mock(com.hotelsa.backend.booking.mapper.BookingMapper.class),
                authService,
                Mockito.mock(com.hotelsa.backend.addon.repository.AddonRepository.class),
                Mockito.mock(com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository.class)
        );

        boolean available = bookingService.isRoomAvailable(roomId, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        assertTrue(available, "Room OCCUPIED but without active overlapping bookings should be considered available");

        verify(roomRepository, times(1)).findById(roomId);
        verify(bookingRepository, times(1)).existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong());
    }

    @Test
    void roomAvailableWhenNoOverlappingBookings() {
        Long roomId = 4L;
        Room room = new Room();
        room.setId(roomId);
        room.setHotelId(10L);
        room.setStatus(RoomStatus.AVAILABLE);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong()))
                .thenReturn(false);

        var bookingService = new com.hotelsa.backend.booking.service.BookingService(
                Mockito.mock(HotelRepository.class),
                bookingRepository,
                Mockito.mock(com.hotelsa.backend.guest.repository.GuestRepository.class),
                roomRepository,
                Mockito.mock(com.hotelsa.backend.booking.mapper.BookingMapper.class),
                authService,
                Mockito.mock(com.hotelsa.backend.addon.repository.AddonRepository.class),
                Mockito.mock(com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository.class)
        );

        boolean available = bookingService.isRoomAvailable(roomId, LocalDate.of(2025,12,1), LocalDate.of(2025,12,5));
        assertTrue(available, "Room AVAILABLE with no overlapping bookings should be available");

        verify(roomRepository, times(1)).findById(roomId);
        verify(bookingRepository, times(1)).existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong());
    }

    @Test
    void roomNotAvailableWhenOverlappingBookingExists() {
        Long roomId = 5L;
        Room room = new Room();
        room.setId(roomId);
        room.setHotelId(10L);
        room.setStatus(RoomStatus.AVAILABLE);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong()))
                .thenReturn(true);

        var bookingService = new com.hotelsa.backend.booking.service.BookingService(
                Mockito.mock(HotelRepository.class),
                bookingRepository,
                Mockito.mock(com.hotelsa.backend.guest.repository.GuestRepository.class),
                roomRepository,
                Mockito.mock(com.hotelsa.backend.booking.mapper.BookingMapper.class),
                authService,
                Mockito.mock(com.hotelsa.backend.addon.repository.AddonRepository.class),
                Mockito.mock(com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository.class)
        );

        boolean available = bookingService.isRoomAvailable(roomId, LocalDate.of(2025,12,10), LocalDate.of(2025,12,15));
        assertFalse(available, "Room AVAILABLE but with overlapping booking should not be available");

        verify(roomRepository, times(1)).findById(roomId);
        verify(bookingRepository, times(1)).existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                eq(roomId), any(), any(LocalDate.class), any(LocalDate.class), anyLong());
    }
}
