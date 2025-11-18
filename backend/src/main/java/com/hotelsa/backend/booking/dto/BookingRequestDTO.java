package com.hotelsa.backend.booking.dto;

import com.hotelsa.backend.booking.enums.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {

    @NotNull(message = "El ID del huésped es obligatorio")
    private Long guestId;

    @NotNull(message = "El ID de la habitación es obligatorio")
    private Long roomId;

    @NotNull(message = "La fecha de check-in es obligatoria")
    private LocalDate checkInDate;

    @NotNull(message = "La fecha de check-out es obligatoria")
    private LocalDate checkOutDate;

    @NotNull(message = "El estado de la reserva es obligatorio")
    private BookingStatus status;

    @NotBlank(message = "El creador de la reserva es obligatorio")
    @Size(max = 100, message = "El creador no puede exceder 100 caracteres")
    private String createdBy;

    @NotNull(message = "El tiempo de antelación de la reserva es obligatorio")
    private LocalDate bookingLeadTime;

    @Size(max = 255, message = "Las notas no pueden exceder 255 caracteres")
    private String notes;
}
