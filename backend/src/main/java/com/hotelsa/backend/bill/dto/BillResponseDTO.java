package com.hotelsa.backend.bill.dto;

import com.hotelsa.backend.billaddon.dto.BillAddonResponseDTO;
import lombok.*;

import java.math.BigDecimal;
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
    private String notes;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private List<BillAddonResponseDTO> addons;
}
