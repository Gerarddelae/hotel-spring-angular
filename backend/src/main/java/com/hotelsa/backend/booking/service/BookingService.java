package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.exception.BookingNotFoundException;
import com.hotelsa.backend.booking.mapper.BookingMapper;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.guest.exception.GuestNotFoundException;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.guest.repository.GuestRepository;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.exception.RoomNotFoundException;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;
    private final AuthService authService;

    private Long getCurrentHotelId() {
        return authService.getCurrentHotelId();
    }

    @AdminOnly
    @Transactional
    public BookingResponseDTO create(BookingRequestDTO dto) {
        Long hotelId = getCurrentHotelId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("Hotel no encontrado"));

        Guest guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new GuestNotFoundException("Huésped no encontrado o no pertenece a tu hotel"));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel"));

        // Validar que las fechas son coherentes
        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate()) ||
            dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
            throw new IllegalArgumentException("La fecha de check-out debe ser posterior a la fecha de check-in");
        }

        Booking booking = bookingMapper.fromRequestDto(dto);
        booking.setHotel(hotel);
        booking.setHotelId(hotelId);
        booking.setGuest(guest);
        booking.setRoom(room);

        Booking savedBooking = bookingRepository.save(booking);
        log.debug("✅ Created booking {} for guest {} in room {} at hotel {}",
                savedBooking.getId(), guest.getFullName(), room.getNumber(), hotelId);

        return bookingMapper.fromEntity(savedBooking);
    }

    @AdminOnly
    @Transactional
    public BookingResponseDTO update(Long id, BookingRequestDTO dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        Guest guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new GuestNotFoundException("Huésped no encontrado o no pertenece a tu hotel"));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel"));

        // Validar que las fechas son coherentes
        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate()) ||
            dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
            throw new IllegalArgumentException("La fecha de check-out debe ser posterior a la fecha de check-in");
        }

        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setStatus(dto.getStatus());
        booking.setCreatedBy(dto.getCreatedBy());
        booking.setBookingLeadTime(dto.getBookingLeadTime());
        booking.setNotes(dto.getNotes());

        Booking updatedBooking = bookingRepository.save(booking);
        log.debug("✅ Updated booking {} for hotel {}", updatedBooking.getId(), booking.getHotelId());

        return bookingMapper.fromEntity(updatedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        return bookingMapper.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> findAll() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(bookingMapper::fromEntity).toList();
    }

    @AdminOnly
    @Transactional
    public void delete(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        booking.setDeleted(true);
        bookingRepository.save(booking);
        log.debug("🗑️ Soft deleted booking {} for hotel {}", booking.getId(), booking.getHotelId());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsCheckingOutToday() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckOutDate(today);
        return bookings.stream().map(bookingMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getExpiredBookings() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckOutDateBefore(today);
        return bookings.stream().map(bookingMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsStartingToday() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckInDate(today);
        return bookings.stream().map(bookingMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByGuest(Long guestId) {
        List<Booking> bookings = bookingRepository.findByGuestId(guestId);
        return bookings.stream().map(bookingMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByRoomAndStatus(Long roomId, BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByRoomIdAndStatus(roomId, status);
        return bookings.stream().map(bookingMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsBetween(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        List<Booking> bookings = bookingRepository.findByCheckInDateBetween(start, end);
        return bookings.stream().map(bookingMapper::fromEntity).toList();
    }
}
