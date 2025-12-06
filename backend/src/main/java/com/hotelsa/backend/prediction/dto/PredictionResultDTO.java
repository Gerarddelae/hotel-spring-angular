package com.hotelsa.backend.prediction.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResultDTO {
    
    /** ID de la reserva (para mapear respuesta de Flask) */
    private Long bookingId;
    
    /** Probabilidad de cancelación (0.0 - 1.0) */
    private Double cancellationProbability;
    
    /** Predicción binaria: true = cancelará */
    private Boolean willCancel;
    
    /** Nivel de riesgo: LOW, MEDIUM, HIGH, UNKNOWN */
    private String riskLevel;
}
