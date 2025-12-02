package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.addon.repository.AddonRepository;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.mapper.BookingMapper;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.guest.repository.GuestRepository;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para verificar la actualización de estadísticas del huésped:
 * - totalBookingsClient: se incrementa al crear una reserva
 * - previousCancellations: se incrementa al cancelar una reserva
 */
class BookingServiceGuestStatsTest {

    private BookingRepository bookingRepository;
    private GuestRepository guestRepository;
    private RoomRepository roomRepository;
    private HotelRepository hotelRepository;
    private BookingMapper bookingMapper;
    private AuthService authService;
    private AddonRepository addonRepository;
    private BookingAddonRepository bookingAddonRepository;
    private BookingService bookingService;

    private static final Long HOTEL_ID = 10L;
    private static final Long GUEST_ID = 1L;
    private static final Long ROOM_ID = 100L;
    private static final Long BOOKING_ID = 1000L;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        guestRepository = Mockito.mock(GuestRepository.class);
        roomRepository = Mockito.mock(RoomRepository.class);
        hotelRepository = Mockito.mock(HotelRepository.class);
        bookingMapper = Mockito.mock(BookingMapper.class);
        authService = Mockito.mock(AuthService.class);
        addonRepository = Mockito.mock(AddonRepository.class);
        bookingAddonRepository = Mockito.mock(BookingAddonRepository.class);

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

