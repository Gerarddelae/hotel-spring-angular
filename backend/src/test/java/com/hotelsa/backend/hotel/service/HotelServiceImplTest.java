package com.hotelsa.backend.hotel.service;

import com.hotelsa.backend.hotel.dto.HotelResponse;
import com.hotelsa.backend.hotel.dto.HotelUpdateRequest;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.mapper.HotelMapper;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel testHotel;
    private HotelUpdateRequest updateRequest;
    private HotelResponse hotelResponse;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        testHotel = Hotel.builder()
                .id(1L)
                .name("Hotel Test")
                .address("Calle Test 123")
                .city("Ciudad Test")
                .country("País Test")
                .phone("+1234567890")
                .description("Descripción de prueba")
                .build();

        updateRequest = HotelUpdateRequest.builder()
                .name("Hotel Actualizado")
                .address("Nueva Dirección 456")
                .city("Nueva Ciudad")
                .country("Nuevo País")
                .phone("+0987654321")
                .description("Nueva descripción")
                .build();

        hotelResponse = HotelResponse.builder()
                .id(1L)
                .name("Hotel Actualizado")
                .address("Nueva Dirección 456")
                .city("Nueva Ciudad")
                .country("Nuevo País")
                .phone("+0987654321")
                .description("Nueva descripción")
                .build();

        // Configurar TenantContext
        TenantContext.setCurrentTenant(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("updateHotel - Actualización completa exitosa")
    void updateHotel_Success() {
        // Arrange
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);
        when(hotelMapper.toResponse(any(Hotel.class))).thenReturn(hotelResponse);

        // Act
        HotelResponse result = hotelService.updateHotel(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Hotel Actualizado", result.getName());

        ArgumentCaptor<Hotel> hotelCaptor = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepository).save(hotelCaptor.capture());

        Hotel savedHotel = hotelCaptor.getValue();
        assertEquals("Hotel Actualizado", savedHotel.getName());
        assertEquals("Nueva Dirección 456", savedHotel.getAddress());
        assertEquals("Nueva Ciudad", savedHotel.getCity());
        assertEquals("Nuevo País", savedHotel.getCountry());
        assertEquals("+0987654321", savedHotel.getPhone());
        assertEquals("Nueva descripción", savedHotel.getDescription());
    }

    @Test
    @DisplayName("updateHotel - Hotel no encontrado lanza excepción")
    void updateHotel_NotFound() {
        // Arrange
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () -> 
            hotelService.updateHotel(999L, updateRequest)
        );

        verify(hotelRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateHotel - Tenant diferente lanza excepción")
    void updateHotel_WrongTenant() {
        // Arrange
        TenantContext.setCurrentTenant(2L); // Tenant diferente
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () -> 
            hotelService.updateHotel(1L, updateRequest)
        );

        verify(hotelRepository, never()).save(any());
    }

    @Test
    @DisplayName("patchHotel - Actualización parcial solo campos no nulos")
    void patchHotel_PartialUpdate() {
        // Arrange
        HotelUpdateRequest partialRequest = HotelUpdateRequest.builder()
                .name("Nuevo Nombre")
                .phone("+9999999999")
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);
        when(hotelMapper.toResponse(any(Hotel.class))).thenReturn(hotelResponse);

        // Act
        HotelResponse result = hotelService.patchHotel(1L, partialRequest);

        // Assert
        assertNotNull(result);
        verify(hotelMapper).updateEntityFromRequest(eq(partialRequest), any(Hotel.class));
        verify(hotelRepository).save(any(Hotel.class));
    }

    @Test
    @DisplayName("patchHotel - Hotel no encontrado lanza excepción")
    void patchHotel_NotFound() {
        // Arrange
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () -> 
            hotelService.patchHotel(999L, updateRequest)
        );

        verify(hotelRepository, never()).save(any());
    }

    @Test
    @DisplayName("patchHotel - Tenant diferente lanza excepción")
    void patchHotel_WrongTenant() {
        // Arrange
        TenantContext.setCurrentTenant(3L); // Tenant diferente
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () -> 
            hotelService.patchHotel(1L, updateRequest)
        );

        verify(hotelRepository, never()).save(any());
    }

    @Test
    @DisplayName("getHotelById - Obtención exitosa")
    void getHotelById_Success() {
        // Arrange
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(hotelMapper.toResponse(testHotel)).thenReturn(hotelResponse);

        // Act
        HotelResponse result = hotelService.getHotelById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(hotelRepository).findById(1L);
        verify(hotelMapper).toResponse(testHotel);
    }

    @Test
    @DisplayName("getHotelById - Hotel no encontrado lanza excepción")
    void getHotelById_NotFound() {
        // Arrange
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () -> 
            hotelService.getHotelById(999L)
        );
    }

    @Test
    @DisplayName("getHotelById - Tenant diferente lanza excepción")
    void getHotelById_WrongTenant() {
        // Arrange
        TenantContext.setCurrentTenant(5L); // Tenant diferente
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () -> 
            hotelService.getHotelById(1L)
        );
    }

    @Test
    @DisplayName("Tenant nulo permite acceso (para casos especiales)")
    void updateHotel_NullTenant_AllowsAccess() {
        // Arrange
        TenantContext.clear(); // Tenant nulo
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);
        when(hotelMapper.toResponse(any(Hotel.class))).thenReturn(hotelResponse);

        // Act
        HotelResponse result = hotelService.updateHotel(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(hotelRepository).save(any(Hotel.class));
    }
}
