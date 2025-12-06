package com.hotelsa.backend.prediction.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionFeatureDTO {
    
    /** ID de la reserva para mapear la respuesta */
    private Long bookingId;
    
    /** Días entre fecha de reserva y check-in */
    private Integer leadTime;
    
    /** Precio promedio por noche (ADR) */
    private BigDecimal avgPricePerRoom;
    
    /** Cantidad de addons/servicios adicionales */
    private Integer noOfSpecialRequests;
    
    /** Día del mes de llegada (1-31) */
    private Integer arrivalDate;
    
    /** Mes de llegada (1-12) */
    private Integer arrivalMonth;
    
    /** Noches entre semana (Lun-Vie) */
    private Integer noOfWeekNights;
    
    /** Noches de fin de semana (Sab-Dom) */
    private Integer noOfWeekendNights;
    
    /** Cancelaciones previas del huésped */
    private Integer previousCancellations;
    
    /** Reservas previas no canceladas del huésped */
    private Integer previousBookingsNotCanceled;
    
    /** Si el huésped es cliente repetido */
    private Boolean isRepeatedGuest;
}
