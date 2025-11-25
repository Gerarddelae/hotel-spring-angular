package com.hotelsa.backend.bill.service;

import com.hotelsa.backend.bill.dto.BillRequestDTO;
import com.hotelsa.backend.bill.dto.BillResponseDTO;
import com.hotelsa.backend.bill.mapper.BillMapper;
import com.hotelsa.backend.bill.model.Bill;
import com.hotelsa.backend.billaddon.entity.BillAddon;
import com.hotelsa.backend.billaddon.entity.BillAddonId;
import com.hotelsa.backend.billaddon.mapper.BillAddonMapper;
import com.hotelsa.backend.billaddon.repository.BillAddonRepository;
import com.hotelsa.backend.bill.repository.BillRepository;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.bookingaddon.entity.BookingAddon;
import com.hotelsa.backend.bookingaddon.entity.BookingAddonId;
import com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.bill.enums.BillStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillServiceTest {

    @Mock private BillRepository billRepository;
    @Mock private BillMapper billMapper;
    @Mock private BillAddonRepository billAddonRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private BookingAddonRepository bookingAddonRepository;
    @Mock private BillAddonMapper billAddonMapper;
    @Mock private AuthService authService;

    @InjectMocks private com.hotelsa.backend.bill.service.BillService billService;

    private Hotel hotel;
    private Guest guest;
    private Room room;
    private Booking booking;
    private BillRequestDTO requestDto;
    private BillResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(11L);

        guest = Guest.builder().id(2L).fullName("Test Guest").hotelId(hotel.getId()).build();
        room = Room.builder().id(3L).hotelId(hotel.getId()).number("100").build();

        booking = Booking.builder()
                .id(5L)
                .hotelId(hotel.getId())
                .guest(guest)
                .room(room)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .status(BookingStatus.CONFIRMED)
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .build();

        requestDto = BillRequestDTO.builder()
                .notes("Factura prueba")
                .status(BillStatus.UNPAID)
                .build();

        responseDto = BillResponseDTO.builder()
                .id(20L)
                .bookingId(booking.getId())
                .notes("Factura prueba")
                .totalAmount(BigDecimal.valueOf(150))
                .build();

        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());
    }

    @Test
    void createBill_ShouldCreateBillWithAddonsAndReturnDto() {
        // Preparar booking
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        // Mapper from request -> entity (no id yet)
        Bill mapped = Bill.builder().notes(requestDto.getNotes()).status(requestDto.getStatus()).build();
        when(billMapper.fromRequestDto(requestDto)).thenReturn(mapped);

        // Primer save de factura (antes de addons) devuelve factura con id
        Bill savedInitial = Bill.builder().id(30L).hotelId(hotel.getId()).totalAmount(BigDecimal.ZERO).build();
        when(billRepository.save(any(Bill.class))).thenReturn(savedInitial).thenReturn(savedInitial);

        // Simular booking addons existentes
        com.hotelsa.backend.addon.model.Addon addon = new com.hotelsa.backend.addon.model.Addon();
        addon.setId(7L);
        addon.setName("Desayuno");
        addon.setDescription("Buffet");
        addon.setPrice(50);

        BookingAddon ba = BookingAddon.builder()
                .id(new BookingAddonId(booking.getId(), addon.getId()))
                .booking(booking)
                .addon(addon)
                .quantity(2)
                .hotelId(hotel.getId())
                .build();

        when(bookingAddonRepository.findByIdBookingIdAndHotelId(booking.getId(), hotel.getId()))
                .thenReturn(List.of(ba));

        // Simular guardado de BillAddon
        BillAddon persisted = BillAddon.builder()
                .id(new BillAddonId(savedInitial.getId(), addon.getId()))
                .bill(savedInitial)
                .addonId(addon.getId())
                .addonName(addon.getName())
                .unitPrice(BigDecimal.valueOf(addon.getPrice()))
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(addon.getPrice() * 2))
                .hotelId(hotel.getId())
                .build();

        when(billAddonRepository.save(any(BillAddon.class))).thenReturn(persisted);

        // Mapper from entity to response
        Bill finalBill = Bill.builder()
                .id(savedInitial.getId())
                .hotelId(hotel.getId())
                .booking(booking)
                .totalAmount(persisted.getTotalPrice())
                .addons(List.of(persisted))
                .build();

        when(billRepository.save(savedInitial)).thenReturn(finalBill);
        when(billRepository.findByIdWithRelations(savedInitial.getId())).thenReturn(Optional.of(finalBill));
        when(billMapper.fromEntity(any(Bill.class))).thenReturn(responseDto);

        BillResponseDTO res = billService.createBill(booking.getId(), requestDto);

        assertNotNull(res);
        assertEquals(responseDto.getId(), res.getId());
        verify(billRepository, atLeastOnce()).save(any(Bill.class));
        verify(billAddonRepository).save(any(BillAddon.class));
        verify(billRepository).findByIdWithRelations(savedInitial.getId());
    }

    @Test
    void createBill_ShouldThrowWhenBookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.hotelsa.backend.booking.exception.BookingNotFoundException.class,
                () -> billService.createBill(999L, requestDto));
    }

    @Test
    void findById_ShouldReturnDtoWhenBelongsToHotel() {
        Bill entity = Bill.builder().id(40L).hotelId(hotel.getId()).totalAmount(BigDecimal.valueOf(100)).build();
        when(billRepository.findByIdWithRelations(40L)).thenReturn(Optional.of(entity));
        when(billMapper.fromEntity(entity)).thenReturn(responseDto);

        BillResponseDTO res = billService.findById(40L);

        assertNotNull(res);
        assertEquals(responseDto.getId(), res.getId());
    }

    @Test
    void findById_ShouldThrowWhenBillNotBelongToHotel() {
        Bill entity = Bill.builder().id(50L).hotelId(999L).totalAmount(BigDecimal.valueOf(20)).build();
        when(billRepository.findByIdWithRelations(50L)).thenReturn(Optional.of(entity));

        assertThrows(com.hotelsa.backend.bill.exception.BillNotFoundException.class,
                () -> billService.findById(50L));
    }

    @Test
    void findAll_ShouldReturnMappedList() {
        Bill b1 = Bill.builder().id(1L).hotelId(hotel.getId()).totalAmount(BigDecimal.ONE).build();
        Bill b2 = Bill.builder().id(2L).hotelId(hotel.getId()).totalAmount(BigDecimal.TEN).build();

        when(billRepository.findAllWithRelations()).thenReturn(List.of(b1, b2));
        when(billMapper.fromEntity(b1)).thenReturn(BillResponseDTO.builder().id(1L).totalAmount(BigDecimal.ONE).build());
        when(billMapper.fromEntity(b2)).thenReturn(BillResponseDTO.builder().id(2L).totalAmount(BigDecimal.TEN).build());

        List<BillResponseDTO> res = billService.findAll();

        assertNotNull(res);
        assertEquals(2, res.size());
    }

    @Test
    void updateStatus_ShouldUpdateAndReturnDto_WhenAuthorized() {
        Bill entity = Bill.builder().id(60L).hotelId(hotel.getId()).status(BillStatus.UNPAID).build();
        when(billRepository.findById(60L)).thenReturn(Optional.of(entity));
        when(billRepository.save(entity)).thenReturn(entity);
        when(billMapper.fromEntity(entity)).thenReturn(responseDto);

        BillResponseDTO res = billService.updateStatus(60L, BillStatus.PAID);

        assertNotNull(res);
        verify(billRepository).save(entity);
    }

    @Test
    void delete_ShouldSoftDeleteBillWhenAuthorized() {
        Bill entity = Bill.builder().id(70L).hotelId(hotel.getId()).deleted(false).build();
        when(billRepository.findById(70L)).thenReturn(Optional.of(entity));
        when(billRepository.save(entity)).thenReturn(entity);

        billService.delete(70L);

        assertTrue(entity.isDeleted());
        verify(billRepository).save(entity);
    }
}
