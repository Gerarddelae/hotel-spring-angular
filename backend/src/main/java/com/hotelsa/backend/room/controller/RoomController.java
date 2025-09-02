package com.hotelsa.backend.room.controller;

import com.hotelsa.backend.room.dto.RoomRequestDTO;
import com.hotelsa.backend.room.dto.RoomResponseDTO;
import com.hotelsa.backend.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody RoomRequestDTO dto) {
        RoomResponseDTO createdRoom = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long id) {
        RoomResponseDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<RoomResponseDTO>> getRoomsByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequestDTO dto) {
        RoomResponseDTO updatedRoom = roomService.updateRoom(id, dto);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
