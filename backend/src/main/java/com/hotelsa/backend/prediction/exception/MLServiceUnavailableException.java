package com.hotelsa.backend.prediction.exception;

public class MLServiceUnavailableException extends RuntimeException {
    
    public MLServiceUnavailableException() {
        super("El servicio de ML no está disponible");
    }
    
    public MLServiceUnavailableException(String message) {
        super(message);
    }
    
    public MLServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
