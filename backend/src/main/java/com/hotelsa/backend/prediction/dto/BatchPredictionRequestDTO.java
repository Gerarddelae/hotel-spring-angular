package com.hotelsa.backend.prediction.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchPredictionRequestDTO {
    
    /** Lista de reservas con sus features para predicción */
    private List<PredictionFeatureDTO> bookings;
}