        when(authService.getCurrentHotelId()).thenReturn(HOTEL_ID);
    }

    private Hotel createTestHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(HOTEL_ID);
        hotel.setName("Test Hotel");
        return hotel;
    }

    private Guest createTestGuest(Integer totalBookings, Integer previousCancellations) {
        Guest guest = new Guest();
        guest.setId(GUEST_ID);
        guest.setHotelId(HOTEL_ID);
        guest.setFullName("John Doe");
        guest.setEmail("john@test.com");
        guest.setPhone("123456789");
        guest.setDocumentType("DNI");
        guest.setDocumentNumber("12345678");
        guest.setAddress("Test Address");
        guest.setTotalBookingsClient(totalBookings);
        guest.setPreviousCancellations(previousCancellations);
        return guest;
    }

    private Room createTestRoom() {
        Room room = new Room();
        room.setId(ROOM_ID);
        room.setHotelId(HOTEL_ID);
        room.setNumber("101");
        room.setPricePerNight(100.0);
        room.setStatus(RoomStatus.AVAILABLE);
        return room;
    }

    private Booking createTestBooking(Guest guest, Room room, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setHotelId(HOTEL_ID);
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(LocalDate.of(2025, 12, 1));
        booking.setCheckOutDate(LocalDate.of(2025, 12, 5));
        booking.setStatus(status);
        return booking;
    }

    @Nested
    @DisplayName("Tests para totalBookingsClient al crear reserva")
    class CreateBookingTests {

        @Test
        @DisplayName("Debe incrementar totalBookingsClient cuando se crea una nueva reserva")
        void shouldIncrementTotalBookingsClientOnCreate() {
            // Arrange
            Guest guest = createTestGuest(5, 0);
            Room room = createTestRoom();
            Hotel hotel = createTestHotel();

            BookingRequestDTO dto = new BookingRequestDTO();
            dto.setGuestId(GUEST_ID);
            dto.setRoomId(ROOM_ID);
            dto.setCheckInDate(LocalDate.of(2025, 12, 1));
            dto.setCheckOutDate(LocalDate.of(2025, 12, 5));
            dto.setStatus(BookingStatus.CONFIRMED);

            Booking savedBooking = createTestBooking(guest, room, BookingStatus.CONFIRMED);

            when(hotelRepository.findById(HOTEL_ID)).thenReturn(Optional.of(hotel));
            when(guestRepository.findById(GUEST_ID)).thenReturn(Optional.of(guest));
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(bookingMapper.fromRequestDto(any())).thenReturn(new Booking());
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(guestRepository.save(any(Guest.class))).thenReturn(guest);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());

            // Act
            bookingService.create(dto);

            // Assert
            ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
            verify(guestRepository).save(guestCaptor.capture());
            Guest savedGuest = guestCaptor.getValue();
            
            assertEquals(6, savedGuest.getTotalBookingsClient(), 
                    "totalBookingsClient debería incrementarse de 5 a 6");
        }

        @Test
        @DisplayName("Debe manejar totalBookingsClient null como 0 al crear reserva")
        void shouldHandleNullTotalBookingsClientOnCreate() {
            // Arrange
            Guest guest = createTestGuest(null, 0);
            Room room = createTestRoom();
            Hotel hotel = createTestHotel();

            BookingRequestDTO dto = new BookingRequestDTO();
            dto.setGuestId(GUEST_ID);
            dto.setRoomId(ROOM_ID);
            dto.setCheckInDate(LocalDate.of(2025, 12, 1));
            dto.setCheckOutDate(LocalDate.of(2025, 12, 5));
            dto.setStatus(BookingStatus.CONFIRMED);

            Booking savedBooking = createTestBooking(guest, room, BookingStatus.CONFIRMED);

            when(hotelRepository.findById(HOTEL_ID)).thenReturn(Optional.of(hotel));
            when(guestRepository.findById(GUEST_ID)).thenReturn(Optional.of(guest));
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(bookingMapper.fromRequestDto(any())).thenReturn(new Booking());
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(guestRepository.save(any(Guest.class))).thenReturn(guest);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());

            // Act
            bookingService.create(dto);

            // Assert
            ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
            verify(guestRepository).save(guestCaptor.capture());
            Guest savedGuest = guestCaptor.getValue();
            
            assertEquals(1, savedGuest.getTotalBookingsClient(), 
                    "totalBookingsClient debería ser 1 cuando era null");
        }

        @Test
        @DisplayName("Debe incrementar totalBookingsClient cuando huésped no tenía reservas previas")
        void shouldIncrementFromZeroTotalBookingsClient() {
            // Arrange
            Guest guest = createTestGuest(0, 0);
            Room room = createTestRoom();
            Hotel hotel = createTestHotel();

            BookingRequestDTO dto = new BookingRequestDTO();
            dto.setGuestId(GUEST_ID);
            dto.setRoomId(ROOM_ID);
            dto.setCheckInDate(LocalDate.of(2025, 12, 1));
            dto.setCheckOutDate(LocalDate.of(2025, 12, 5));
            dto.setStatus(BookingStatus.CONFIRMED);

            Booking savedBooking = createTestBooking(guest, room, BookingStatus.CONFIRMED);

            when(hotelRepository.findById(HOTEL_ID)).thenReturn(Optional.of(hotel));
            when(guestRepository.findById(GUEST_ID)).thenReturn(Optional.of(guest));
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(bookingMapper.fromRequestDto(any())).thenReturn(new Booking());
            when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
            when(guestRepository.save(any(Guest.class))).thenReturn(guest);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());

            // Act
            bookingService.create(dto);

            // Assert
            ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
            verify(guestRepository).save(guestCaptor.capture());
            Guest savedGuest = guestCaptor.getValue();
            
            assertEquals(1, savedGuest.getTotalBookingsClient(), 
                    "totalBookingsClient debería incrementarse de 0 a 1");
        }
    }

    @Nested
    @DisplayName("Tests para previousCancellations al cancelar reserva con cancelBooking()")
    class CancelBookingTests {

        @Test
        @DisplayName("Debe incrementar previousCancellations cuando se cancela una reserva activa")
        void shouldIncrementPreviousCancellationsOnCancel() {
            // Arrange
            Guest guest = createTestGuest(5, 2);
            Room room = createTestRoom();
            Booking booking = createTestBooking(guest, room, BookingStatus.CONFIRMED);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(roomRepository.save(any(Room.class))).thenReturn(room);
            when(guestRepository.save(any(Guest.class))).thenReturn(guest);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());
            when(bookingAddonRepository.findByIdBookingIdAndHotelId(anyLong(), anyLong()))
                    .thenReturn(java.util.Collections.emptyList());

            // Act
            bookingService.cancelBooking(BOOKING_ID);

            // Assert
            ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
            verify(guestRepository).save(guestCaptor.capture());
            Guest savedGuest = guestCaptor.getValue();
            
            assertEquals(3, savedGuest.getPreviousCancellations(), 
                    "previousCancellations debería incrementarse de 2 a 3");
        }

        @Test
        @DisplayName("Debe manejar previousCancellations null como 0 al cancelar")
        void shouldHandleNullPreviousCancellationsOnCancel() {
            // Arrange
            Guest guest = createTestGuest(5, null);
            Room room = createTestRoom();
            Booking booking = createTestBooking(guest, room, BookingStatus.CONFIRMED);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(roomRepository.save(any(Room.class))).thenReturn(room);
            when(guestRepository.save(any(Guest.class))).thenReturn(guest);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());
            when(bookingAddonRepository.findByIdBookingIdAndHotelId(anyLong(), anyLong()))
                    .thenReturn(java.util.Collections.emptyList());

            // Act
            bookingService.cancelBooking(BOOKING_ID);

            // Assert
            ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
            verify(guestRepository).save(guestCaptor.capture());
            Guest savedGuest = guestCaptor.getValue();
            
            assertEquals(1, savedGuest.getPreviousCancellations(), 
                    "previousCancellations debería ser 1 cuando era null");
        }

        @Test
        @DisplayName("No debe incrementar previousCancellations si la reserva ya estaba cancelada")
        void shouldNotIncrementPreviousCancellationsIfAlreadyCancelled() {
            // Arrange
            Guest guest = createTestGuest(5, 2);
            Room room = createTestRoom();
            Booking booking = createTestBooking(guest, room, BookingStatus.CANCELLED);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());
            when(bookingAddonRepository.findByIdBookingIdAndHotelId(anyLong(), anyLong()))
                    .thenReturn(java.util.Collections.emptyList());

            // Act
            bookingService.cancelBooking(BOOKING_ID);

            // Assert
            verify(guestRepository, never()).save(any(Guest.class));
        }

        @Test
        @DisplayName("Debe liberar la habitación al cancelar")
        void shouldReleaseRoomOnCancel() {
            // Arrange
            Guest guest = createTestGuest(5, 2);
            Room room = createTestRoom();
            room.setStatus(RoomStatus.OCCUPIED);
            Booking booking = createTestBooking(guest, room, BookingStatus.CONFIRMED);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(roomRepository.save(any(Room.class))).thenReturn(room);
            when(guestRepository.save(any(Guest.class))).thenReturn(guest);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());
            when(bookingAddonRepository.findByIdBookingIdAndHotelId(anyLong(), anyLong()))
                    .thenReturn(java.util.Collections.emptyList());

            // Act
            bookingService.cancelBooking(BOOKING_ID);

            // Assert
            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(roomRepository).save(roomCaptor.capture());
            Room savedRoom = roomCaptor.getValue();
            
            assertEquals(RoomStatus.AVAILABLE, savedRoom.getStatus(), 
                    "La habitación debería estar AVAILABLE después de cancelar");
        }
    }

    @Nested
    @DisplayName("Tests para previousCancellations al cancelar mediante update()")
    class UpdateToCancelledTests {

        @Test
        @DisplayName("Debe incrementar previousCancellations cuando se actualiza estado a CANCELLED")
        void shouldIncrementPreviousCancellationsOnUpdateToCancelled() {
            // Arrange
            Guest guest = createTestGuest(5, 1);
            Room room = createTestRoom();
            Booking booking = createTestBooking(guest, room, BookingStatus.CONFIRMED);

            BookingRequestDTO dto = new BookingRequestDTO();
            dto.setGuestId(GUEST_ID);
            dto.setRoomId(ROOM_ID);
            dto.setCheckInDate(LocalDate.of(2025, 12, 1));
            dto.setCheckOutDate(LocalDate.of(2025, 12, 5));
            dto.setStatus(BookingStatus.CANCELLED);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(guestRepository.findById(GUEST_ID)).thenReturn(Optional.of(guest));
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(roomRepository.save(any(Room.class))).thenReturn(room);
            when(guestRepository.save(any(Guest.class))).thenReturn(guest);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());
            when(bookingAddonRepository.findByIdBookingIdAndHotelId(anyLong(), anyLong()))
                    .thenReturn(java.util.Collections.emptyList());

            // Act
            bookingService.update(BOOKING_ID, dto);

            // Assert
            ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
            verify(guestRepository).save(guestCaptor.capture());
            Guest savedGuest = guestCaptor.getValue();
            
            assertEquals(2, savedGuest.getPreviousCancellations(), 
                    "previousCancellations debería incrementarse de 1 a 2");
        }

        @Test
        @DisplayName("No debe incrementar previousCancellations si el estado no cambia a CANCELLED")
        void shouldNotIncrementIfStatusNotChangedToCancelled() {
            // Arrange
            Guest guest = createTestGuest(5, 1);
            Room room = createTestRoom();
            Booking booking = createTestBooking(guest, room, BookingStatus.PENDING);

            BookingRequestDTO dto = new BookingRequestDTO();
            dto.setGuestId(GUEST_ID);
            dto.setRoomId(ROOM_ID);
            dto.setCheckInDate(LocalDate.of(2025, 12, 1));
            dto.setCheckOutDate(LocalDate.of(2025, 12, 5));
            dto.setStatus(BookingStatus.CONFIRMED); // No es CANCELLED

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(guestRepository.findById(GUEST_ID)).thenReturn(Optional.of(guest));
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());
            when(bookingAddonRepository.findByIdBookingIdAndHotelId(anyLong(), anyLong()))
                    .thenReturn(java.util.Collections.emptyList());

            // Act
            bookingService.update(BOOKING_ID, dto);

            // Assert - no se debe llamar a save del guest
            verify(guestRepository, never()).save(any(Guest.class));
        }

        @Test
        @DisplayName("No debe incrementar previousCancellations si ya estaba CANCELLED")
        void shouldNotIncrementIfAlreadyCancelled() {
            // Arrange
            Guest guest = createTestGuest(5, 3);
            Room room = createTestRoom();
            Booking booking = createTestBooking(guest, room, BookingStatus.CANCELLED);

            BookingRequestDTO dto = new BookingRequestDTO();
            dto.setGuestId(GUEST_ID);
            dto.setRoomId(ROOM_ID);
            dto.setCheckInDate(LocalDate.of(2025, 12, 1));
            dto.setCheckOutDate(LocalDate.of(2025, 12, 5));
            dto.setStatus(BookingStatus.CANCELLED);

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            when(guestRepository.findById(GUEST_ID)).thenReturn(Optional.of(guest));
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());
            when(bookingAddonRepository.findByIdBookingIdAndHotelId(anyLong(), anyLong()))
                    .thenReturn(java.util.Collections.emptyList());

            // Act
            bookingService.update(BOOKING_ID, dto);

            // Assert - no se debe llamar a save del guest porque ya estaba cancelada
            verify(guestRepository, never()).save(any(Guest.class));
        }
    }

    @Nested
    @DisplayName("Tests de integración de estadísticas")
    class IntegrationTests {

        @Test
        @DisplayName("Múltiples reservas del mismo huésped deben incrementar contador correctamente")
        void multipleBooksingShouldIncrementCorrectly() {
            // Arrange
            Guest guest = createTestGuest(0, 0);
            Room room = createTestRoom();
            Hotel hotel = createTestHotel();

            when(hotelRepository.findById(HOTEL_ID)).thenReturn(Optional.of(hotel));
            when(guestRepository.findById(GUEST_ID)).thenReturn(Optional.of(guest));
            when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
            when(bookingMapper.fromRequestDto(any())).thenReturn(new Booking());
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setId(System.currentTimeMillis());
                return b;
            });
            when(guestRepository.save(any(Guest.class))).thenAnswer(inv -> inv.getArgument(0));
            when(bookingMapper.fromEntity(any(Booking.class))).thenReturn(new BookingResponseDTO());

            BookingRequestDTO dto = new BookingRequestDTO();
            dto.setGuestId(GUEST_ID);
            dto.setRoomId(ROOM_ID);
            dto.setCheckInDate(LocalDate.of(2025, 12, 1));
            dto.setCheckOutDate(LocalDate.of(2025, 12, 5));
            dto.setStatus(BookingStatus.CONFIRMED);

            // Act - Simular 3 reservas
            bookingService.create(dto);
            bookingService.create(dto);
            bookingService.create(dto);

            // Assert
            assertEquals(3, guest.getTotalBookingsClient(), 
                    "totalBookingsClient debería ser 3 después de 3 reservas");
        }
    }
}
