package com.hotelsa.backend.booking.dto;

import com.hotelsa.backend.booking.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long id;
    private Long guestId;
    private String guestName;
    private Long roomId;
    private String roomNumber;
    private Long hotelId;
    private String hotelName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDate bookingLeadTime;
    private String notes;
    private List<BookingAddonResponse> addons;
    private BigDecimal totalAmount;
}
