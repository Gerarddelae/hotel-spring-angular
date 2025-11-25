package com.hotelsa.backend.bill.service;

import com.hotelsa.backend.bill.dto.RevenueDTO;
import com.hotelsa.backend.bill.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para las funcionalidades del Dashboard en BillService
 */
@ExtendWith(MockitoExtension.class)
class BillServiceDashboardTest {

    @Mock
    private BillRepository billRepository;

    private BillService billService;

    @BeforeEach
    void setUp() {
        // Crear mocks para todas las dependencias del BillService
        var billMapper = mock(com.hotelsa.backend.bill.mapper.BillMapper.class);
        var billAddonRepository = mock(com.hotelsa.backend.billaddon.repository.BillAddonRepository.class);
        var bookingRepository = mock(com.hotelsa.backend.booking.repository.BookingRepository.class);
        var bookingAddonRepository = mock(com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository.class);
        var billAddonMapper = mock(com.hotelsa.backend.billaddon.mapper.BillAddonMapper.class);
        var authService = mock(com.hotelsa.backend.auth.service.AuthService.class);

        billService = new BillService(
                billRepository,
                billMapper,
                billAddonRepository,
                bookingRepository,
                bookingAddonRepository,
                billAddonMapper,
                authService
        );
    }

    @Test
    void getTotalRevenue_debeRetornarIngresosTotales() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("125000.00");
        when(billRepository.sumTotalRevenue()).thenReturn(expectedTotal);

