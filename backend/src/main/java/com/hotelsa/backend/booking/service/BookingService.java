package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.exception.BookingNotFoundException;
import com.hotelsa.backend.booking.exception.BookingAddonNotFoundException;
import com.hotelsa.backend.booking.mapper.BookingMapper;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.booking.repository.BookingRepository;
import com.hotelsa.backend.bookingaddon.entity.BookingAddon;
import com.hotelsa.backend.bookingaddon.entity.BookingAddonId;
import com.hotelsa.backend.bookingaddon.repository.BookingAddonRepository;
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
import com.hotelsa.backend.addon.model.Addon;
import com.hotelsa.backend.addon.repository.AddonRepository;
import com.hotelsa.backend.addon.mapper.AddonMapper;
import com.hotelsa.backend.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    private final AddonRepository addonRepository;
    private final BookingAddonRepository bookingAddonRepository;

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

        // Cambiar estado de la habitación a OCCUPIED
        room.setStatus(com.hotelsa.backend.room.enums.RoomStatus.OCCUPIED);
        roomRepository.save(room);

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

        // Si se cancela la reserva, liberar la habitación
        if (dto.getStatus() == BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.CANCELLED) {
            room.setStatus(com.hotelsa.backend.room.enums.RoomStatus.AVAILABLE);
            roomRepository.save(room);
            log.debug("🔓 Habitación {} liberada por cancelación de booking {}", room.getNumber(), id);
        }
        // Si se reactiva una reserva cancelada, ocupar la habitación
        else if (booking.getStatus() == BookingStatus.CANCELLED && dto.getStatus() != BookingStatus.CANCELLED) {
            room.setStatus(com.hotelsa.backend.room.enums.RoomStatus.OCCUPIED);
            roomRepository.save(room);
            log.debug("🔒 Habitación {} ocupada por reactivación de booking {}", room.getNumber(), id);
        }

        booking.setStatus(dto.getStatus());
        booking.setCreatedBy(dto.getCreatedBy());
        booking.setBookingLeadTime(dto.getBookingLeadTime());
        booking.setNotes(dto.getNotes());

        Booking updatedBooking = bookingRepository.save(booking);
        log.debug("✅ Updated booking {} for hotel {}", updatedBooking.getId(), booking.getHotelId());

        BookingResponseDTO response = bookingMapper.fromEntity(updatedBooking);
        // Asegurar que la respuesta incluya los addons con quantity y subtotal
        response.setAddons(getAddonsFromBooking(id));
        return response;
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        return bookingMapper.fromEntity(booking, true);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> findAll() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(b -> bookingMapper.fromEntity(b, false)).toList();
    }

    @AdminOnly
    @Transactional
    public void delete(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        // Liberar la habitación al eliminar la reserva
        Room room = booking.getRoom();
        if (room != null) {
            room.setStatus(com.hotelsa.backend.room.enums.RoomStatus.AVAILABLE);
            roomRepository.save(room);
            log.debug("🔓 Habitación {} liberada por eliminación de booking {}", room.getNumber(), id);
        }

        booking.setDeleted(true);
        bookingRepository.save(booking);
        log.debug("🗑️ Soft deleted booking {} for hotel {}", booking.getId(), booking.getHotelId());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsCheckingOutToday() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckOutDate(today);
        return bookings.stream().map(b -> bookingMapper.fromEntity(b, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getExpiredBookings() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckOutDateBefore(today);
        return bookings.stream().map(b -> bookingMapper.fromEntity(b, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsStartingToday() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckInDate(today);
        return bookings.stream().map(b -> bookingMapper.fromEntity(b, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByGuest(Long guestId) {
        List<Booking> bookings = bookingRepository.findByGuestId(guestId);
        return bookings.stream().map(b -> bookingMapper.fromEntity(b, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByRoomAndStatus(Long roomId, BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByRoomIdAndStatus(roomId, status);
        return bookings.stream().map(b -> bookingMapper.fromEntity(b, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsBetween(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        List<Booking> bookings = bookingRepository.findByCheckInDateBetween(start, end);
        return bookings.stream().map(b -> bookingMapper.fromEntity(b, false)).toList();
    }

    @Transactional
    public BookingResponseDTO addAddonsToBooking(Long bookingId, List<com.hotelsa.backend.booking.dto.BookingAddonRequest> addonRequests) {
        Long currentHotelId = getCurrentHotelId();
        log.debug("Agregar addons a booking {} (currentHotelId={}) addonRequests={}", bookingId, currentHotelId, addonRequests);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        // Seguridad adicional: asegurarnos de que la reserva pertenece al tenant actual
        if (currentHotelId != null && !currentHotelId.equals(booking.getHotelId())) {
            throw new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel");
        }

        // Cargar todos los addons solicitados
        List<Long> ids = addonRequests.stream().map(r -> r.getAddonId()).toList();
        List<Addon> addons = addonRepository.findByIdIn(ids);
        if (addons.size() != ids.size()) {
            throw new com.hotelsa.backend.addon.exception.AddonNotFoundException("Algunos addons no existen");
        }

        // Procesar cada request (crear o actualizar cantidad)
        for (com.hotelsa.backend.booking.dto.BookingAddonRequest req : addonRequests) {
            Long addonId = req.getAddonId();
            Integer qty = req.getQuantity() == null || req.getQuantity() < 1 ? 1 : req.getQuantity();

            Addon addon = addons.stream().filter(a -> a.getId().equals(addonId)).findFirst().orElse(null);
            if (addon == null) {
                throw new com.hotelsa.backend.addon.exception.AddonNotFoundException("Addon no encontrado: " + addonId);
            }

            if (!java.util.Objects.equals(addon.getHotelId(), currentHotelId)) {
                throw new BadRequestException("Addon no pertenece al mismo hotel");
            }

            BookingAddonId id = new BookingAddonId(bookingId, addonId);

            // Si la relación existe, actualizar cantidad; si no, crear
            if (bookingAddonRepository.existsByIdBookingIdAndIdAddonIdAndHotelId(bookingId, addonId, currentHotelId)) {
                BookingAddon existing = bookingAddonRepository.findByIdAndHotelId(id, currentHotelId)
                        .orElseThrow(() -> new BookingAddonNotFoundException("Relación booking-addon no encontrada"));

                if (existing.isDeleted()) {
                    existing.setDeleted(false);
                    existing.setQuantity(qty);
                } else {
                    existing.setQuantity((existing.getQuantity() == null ? 0 : existing.getQuantity()) + qty);
                }

                bookingAddonRepository.save(existing);
            } else {
                BookingAddon link = BookingAddon.builder()
                        .id(id)
                        .booking(booking)
                        .addon(addon)
                        .hotelId(currentHotelId)
                        .quantity(qty)
                        .build();

                bookingAddonRepository.save(link);
            }
        }

        // Construir la respuesta manualmente para incluir los addons filtrados correctamente
        BookingResponseDTO response = bookingMapper.fromEntity(booking);
        response.setAddons(getAddonsFromBooking(bookingId));

        return response;
    }

    @Transactional
    public void removeAddonFromBooking(Long bookingId, Long addonId) {
        Long currentHotelId = getCurrentHotelId();
        BookingAddonId id = new BookingAddonId(bookingId, addonId);

        BookingAddon link = bookingAddonRepository.findByIdAndHotelId(id, currentHotelId)
                .orElseThrow(() -> new BookingAddonNotFoundException("Relación booking-addon no encontrada"));

        link.setDeleted(true);
        bookingAddonRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<com.hotelsa.backend.booking.dto.BookingAddonResponse> getAddonsFromBooking(Long bookingId) {
        Long currentHotelId = getCurrentHotelId();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        // Filtrar por hotelId explícitamente
        List<BookingAddon> links = bookingAddonRepository.findByIdBookingIdAndHotelId(bookingId, currentHotelId);
        return links.stream().map(l -> {
            var addon = l.getAddon();
            int qty = l.getQuantity() == null ? 1 : l.getQuantity();
            Integer price = addon.getPrice() == null ? 0 : addon.getPrice();
            return com.hotelsa.backend.booking.dto.BookingAddonResponse.builder()
                    .id(addon.getId())
                    .name(addon.getName())
                    .description(addon.getDescription())
                    .price(price)
                    .createdAt(addon.getCreatedAt())
                    .quantity(qty)
                    .subtotal(price * qty)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public BookingResponseDTO updateAddonQuantity(Long bookingId, Long addonId, Integer newQuantity) {
        Long currentHotelId = getCurrentHotelId();
        log.debug("Actualizar cantidad del addon {} en booking {} a quantity={} (currentHotelId={})",
                addonId, bookingId, newQuantity, currentHotelId);

        // Validar que la reserva existe y pertenece al tenant actual
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        if (currentHotelId != null && !currentHotelId.equals(booking.getHotelId())) {
            throw new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel");
        }

        // Buscar la relación BookingAddon
        BookingAddonId id = new BookingAddonId(bookingId, addonId);
        BookingAddon link = bookingAddonRepository.findByIdAndHotelId(id, currentHotelId)
                .orElseThrow(() -> new BookingAddonNotFoundException("Relación booking-addon no encontrada"));

        // Validar que no esté soft-deleted
        if (link.isDeleted()) {
            throw new BookingAddonNotFoundException("El addon ya fue eliminado de esta reserva");
        }

        // Actualizar cantidad (reemplazo, no suma)
        link.setQuantity(newQuantity);
        bookingAddonRepository.save(link);

        log.debug("✅ Actualizada cantidad del addon {} en booking {} a {}", addonId, bookingId, newQuantity);

        // Construir respuesta con addons actualizados
        BookingResponseDTO response = bookingMapper.fromEntity(booking);
        response.setAddons(getAddonsFromBooking(bookingId));

        return response;
    }

    @AdminOnly
    @Transactional
    public BookingResponseDTO cancelBooking(Long id) {
        Long currentHotelId = getCurrentHotelId();
        log.debug("Intentando cancelar booking {} (currentHotelId={})", id, currentHotelId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        // Seguridad adicional: asegurarnos de que la reserva pertenece al tenant actual
        if (currentHotelId != null && !currentHotelId.equals(booking.getHotelId())) {
            throw new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel");
        }

        // Liberar la habitación al cancelar la reserva
        Room room = booking.getRoom();
        if (room != null && booking.getStatus() != BookingStatus.CANCELLED) {
            room.setStatus(com.hotelsa.backend.room.enums.RoomStatus.AVAILABLE);
            roomRepository.save(room);
            log.debug("🔓 Habitación {} liberada por cancelación de booking {}", room.getNumber(), id);
        }

        // Cambiar el estado a CANCELLED y persistir
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        log.debug("✅ Cancelada booking {} para hotel {}", saved.getId(), saved.getHotelId());

        BookingResponseDTO response = bookingMapper.fromEntity(saved);
        // Incluir los addons correctamente filtrados
        response.setAddons(getAddonsFromBooking(id));
        return response;
    }
}
