package com.hotelsa.backend.addon.exception;

import com.hotelsa.backend.common.exception.ResourceAccessDeniedException;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un addon
 * que no pertenece a su hotel.
 */
public class AddonAccessDeniedException extends ResourceAccessDeniedException {

    public AddonAccessDeniedException(Long addonId) {
        super("Addon", addonId);
    }

    public AddonAccessDeniedException(String message) {
        super(message);
    }
}
