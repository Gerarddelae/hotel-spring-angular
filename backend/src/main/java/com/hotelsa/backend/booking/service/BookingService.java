package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.dto.BookingReplaceRequestDTO;
import com.hotelsa.backend.booking.exception.BookingAccessDeniedException;
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

        // Calcular total inicial (solo estancia)
        java.math.BigDecimal nights = java.math.BigDecimal.valueOf(java.time.temporal.ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate()));
        java.math.BigDecimal roomPrice = java.math.BigDecimal.valueOf(room.getPricePerNight());
        booking.setTotalAmount(nights.multiply(roomPrice));

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

        // Recalcular total (estadia + addons actuales)
        calculateAndSetBookingTotal(id);

        BookingResponseDTO response = bookingMapper.fromEntity(updatedBooking);
        // Asegurar que la respuesta incluya los addons con quantity y subtotal
        response.setAddons(getAddonsFromBooking(id));
        return response;
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO findById(Long id) {
        Long currentHotelId = getCurrentHotelId();
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada con ID: " + id));

        // Validación explícita de multi-tenancy
        if (currentHotelId != null && !currentHotelId.equals(booking.getHotelId())) {
            throw new BookingAccessDeniedException(id);
        }

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

        // Recalcular total y persistir
        calculateAndSetBookingTotal(bookingId);

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

    // Nuevo método: verifica si una habitación está disponible en un rango de fechas
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return isRoomAvailable(roomId, checkIn, checkOut, null);
    }

    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {
        Long hotelId = getCurrentHotelId();

        // Validar existencia de la habitación y pertenencia al tenant actual
        com.hotelsa.backend.room.model.Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new com.hotelsa.backend.room.exception.RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel"));

        if (hotelId != null && !hotelId.equals(room.getHotelId())) {
            throw new com.hotelsa.backend.room.exception.RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel");
        }

        // Si la habitación está en MAINTENANCE, siempre se considera no disponible
        if (room.getStatus() == com.hotelsa.backend.room.enums.RoomStatus.MAINTENANCE) {
            return false;
        }

        // Para OCCUPIED dejamos la decisión a la presencia de reservas activas (solapadas).
        // Si se proporciona excludeBookingId, excluimos esa reserva de la comprobación (edición de reserva)
        boolean existsOverlap;
        if (excludeBookingId != null) {
            existsOverlap = bookingRepository.existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelIdAndIdNot(
                    roomId,
                    BookingStatus.CANCELLED,
                    checkOut,
                    checkIn,
                    hotelId,
                    excludeBookingId
            );
        } else {
            existsOverlap = bookingRepository.existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
                    roomId,
                    BookingStatus.CANCELLED,
                    checkOut,
                    checkIn,
                    hotelId
            );
        }
        return !existsOverlap;
    }

    @AdminOnly
    @Transactional
    public BookingResponseDTO cancelBooking(Long id) {
        Long currentHotelId = getCurrentHotelId();
        log.debug("Intentando cancelar booking {} (currentHotelId={})", id, currentHotelId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        // Seguridad adicional: asegurarnos de que la reserva pertenece al tenant current
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

    @Transactional
    public BookingResponseDTO replaceAddonsForBooking(Long bookingId, List<com.hotelsa.backend.booking.dto.BookingAddonRequest> addonRequests) {
        Long currentHotelId = getCurrentHotelId();
        log.debug("Reemplazando addons para booking {} (currentHotelId={}) addonRequests={}", bookingId, currentHotelId, addonRequests);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel"));

        if (currentHotelId != null && !currentHotelId.equals(booking.getHotelId())) {
            throw new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel");
        }

        // Cargar links actuales (no deleted) para la reserva y el hotel
        List<com.hotelsa.backend.bookingaddon.entity.BookingAddon> existingLinks = bookingAddonRepository.findByIdBookingIdAndHotelId(bookingId, currentHotelId);

        // Mapear requests por addonId para acceso rápido
        java.util.Map<Long, Integer> requestedMap = addonRequests == null ? java.util.Map.of() : addonRequests.stream()
                .collect(java.util.stream.Collectors.toMap(com.hotelsa.backend.booking.dto.BookingAddonRequest::getAddonId,
                        r -> r.getQuantity() == null ? 1 : r.getQuantity(), (a, b) -> b));

        // Marcar como deleted los links existentes que no están en la nueva lista; actualizar cantidades de los que sí
        for (com.hotelsa.backend.bookingaddon.entity.BookingAddon link : existingLinks) {
            Long addonId = link.getId().getAddonId();
            if (requestedMap.containsKey(addonId)) {
                link.setDeleted(false);
                link.setQuantity(requestedMap.get(addonId));
                bookingAddonRepository.save(link);
                requestedMap.remove(addonId);
            } else {
                // marcar como deleted (soft-delete)
                link.setDeleted(true);
                bookingAddonRepository.save(link);
            }
        }

        // Crear nuevas relaciones para los addonIds restantes en requestedMap
        if (!requestedMap.isEmpty()) {
            // cargar addons por ids
            List<Long> toCreateIds = new java.util.ArrayList<>(requestedMap.keySet());
            List<Addon> addons = addonRepository.findByIdIn(toCreateIds);
            if (addons.size() != toCreateIds.size()) {
                throw new com.hotelsa.backend.addon.exception.AddonNotFoundException("Algunos addons no existen");
            }

            for (Addon addon : addons) {
                Long addonId = addon.getId();
                Integer qty = requestedMap.get(addonId);
                com.hotelsa.backend.bookingaddon.entity.BookingAddonId id = new com.hotelsa.backend.bookingaddon.entity.BookingAddonId(bookingId, addonId);
                com.hotelsa.backend.bookingaddon.entity.BookingAddon newLink = com.hotelsa.backend.bookingaddon.entity.BookingAddon.builder()
                        .id(id)
                        .booking(booking)
                        .addon(addon)
                        .hotelId(currentHotelId)
                        .quantity(qty == null ? 1 : qty)
                        .deleted(false)
                        .build();
                bookingAddonRepository.save(newLink);
            }
        }

        // Recalcular total y persistir
        calculateAndSetBookingTotal(bookingId);

        BookingResponseDTO response = bookingMapper.fromEntity(booking);
        response.setAddons(getAddonsFromBooking(bookingId));
        return response;
    }

    @Transactional
    public BookingResponseDTO replaceBooking(Long bookingId, com.hotelsa.backend.booking.dto.BookingReplaceRequestDTO dto) {
        Long currentHotelId = getCurrentHotelId();
        log.debug("Reemplazando booking {} con payload {} (currentHotelId={})", bookingId, dto, currentHotelId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada"));

        if (currentHotelId != null && !currentHotelId.equals(booking.getHotelId())) {
            throw new BookingNotFoundException("Reserva no encontrada o no pertenece a tu hotel");
        }

        // Validaciones básicos
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null || !dto.getCheckInDate().isBefore(dto.getCheckOutDate())) {
            throw new IllegalArgumentException("Fechas inválidas: checkIn debe ser anterior a checkOut");
        }

        // Validar room existe y pertenece al hotel
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada"));
        if (currentHotelId != null && !currentHotelId.equals(room.getHotelId())) {
            throw new RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel");
        }

        // Verificar disponibilidad del room para el nuevo rango, excluyendo la propia reserva
        boolean conflict = bookingRepository.existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelIdAndIdNot(
                dto.getRoomId(), BookingStatus.CANCELLED, dto.getCheckOutDate(), dto.getCheckInDate(), currentHotelId, bookingId
        );
        if (conflict) {
            throw new com.hotelsa.backend.common.exception.BadRequestException("La habitación no está disponible en el rango solicitado");
        }

        // Actualizar campos principales
        Guest guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new com.hotelsa.backend.guest.exception.GuestNotFoundException("Huésped no encontrado"));
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setStatus(dto.getStatus() == null ? booking.getStatus() : dto.getStatus());
        booking.setNotes(dto.getNotes());

        Booking saved = bookingRepository.save(booking);

        // Reemplazar addons usando la lógica ya existente (reactiva, actualiza, borra)
        replaceAddonsForBooking(bookingId, dto.getAddons());

        // Recalcular total y persistir
        calculateAndSetBookingTotal(bookingId);

        BookingResponseDTO response = bookingMapper.fromEntity(saved);
        response.setAddons(getAddonsFromBooking(bookingId));
        return response;
    }

    // Helper para calcular y persistir total de booking = noches * precioNoche + sum(addons)
    @Transactional
    protected void calculateAndSetBookingTotal(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada"));

        Room room = booking.getRoom();
        if (room == null) {
            room = roomRepository.findById(booking.getRoom().getId()).orElse(null);
        }

        java.math.BigDecimal nights = java.math.BigDecimal.valueOf(java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate()));
        java.math.BigDecimal roomPrice = room == null ? java.math.BigDecimal.ZERO : java.math.BigDecimal.valueOf(room.getPricePerNight());
        java.math.BigDecimal staySubtotal = nights.multiply(roomPrice);

        Long currentHotelId = getCurrentHotelId();
        List<com.hotelsa.backend.bookingaddon.entity.BookingAddon> links = bookingAddonRepository.findByIdBookingIdAndHotelId(bookingId, currentHotelId);

        java.math.BigDecimal addonsTotal = links.stream()
                .filter(l -> !l.isDeleted())
                .map(l -> {
                    int qty = l.getQuantity() == null ? 1 : l.getQuantity();
                    int price = l.getAddon().getPrice() == null ? 0 : l.getAddon().getPrice();
                    return java.math.BigDecimal.valueOf(price).multiply(java.math.BigDecimal.valueOf(qty));
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        booking.setTotalAmount(staySubtotal.add(addonsTotal));
        bookingRepository.save(booking);
    }

    // Dashboard methods
    @Transactional(readOnly = true)
    public com.hotelsa.backend.booking.dto.BookingStatusCountDTO countByStatus() {
        int pending = bookingRepository.countByStatus(BookingStatus.PENDING);
        int confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        int checkedIn = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);
        int total = pending + confirmed + checkedIn;

        return new com.hotelsa.backend.booking.dto.BookingStatusCountDTO(total, pending, confirmed, checkedIn);
    }

    @Transactional(readOnly = true)
    public com.hotelsa.backend.booking.dto.ActiveGuestsCountDTO getActiveGuestsCount() {
        java.time.LocalDate today = java.time.LocalDate.now();

        // Intentar con la query explícita primero
        int count = bookingRepository.countActiveGuestsTodayExplicit(today, BookingStatus.CHECKED_IN);

        log.debug("🔍 Active guests count for today {}: {}", today, count);

        return new com.hotelsa.backend.booking.dto.ActiveGuestsCountDTO(count);
    }
}

