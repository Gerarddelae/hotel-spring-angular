package com.hotelsa.backend.room.controller;

import com.hotelsa.backend.room.dto.RoomRequestDTO;
import com.hotelsa.backend.room.dto.RoomResponseDTO;
import com.hotelsa.backend.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * Crea una nueva habitación para el hotel del usuario autenticado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody RoomRequestDTO dto) {
        RoomResponseDTO createdRoom = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    /**
     * Obtiene los detalles de una habitación específica (solo si pertenece al hotel del usuario).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long id) {
        RoomResponseDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    /**
     * Lista todas las habitaciones del hotel del usuario autenticado.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsForCurrentHotel() {
        List<RoomResponseDTO> rooms = roomService.getRoomsForCurrentHotel();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Obtiene las habitaciones disponibles en un rango de fechas para el hotel del usuario autenticado.
     * Query params: checkIn (yyyy-MM-dd), checkOut (yyyy-MM-dd)
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<RoomResponseDTO>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        // Validaciones básicas
        if (checkIn == null || checkOut == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!checkIn.isBefore(checkOut)) {
            return ResponseEntity.badRequest().build();
        }

        List<RoomResponseDTO> available = roomService.getAvailableRooms(checkIn, checkOut);
        return ResponseEntity.ok(available);
    }

    /**
     * Actualiza una habitación (solo si pertenece al hotel del usuario).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequestDTO dto
    ) {
        RoomResponseDTO updatedRoom = roomService.updateRoom(id, dto);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * Elimina (soft delete) una habitación del hotel del usuario autenticado.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // Dashboard endpoints
    /**
     * Obtiene el conteo de habitaciones ocupadas.
     */
    @GetMapping("/occupied-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<com.hotelsa.backend.room.dto.OccupiedRoomsCountDTO> getOccupiedCount() {
        com.hotelsa.backend.room.dto.OccupiedRoomsCountDTO count = roomService.getOccupiedCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Obtiene el resumen del dashboard con todas las habitaciones, su estado y booking activo si existe.
     */
    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<com.hotelsa.backend.room.dto.RoomDashboardItemDTO>> getDashboardSummary() {
        List<com.hotelsa.backend.room.dto.RoomDashboardItemDTO> summary = roomService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Obtiene las opciones de estado de habitación (AVAILABLE, OCCUPIED, MAINTENANCE).
     */
    @GetMapping("/status-options")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<String>> getStatusOptions() {
        List<String> statuses = roomService.getStatusOptions();
        return ResponseEntity.ok(statuses);
    }
}
