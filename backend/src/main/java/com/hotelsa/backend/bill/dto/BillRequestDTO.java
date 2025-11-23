package com.hotelsa.backend.bill.dto;

import com.hotelsa.backend.bill.enums.BillStatus;
import com.hotelsa.backend.bill.enums.PaymentMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillRequestDTO {

    private String notes;

    private BillStatus status;

    private PaymentMethod paymentMethod;
}
