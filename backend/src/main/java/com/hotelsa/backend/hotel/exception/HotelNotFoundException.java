package com.hotelsa.backend.hotel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HotelNotFoundException extends RuntimeException {

    public HotelNotFoundException(String message) {
        super(message);
    }

    public HotelNotFoundException(Long id) {
        super("Hotel with id " + id + " not found");
    }
}
