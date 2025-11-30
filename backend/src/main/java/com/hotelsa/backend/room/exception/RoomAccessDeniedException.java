package com.hotelsa.backend.room.exception;

import com.hotelsa.backend.common.exception.ResourceAccessDeniedException;

/**
 * Excepción lanzada cuando un usuario intenta acceder a una habitación
 * que no pertenece a su hotel.
 */
public class RoomAccessDeniedException extends ResourceAccessDeniedException {

    public RoomAccessDeniedException(Long roomId) {
        super("Habitación", roomId);
    }

    public RoomAccessDeniedException(String message) {
        super(message);
    }
}
