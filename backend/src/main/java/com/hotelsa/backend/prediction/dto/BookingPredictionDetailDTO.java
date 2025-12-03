package com.hotelsa.backend.prediction.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPredictionDetailDTO {
    
    /** ID de la reserva */
    private Long bookingId;
    
    /** Nombre completo del huésped */
    private String guestName;
    
    /** Email del huésped */
    private String guestEmail;
    
    /** Número de habitación */
    private String roomNumber;
    
    /** Fecha de check-in */
    private LocalDate checkInDate;
    
    /** Fecha de check-out */
    private LocalDate checkOutDate;
    
    /** Monto total de la reserva */
    private BigDecimal totalAmount;
    
    /** Probabilidad de cancelación (0.0 - 1.0) */
    private Double cancellationProbability;
    
    /** Predicción binaria: true = cancelará */
    private Boolean willCancel;
    
    /** Nivel de riesgo: LOW, MEDIUM, HIGH, UNKNOWN */
    private String riskLevel;
}
