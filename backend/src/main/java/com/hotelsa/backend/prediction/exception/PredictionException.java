package com.hotelsa.backend.prediction.exception;

public class PredictionException extends RuntimeException {
    
    public PredictionException() {
        super("Error al realizar la predicción");
    }
    
    public PredictionException(String message) {
        super(message);
    }
    
    public PredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
