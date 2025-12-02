package com.hotelsa.backend.hotel.service;

import com.hotelsa.backend.hotel.dto.HotelResponse;
import com.hotelsa.backend.hotel.dto.HotelUpdateRequest;

public interface HotelService {

    /**
     * Actualiza todos los campos del hotel (PUT).
     *
     * @param id      ID del hotel a actualizar
     * @param request DTO con todos los campos obligatorios
     * @return HotelResponse con los datos actualizados
     */
    HotelResponse updateHotel(Long id, HotelUpdateRequest request);

    /**
     * Actualiza parcialmente los campos del hotel (PATCH).
     * Solo se actualizan los campos que no son nulos en el request.
     *
     * @param id      ID del hotel a actualizar
     * @param request DTO con los campos a actualizar (opcionales)
     * @return HotelResponse con los datos actualizados
     */
    HotelResponse patchHotel(Long id, HotelUpdateRequest request);

    /**
     * Obtiene un hotel por su ID.
     *
     * @param id ID del hotel
     * @return HotelResponse con los datos del hotel
     */
    HotelResponse getHotelById(Long id);
}
