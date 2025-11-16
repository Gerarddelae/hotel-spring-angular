package com.hotelsa.backend.guest.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.guest.dto.GuestRequestDTO;
import com.hotelsa.backend.guest.dto.GuestResponseDTO;
import com.hotelsa.backend.guest.exception.GuestNotFoundException;
import com.hotelsa.backend.guest.mapper.GuestMapper;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.guest.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestService {

    private final HotelRepository hotelRepository;
    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;
    private final AuthService authService;

    private Long getCurrentHotelId() {
        return authService.getCurrentHotelId();
    }

    @AdminOnly
    @Transactional
    public GuestResponseDTO createGuest(GuestRequestDTO dto) {
        Long hotelId = getCurrentHotelId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("Hotel no encontrado"));

        if (guestRepository.existsByDocumentNumber(dto.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un huésped con ese número de documento");
        }

        if (guestRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un huésped con ese correo electrónico");
        }

        Guest guest = guestMapper.fromRequestDto(dto);
        guest.setHotel(hotel);
        guest.setHotelId(hotelId);

        Guest savedGuest = guestRepository.save(guest);
        log.debug("✅ Created guest {} for hotel {}", savedGuest.getFullName(), hotelId);

        return guestMapper.fromEntity(savedGuest);
    }

    @Transactional(readOnly = true)
    public GuestResponseDTO getGuestById(Long guestId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new GuestNotFoundException("Huésped no encontrado o no pertenece a tu hotel"));

        return guestMapper.fromEntity(guest);
    }

    @Transactional(readOnly = true)
    public List<GuestResponseDTO> getGuestsForCurrentHotel() {
        List<Guest> guests = guestRepository.findAll();
        return guests.stream().map(guestMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<GuestResponseDTO> searchGuestsByName(String fullName) {
        List<Guest> guests = guestRepository.findByFullNameContainingIgnoreCase(fullName);
        return guests.stream().map(guestMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public GuestResponseDTO getGuestByEmail(String email) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new GuestNotFoundException("Huésped no encontrado"));

        return guestMapper.fromEntity(guest);
    }

    @AdminOnly
    @Transactional
    public GuestResponseDTO updateGuest(Long guestId, GuestRequestDTO dto) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new GuestNotFoundException("Huésped no encontrado o no pertenece a tu hotel"));

        // Verificar si el número de documento ya existe en otro huésped
        if (!guest.getDocumentNumber().equals(dto.getDocumentNumber()) &&
                guestRepository.existsByDocumentNumber(dto.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un huésped con ese número de documento");
        }

        // Verificar si el correo ya existe en otro huésped
        if (!guest.getEmail().equals(dto.getEmail()) &&
                guestRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un huésped con ese correo electrónico");
        }

        guest.setFullName(dto.getFullName());
        guest.setDocumentType(dto.getDocumentType());
        guest.setDocumentNumber(dto.getDocumentNumber());
        guest.setEmail(dto.getEmail());
        guest.setPhone(dto.getPhone());
        guest.setAddress(dto.getAddress());
        guest.setPreviousCancellations(dto.getPreviousCancellations());
        guest.setTotalBookingsClient(dto.getTotalBookingsClient());

        Guest updatedGuest = guestRepository.save(guest);
        log.debug("✅ Updated guest {} for hotel {}", updatedGuest.getFullName(), guest.getHotelId());

        return guestMapper.fromEntity(updatedGuest);
    }

    @AdminOnly
    @Transactional
    public void deleteGuest(Long guestId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new GuestNotFoundException("Huésped no encontrado o no pertenece a tu hotel"));

        guest.setDeleted(true);
        guestRepository.save(guest);
        log.debug("🗑️ Soft deleted guest {} for hotel {}", guest.getFullName(), guest.getHotelId());
    }
}