        // When
        RevenueDTO result = billService.getTotalRevenue();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, expectedTotal.compareTo(result.getTotal()));
        assertEquals("USD", result.getCurrency());
        verify(billRepository, times(1)).sumTotalRevenue();
    }

    @Test
    void getTotalRevenue_debeManejarCeroCuandoNoHayIngresos() {
        // Given
        when(billRepository.sumTotalRevenue()).thenReturn(BigDecimal.ZERO);

        // When
        RevenueDTO result = billService.getTotalRevenue();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotal()));
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void getTotalRevenueToday_debeRetornarIngresosDeLaFechaActual() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("5600.00");
        when(billRepository.sumRevenueByDate(any(LocalDate.class))).thenReturn(expectedTotal);

        // When
        RevenueDTO result = billService.getTotalRevenueToday();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, expectedTotal.compareTo(result.getTotal()));
        assertEquals("USD", result.getCurrency());
        verify(billRepository).sumRevenueByDate(any(LocalDate.class));
    }

    @Test
    void getTotalRevenueToday_debeUsarFechaActual() {
        // Given
        when(billRepository.sumRevenueByDate(any())).thenReturn(BigDecimal.ZERO);

        // When
        billService.getTotalRevenueToday();

        // Then
        verify(billRepository).sumRevenueByDate(argThat(date ->
            date.equals(LocalDate.now()) || date.equals(LocalDate.now().minusDays(1))
        ));
    }

    @Test
    void getTotalRevenueToday_debeManejarCeroCuandoNoHayIngresosHoy() {
        // Given
        when(billRepository.sumRevenueByDate(any())).thenReturn(BigDecimal.ZERO);

        // When
        RevenueDTO result = billService.getTotalRevenueToday();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotal()));
    }

    @Test
    void getTotalRevenueMonth_debeRetornarIngresosDelMesActual() {
        // Given
        BigDecimal expectedTotal = new BigDecimal("48500.00");
        when(billRepository.sumRevenueByMonth(anyInt(), anyInt())).thenReturn(expectedTotal);

        // When
        RevenueDTO result = billService.getTotalRevenueMonth();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, expectedTotal.compareTo(result.getTotal()));
        assertEquals("USD", result.getCurrency());
        verify(billRepository).sumRevenueByMonth(anyInt(), anyInt());
    }

    @Test
    void getTotalRevenueMonth_debeUsarMesYAnioActual() {
        // Given
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        when(billRepository.sumRevenueByMonth(currentMonth, currentYear))
                .thenReturn(new BigDecimal("10000.00"));

        // When
        RevenueDTO result = billService.getTotalRevenueMonth();

        // Then
        assertThat(result).isNotNull();
        verify(billRepository).sumRevenueByMonth(eq(currentMonth), eq(currentYear));
    }

    @Test
    void getTotalRevenueMonth_debeManejarCeroCuandoNoHayIngresosEnElMes() {
        // Given
        when(billRepository.sumRevenueByMonth(anyInt(), anyInt())).thenReturn(BigDecimal.ZERO);

        // When
        RevenueDTO result = billService.getTotalRevenueMonth();

        // Then
        assertThat(result).isNotNull();
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotal()));
    }

    @Test
    void getTotalRevenue_debeRetornarMonedaUSD() {
        // Given
        when(billRepository.sumTotalRevenue()).thenReturn(new BigDecimal("1000.00"));

        // When
        RevenueDTO result = billService.getTotalRevenue();

        // Then
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void getTotalRevenueToday_debeRetornarMonedaUSD() {
        // Given
        when(billRepository.sumRevenueByDate(any())).thenReturn(new BigDecimal("1000.00"));

        // When
        RevenueDTO result = billService.getTotalRevenueToday();

        // Then
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void getTotalRevenueMonth_debeRetornarMonedaUSD() {
        // Given
        when(billRepository.sumRevenueByMonth(anyInt(), anyInt())).thenReturn(new BigDecimal("1000.00"));

        // When
        RevenueDTO result = billService.getTotalRevenueMonth();

        // Then
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void getTotalRevenue_debeInvocarRepositorioUnaVez() {
        // Given
        when(billRepository.sumTotalRevenue()).thenReturn(BigDecimal.ZERO);

        // When
        billService.getTotalRevenue();

        // Then
        verify(billRepository, times(1)).sumTotalRevenue();
        verifyNoMoreInteractions(billRepository);
    }

    @Test
    void getTotalRevenueToday_debeInvocarRepositorioUnaVez() {
        // Given
        when(billRepository.sumRevenueByDate(any())).thenReturn(BigDecimal.ZERO);

        // When
        billService.getTotalRevenueToday();

        // Then
        verify(billRepository, times(1)).sumRevenueByDate(any(LocalDate.class));
        verifyNoMoreInteractions(billRepository);
    }

    @Test
    void getTotalRevenueMonth_debeInvocarRepositorioUnaVez() {
        // Given
        when(billRepository.sumRevenueByMonth(anyInt(), anyInt())).thenReturn(BigDecimal.ZERO);

        // When
        billService.getTotalRevenueMonth();

        // Then
        verify(billRepository, times(1)).sumRevenueByMonth(anyInt(), anyInt());
        verifyNoMoreInteractions(billRepository);
    }

    @Test
    void getTotalRevenue_debePreservarPrecisionDecimal() {
        // Given
        BigDecimal preciseAmount = new BigDecimal("12345.67");
        when(billRepository.sumTotalRevenue()).thenReturn(preciseAmount);

        // When
        RevenueDTO result = billService.getTotalRevenue();

        // Then
        assertEquals(0, preciseAmount.compareTo(result.getTotal()));
    }

    @Test
    void getTotalRevenueToday_debePreservarPrecisionDecimal() {
        // Given
        BigDecimal preciseAmount = new BigDecimal("9876.54");
        when(billRepository.sumRevenueByDate(any())).thenReturn(preciseAmount);

        // When
        RevenueDTO result = billService.getTotalRevenueToday();

        // Then
        assertEquals(0, preciseAmount.compareTo(result.getTotal()));
    }

    @Test
    void getTotalRevenueMonth_debePreservarPrecisionDecimal() {
        // Given
        BigDecimal preciseAmount = new BigDecimal("54321.98");
        when(billRepository.sumRevenueByMonth(anyInt(), anyInt())).thenReturn(preciseAmount);

        // When
        RevenueDTO result = billService.getTotalRevenueMonth();

        // Then
        assertEquals(0, preciseAmount.compareTo(result.getTotal()));
    }

    @Test
    void todosLosMetodos_debenManejarMontosGrandes() {
        // Given
        BigDecimal largeAmount = new BigDecimal("999999999.99");
        when(billRepository.sumTotalRevenue()).thenReturn(largeAmount);
        when(billRepository.sumRevenueByDate(any())).thenReturn(largeAmount);
        when(billRepository.sumRevenueByMonth(anyInt(), anyInt())).thenReturn(largeAmount);

        // When
        RevenueDTO total = billService.getTotalRevenue();
        RevenueDTO today = billService.getTotalRevenueToday();
        RevenueDTO month = billService.getTotalRevenueMonth();

        // Then
        assertEquals(0, largeAmount.compareTo(total.getTotal()));
        assertEquals(0, largeAmount.compareTo(today.getTotal()));
        assertEquals(0, largeAmount.compareTo(month.getTotal()));
    }
}

