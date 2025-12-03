package com.hotelsa.backend.prediction.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDataDTO {
    
    // === Features de la reserva ===
    
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
    
    // === Features del cliente ===
    
    /** Cancelaciones previas del huésped */
    private Integer previousCancellations;
    
    /** Reservas previas no canceladas del huésped */
    private Integer previousBookingsNotCanceled;
    
    /** Si el huésped es cliente repetido (0 o 1) */
    private Integer isRepeatedGuest;
    
    // === Target ===
    
    /** Variable objetivo: 1 = cancelado, 0 = no cancelado */
    private Integer isCanceled;
}
