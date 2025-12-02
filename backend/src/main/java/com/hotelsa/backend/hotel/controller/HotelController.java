package com.hotelsa.backend.hotel.controller;

import com.hotelsa.backend.hotel.dto.HotelResponse;
import com.hotelsa.backend.hotel.dto.HotelUpdateRequest;
import com.hotelsa.backend.hotel.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    /**
     * Obtiene los detalles de un hotel específico.
     * Solo usuarios autenticados con el rol ADMIN pueden acceder.
     *
     * @param id ID del hotel
     * @return HotelResponse con los datos del hotel
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        HotelResponse response = hotelService.getHotelById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza toda la información del hotel (PUT).
     * Todos los campos son obligatorios y se reemplazan completamente.
     * Solo usuarios autenticados con el rol ADMIN pueden ejecutar esta acción.
     *
     * @param id      ID del hotel a actualizar
     * @param request DTO con todos los campos del hotel
     * @return HotelResponse con los datos actualizados
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelUpdateRequest request
    ) {
        HotelResponse response = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza parcialmente la información del hotel (PATCH).
     * Solo se actualizan los campos proporcionados (no nulos) en el request.
     * Solo usuarios autenticados con el rol ADMIN pueden ejecutar esta acción.
     *
     * @param id      ID del hotel a actualizar
     * @param request DTO con los campos a actualizar (opcionales)
     * @return HotelResponse con los datos actualizados
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponse> patchHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelUpdateRequest request
    ) {
        HotelResponse response = hotelService.patchHotel(id, request);
        return ResponseEntity.ok(response);
    }
}
