package com.hotelsa.backend.room.service;

import com.hotelsa.backend.room.dto.OccupiedRoomsCountDTO;
import com.hotelsa.backend.room.dto.RoomDashboardItemDTO;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para las funcionalidades del Dashboard en RoomService
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceDashboardTest {

    @Mock
    private RoomRepository roomRepository;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        // Crear mocks para todas las dependencias del RoomService
        var hotelRepository = mock(com.hotelsa.backend.hotel.repository.HotelRepository.class);
        var roomMapper = mock(com.hotelsa.backend.room.mapper.RoomMapper.class);
        var authService = mock(com.hotelsa.backend.auth.service.AuthService.class);

        roomService = new RoomService(
                hotelRepository,
                roomRepository,
                roomMapper,
                authService
        );
    }

    @Test
    void getOccupiedCount_debeRetornarContadorDeHabitacionesOcupadas() {
        // Given
        int expectedCount = 28;
        when(roomRepository.countOccupied()).thenReturn(expectedCount);

        // When
        OccupiedRoomsCountDTO result = roomService.getOccupiedCount();

        // Then
        assertThat(result).isNotNull();
        assertEquals(expectedCount, result.getCount());
        verify(roomRepository, times(1)).countOccupied();
    }

    @Test
    void getOccupiedCount_debeManejarCeroCuandoNoHayHabitacionesOcupadas() {
        // Given
        when(roomRepository.countOccupied()).thenReturn(0);

        // When
        OccupiedRoomsCountDTO result = roomService.getOccupiedCount();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, result.getCount());
    }

    @Test
    void getDashboardSummary_debeRetornarListaDeHabitaciones() {
        // Given
        List<RoomDashboardItemDTO> expectedRooms = Arrays.asList(
                new RoomDashboardItemDTO(1L, "101", "OCCUPIED", "SINGLE", 45L),
                new RoomDashboardItemDTO(2L, "102", "AVAILABLE", "DOUBLE", null),
                new RoomDashboardItemDTO(3L, "103", "MAINTENANCE", "SUITE", null)
        );
        when(roomRepository.findDashboardSummary(any(LocalDate.class))).thenReturn(expectedRooms);

        // When
        List<RoomDashboardItemDTO> result = roomService.getDashboardSummary();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getNumber()).isEqualTo("101");
        assertThat(result.get(0).getStatus()).isEqualTo("OCCUPIED");
        assertThat(result.get(0).getCurrentBookingId()).isEqualTo(45L);
        assertThat(result.get(1).getCurrentBookingId()).isNull();

        // Verificar que se llamó con la fecha de hoy
        verify(roomRepository).findDashboardSummary(any(LocalDate.class));
    }

    @Test
    void getDashboardSummary_debeManejarListaVacia() {
        // Given
        when(roomRepository.findDashboardSummary(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<RoomDashboardItemDTO> result = roomService.getDashboardSummary();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getDashboardSummary_debeUsarFechaActual() {
        // Given
        List<RoomDashboardItemDTO> rooms = Collections.emptyList();
        when(roomRepository.findDashboardSummary(any(LocalDate.class))).thenReturn(rooms);

        // When
        roomService.getDashboardSummary();

        // Then
        verify(roomRepository).findDashboardSummary(argThat(date ->
            date.equals(LocalDate.now()) || date.equals(LocalDate.now().minusDays(1))
        ));
    }

    @Test
    void getDashboardSummary_debeIncluirHabitacionesConYSinBookings() {
        // Given
        List<RoomDashboardItemDTO> expectedRooms = Arrays.asList(
                new RoomDashboardItemDTO(1L, "201", "OCCUPIED", "SINGLE", 100L),
                new RoomDashboardItemDTO(2L, "202", "AVAILABLE", "DOUBLE", null),
                new RoomDashboardItemDTO(3L, "203", "OCCUPIED", "SUITE", 101L),
                new RoomDashboardItemDTO(4L, "204", "MAINTENANCE", "SINGLE", null)
        );
        when(roomRepository.findDashboardSummary(any(LocalDate.class))).thenReturn(expectedRooms);

        // When
        List<RoomDashboardItemDTO> result = roomService.getDashboardSummary();

        // Then
        assertThat(result).hasSize(4);

        // Verificar habitaciones con bookings
        long roomsWithBookings = result.stream()
                .filter(r -> r.getCurrentBookingId() != null)
                .count();
        assertEquals(2, roomsWithBookings);

        // Verificar habitaciones sin bookings
        long roomsWithoutBookings = result.stream()
                .filter(r -> r.getCurrentBookingId() == null)
                .count();
        assertEquals(2, roomsWithoutBookings);
    }

    @Test
    void getStatusOptions_debeRetornarTodosLosEstadosDeHabitacion() {
        // When
        List<String> result = roomService.getStatusOptions();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(
                RoomStatus.AVAILABLE.name(),
                RoomStatus.OCCUPIED.name(),
                RoomStatus.MAINTENANCE.name()
        );
    }

    @Test
    void getStatusOptions_debeRetornarNombresEnMayusculas() {
        // When
        List<String> result = roomService.getStatusOptions();

        // Then
        assertThat(result).allMatch(status -> status.equals(status.toUpperCase()));
    }

    @Test
    void getStatusOptions_noDebeInvocarRepository() {
        // When
        roomService.getStatusOptions();

        // Then
        verifyNoInteractions(roomRepository);
    }

    @Test
    void getOccupiedCount_debeInvocarRepositorioUnaVez() {
        // Given
        when(roomRepository.countOccupied()).thenReturn(10);

        // When
        roomService.getOccupiedCount();

        // Then
        verify(roomRepository, times(1)).countOccupied();
        verifyNoMoreInteractions(roomRepository);
    }

    @Test
    void getDashboardSummary_debeInvocarRepositorioUnaVez() {
        // Given
        when(roomRepository.findDashboardSummary(any())).thenReturn(Collections.emptyList());

        // When
        roomService.getDashboardSummary();

        // Then
        verify(roomRepository, times(1)).findDashboardSummary(any(LocalDate.class));
        verifyNoMoreInteractions(roomRepository);
    }

    @Test
    void getDashboardSummary_debePreservarOrdenDelRepository() {
        // Given
        List<RoomDashboardItemDTO> orderedRooms = Arrays.asList(
                new RoomDashboardItemDTO(3L, "103", "OCCUPIED", "SUITE", 50L),
                new RoomDashboardItemDTO(1L, "101", "AVAILABLE", "SINGLE", null),
                new RoomDashboardItemDTO(2L, "102", "MAINTENANCE", "DOUBLE", null)
        );
        when(roomRepository.findDashboardSummary(any())).thenReturn(orderedRooms);

        // When
        List<RoomDashboardItemDTO> result = roomService.getDashboardSummary();

        // Then
        assertThat(result).isNotNull();
        assertEquals("103", result.get(0).getNumber());
        assertEquals("101", result.get(1).getNumber());
        assertEquals("102", result.get(2).getNumber());
    }

    @Test
    void getStatusOptions_debeContenerSoloEstadosValidos() {
        // When
        List<String> result = roomService.getStatusOptions();

        // Then
        assertThat(result).doesNotContain("INVALID_STATUS");
        assertThat(result).allMatch(status -> {
            try {
                RoomStatus.valueOf(status);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }
}

