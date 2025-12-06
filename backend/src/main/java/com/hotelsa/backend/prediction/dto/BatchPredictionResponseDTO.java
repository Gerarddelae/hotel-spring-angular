package com.hotelsa.backend.prediction.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchPredictionResponseDTO {
    
    /** Lista de predicciones con detalles de cada reserva */
    private List<BookingPredictionDetailDTO> predictions;
    
    /** Total de reservas analizadas */
    private Integer totalBookings;
    
    /** Cantidad de reservas con riesgo alto */
    private Integer highRiskCount;
    
    /** Cantidad de reservas con riesgo medio */
    private Integer mediumRiskCount;
    
    /** Cantidad de reservas con riesgo bajo */
    private Integer lowRiskCount;
    
    /** Probabilidad promedio de cancelación */
    private Double averageCancellationProbability;
}
