package com.hotelsa.backend.guest.exception;

import com.hotelsa.backend.common.exception.ResourceAccessDeniedException;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un huésped
 * que no pertenece a su hotel.
 */
public class GuestAccessDeniedException extends ResourceAccessDeniedException {

    public GuestAccessDeniedException(Long guestId) {
        super("Huésped", guestId);
    }

    public GuestAccessDeniedException(String message) {
        super(message);
    }
}
