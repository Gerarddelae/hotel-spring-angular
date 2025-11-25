package com.hotelsa.backend.guest.controller;

import com.hotelsa.backend.guest.dto.GuestRequestDTO;
import com.hotelsa.backend.guest.dto.GuestResponseDTO;
import com.hotelsa.backend.guest.service.GuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    /**
     * Crea un nuevo huésped para el hotel del usuario autenticado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuestResponseDTO> createGuest(@Valid @RequestBody GuestRequestDTO dto) {
        GuestResponseDTO createdGuest = guestService.createGuest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGuest);
    }

    /**
     * Obtiene los detalles de un huésped específico (solo si pertenece al hotel del usuario).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<GuestResponseDTO> getGuestById(@PathVariable Long id) {
        GuestResponseDTO guest = guestService.getGuestById(id);
        return ResponseEntity.ok(guest);
    }

    /**
     * Lista todos los huéspedes del hotel del usuario autenticado.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<GuestResponseDTO>> getGuestsForCurrentHotel() {
        List<GuestResponseDTO> guests = guestService.getGuestsForCurrentHotel();
        return ResponseEntity.ok(guests);
    }

    /**
     * Busca huéspedes por nombre (búsqueda parcial, case-insensitive).
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<GuestResponseDTO>> searchGuests(
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(guestService.searchGuests(query));
    }

    /**
     * Obtiene un huésped por correo electrónico.
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<GuestResponseDTO> getGuestByEmail(@PathVariable String email) {
        GuestResponseDTO guest = guestService.getGuestByEmail(email);
        return ResponseEntity.ok(guest);
    }

    /**
     * Actualiza un huésped (solo si pertenece al hotel del usuario).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuestResponseDTO> updateGuest(
            @PathVariable Long id,
            @Valid @RequestBody GuestRequestDTO dto
    ) {
        GuestResponseDTO updatedGuest = guestService.updateGuest(id, dto);
        return ResponseEntity.ok(updatedGuest);
    }

    /**
     * Elimina (soft delete) un huésped del hotel del usuario autenticado.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.noContent().build();
    }
}
