package com.hotelsa.backend.addon.exception;

public class AddonNotFoundException extends RuntimeException {
    public AddonNotFoundException(String message) {
        super(message);
    }
}

