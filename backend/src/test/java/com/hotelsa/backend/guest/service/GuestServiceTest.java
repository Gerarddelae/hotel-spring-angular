package com.hotelsa.backend.guest.service;

import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.guest.dto.GuestRequestDTO;
import com.hotelsa.backend.guest.dto.GuestResponseDTO;
import com.hotelsa.backend.guest.mapper.GuestMapper;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.guest.repository.GuestRepository;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private GuestMapper guestMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private GuestService guestService;

    private Hotel hotel;
    private Guest guest;
    private GuestRequestDTO guestRequestDTO;
    private GuestResponseDTO guestResponseDTO;

    @BeforeEach
    void setUp() {
        // no openMocks() needed when using MockitoExtension
        hotel = Hotel.builder()
                .id(1L)
                .name("Hotel Test")
                .address("123 Main St")
                .city("CityX")
                .country("CountryX")
                .phone("1234567890")
                .description("Test Hotel")
                .build();

        // optional: keep tenant context similar to repository tests (not required by service when using authService)
        TenantContext.setCurrentTenant(hotel.getId());

        guest = Guest.builder()
                .id(1L)
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

        guestRequestDTO = GuestRequestDTO.builder()
                .fullName(guest.getFullName())
                .documentNumber(guest.getDocumentNumber())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .address(guest.getAddress())
                .documentType(guest.getDocumentType())
                .previousCancellations(guest.getPreviousCancellations())
                .totalBookingsClient(guest.getTotalBookingsClient())
                .build();

        guestResponseDTO = GuestResponseDTO.builder()
                .fullName(guest.getFullName())
                .email(guest.getEmail())
                .build();

        // common mock behavior
        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());
        when(guestMapper.fromEntity(any(Guest.class))).thenAnswer(invocation -> {
            Guest g = invocation.getArgument(0);
            return GuestResponseDTO.builder()
                    .fullName(g.getFullName())
                    .email(g.getEmail())
                    .build();
        });
        when(guestMapper.fromRequestDto(any(GuestRequestDTO.class))).thenAnswer(invocation -> {
            GuestRequestDTO dto = invocation.getArgument(0);
            return Guest.builder()
                    .fullName(dto.getFullName())
                    .documentNumber(dto.getDocumentNumber())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .address(dto.getAddress())
                    .documentType(dto.getDocumentType())
                    .previousCancellations(dto.getPreviousCancellations())
                    .totalBookingsClient(dto.getTotalBookingsClient())
                    .hotel(hotel)
                    .hotelId(hotel.getId())
                    .build();
        });
    }

    @Test
    void createGuest_ShouldCreateGuestSuccessfully() {
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));
        when(guestRepository.existsByDocumentNumber(guestRequestDTO.getDocumentNumber())).thenReturn(false);
        when(guestRepository.findByEmail(guestRequestDTO.getEmail())).thenReturn(Optional.empty());
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);
        when(guestMapper.fromEntity(any(Guest.class))).thenReturn(guestResponseDTO);

        GuestResponseDTO result = guestService.createGuest(guestRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo(guest.getFullName());
        verify(guestRepository, times(1)).save(any(Guest.class));
    }

    @Test
    void getGuestByEmail_ShouldReturnGuestWhenExists() {
        when(guestRepository.findByEmail("janedoe@example.com")).thenReturn(Optional.of(Guest.builder().fullName("Jane Doe").email("janedoe@example.com").hotel(hotel).build()));
        when(guestMapper.fromEntity(any(Guest.class))).thenReturn(GuestResponseDTO.builder().fullName("Jane Doe").email("janedoe@example.com").build());

        GuestResponseDTO result = guestService.getGuestByEmail("janedoe@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        verify(guestRepository, times(1)).findByEmail("janedoe@example.com");
    }

    @Test
    void deleteGuest_ShouldSoftDeleteSuccessfully() {
        Guest g = Guest.builder().id(1L).hotel(hotel).deleted(false).build();
        when(guestRepository.findById(1L)).thenReturn(Optional.of(g));

        guestService.deleteGuest(1L);

        // entity instance should be marked deleted and saved
        assertTrue(g.isDeleted());
        verify(guestRepository).save(g);
    }
}
