package com.hotelsa.backend.billaddon.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillAddonResponseDTO {
    private Long addonId;
    private String addonName;
    private String description;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
