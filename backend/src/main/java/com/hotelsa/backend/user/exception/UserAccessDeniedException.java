package com.hotelsa.backend.user.exception;

import com.hotelsa.backend.common.exception.ResourceAccessDeniedException;

/**
 * Excepción lanzada cuando un usuario intenta acceder a otro usuario
 * que no pertenece a su hotel.
 */
public class UserAccessDeniedException extends ResourceAccessDeniedException {

    public UserAccessDeniedException(Long userId) {
        super("Usuario", userId);
    }

    public UserAccessDeniedException(String message) {
        super(message);
    }
}
