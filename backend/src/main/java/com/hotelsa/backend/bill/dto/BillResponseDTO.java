package com.hotelsa.backend.bill.dto;

import com.hotelsa.backend.billaddon.dto.BillAddonResponseDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponseDTO {
    private Long id;
    private Long bookingId;

    // Información del huésped
    private Long guestId;
    private String guestName;

    // Información de la habitación
    private Long roomId;
    private String roomNumber;

    // Información de fechas y estadía
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;

    // Información de precios
    private BigDecimal roomPricePerNight;
    private BigDecimal accommodationSubtotal;
    private BigDecimal addonsSubtotal;

    // Información de la factura
    private String notes;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private List<BillAddonResponseDTO> addons;
}
