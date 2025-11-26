package com.hotelsa.backend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusCountDTO {
    private int total;
    private int pending;
    private int confirmed;
    private int checkedIn;
}
