package com.hotelsa.backend.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BookingReplaceRequestDTO {

    @NotNull
    private Long guestId;

    @NotNull
    private Long roomId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    private String notes;

    private com.hotelsa.backend.booking.enums.BookingStatus status;

    private List<BookingAddonRequest> addons;
}
