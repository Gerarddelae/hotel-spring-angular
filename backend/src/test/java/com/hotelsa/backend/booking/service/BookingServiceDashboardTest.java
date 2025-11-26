package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.booking.dto.ActiveGuestsCountDTO;
import com.hotelsa.backend.booking.dto.BookingStatusCountDTO;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para las funcionalidades del Dashboard en BookingService
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceDashboardTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        // Crear mocks para todas las dependencias del BookingService
        var hotelRepository = mock(com.hotelsa.backend.hotel.repository.HotelRepository.class);
        var guestRepository = mock(com.hotelsa.backend.guest.repository.GuestRepository.class);
        var roomRepository = mock(com.hotelsa.backend.room.repository.RoomRepository.class);
        var bookingMapper = mock(com.hotelsa.backend.booking.mapper.BookingMapper.class);
        var authService = mock(com.hotelsa.backend.auth.service.AuthService.class);
        var addonRepository = mock(com.hotelsa.backend.addon.repository.AddonRepository.class);
        var bookingAddonRepository = mock(com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository.class);

        bookingService = new BookingService(
                hotelRepository,
                bookingRepository,
                guestRepository,
                roomRepository,
                bookingMapper,
                authService,
                addonRepository,
                bookingAddonRepository
        );
    }

    @Test
    void countByStatus_debeRetornarContadoresPorEstado() {
        // Given
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(5);
        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(10);
        when(bookingRepository.countByStatus(BookingStatus.CHECKED_IN)).thenReturn(8);

        // When
        BookingStatusCountDTO result = bookingService.countByStatus();

        // Then
        assertThat(result).isNotNull();
        assertEquals(23, result.getTotal()); // 5 + 10 + 8
        assertEquals(5, result.getPending());
        assertEquals(10, result.getConfirmed());
        assertEquals(8, result.getCheckedIn());

        // Verificar que se llamaron los métodos correctos
        verify(bookingRepository).countByStatus(BookingStatus.PENDING);
        verify(bookingRepository).countByStatus(BookingStatus.CONFIRMED);
        verify(bookingRepository).countByStatus(BookingStatus.CHECKED_IN);
    }

    @Test
    void countByStatus_debeManejarContadoresCero() {
        // Given
        when(bookingRepository.countByStatus(any())).thenReturn(0);

        // When
        BookingStatusCountDTO result = bookingService.countByStatus();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getPending());
        assertEquals(0, result.getConfirmed());
        assertEquals(0, result.getCheckedIn());
    }

    @Test
    void countByStatus_debeCalcularTotalCorrectamente() {
        // Given
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(12);
        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(18);
        when(bookingRepository.countByStatus(BookingStatus.CHECKED_IN)).thenReturn(15);

        // When
        BookingStatusCountDTO result = bookingService.countByStatus();

        // Then
        assertEquals(45, result.getTotal());
        assertEquals(12, result.getPending());
        assertEquals(18, result.getConfirmed());
        assertEquals(15, result.getCheckedIn());
    }

    @Test
    void getActiveGuestsCount_debeRetornarContadorDeHuespedesActivos() {
        // Given
        int expectedCount = 15;
        when(bookingRepository.countActiveGuestsToday(any(LocalDate.class))).thenReturn(expectedCount);

        // When
        ActiveGuestsCountDTO result = bookingService.getActiveGuestsCount();

        // Then
        assertThat(result).isNotNull();
        assertEquals(expectedCount, result.getCount());

        // Verificar que se llamó con la fecha de hoy
        verify(bookingRepository).countActiveGuestsToday(any(LocalDate.class));
    }

    @Test
    void getActiveGuestsCount_debeManejarCeroCuandoNoHayHuespedesActivos() {
        // Given
        when(bookingRepository.countActiveGuestsToday(any(LocalDate.class))).thenReturn(0);

        // When
        ActiveGuestsCountDTO result = bookingService.getActiveGuestsCount();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, result.getCount());
    }

    @Test
    void getActiveGuestsCount_debeUsarFechaActual() {
        // Given
        LocalDate today = LocalDate.now();
        when(bookingRepository.countActiveGuestsToday(today)).thenReturn(20);

        // When
        ActiveGuestsCountDTO result = bookingService.getActiveGuestsCount();

        // Then
        assertThat(result).isNotNull();
        assertEquals(20, result.getCount());

        // Verificar que se usó la fecha actual (con un pequeño margen de tolerancia)
        verify(bookingRepository).countActiveGuestsToday(argThat(date ->
            date.equals(LocalDate.now()) || date.equals(LocalDate.now().minusDays(1))
        ));
    }

    @Test
    void countByStatus_debeInvocarRepositorioParaCadaEstado() {
        // Given
        when(bookingRepository.countByStatus(any())).thenReturn(1);

        // When
        bookingService.countByStatus();

        // Then
        verify(bookingRepository, times(1)).countByStatus(BookingStatus.PENDING);
        verify(bookingRepository, times(1)).countByStatus(BookingStatus.CONFIRMED);
        verify(bookingRepository, times(1)).countByStatus(BookingStatus.CHECKED_IN);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getActiveGuestsCount_debeInvocarRepositorioUnaVez() {
        // Given
        when(bookingRepository.countActiveGuestsToday(any())).thenReturn(10);

        // When
        bookingService.getActiveGuestsCount();

        // Then
        verify(bookingRepository, times(1)).countActiveGuestsToday(any(LocalDate.class));
        verifyNoMoreInteractions(bookingRepository);
    }
}

