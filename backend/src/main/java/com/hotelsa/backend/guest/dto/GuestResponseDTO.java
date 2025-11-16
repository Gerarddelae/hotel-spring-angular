package com.hotelsa.backend.guest.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestResponseDTO {
    private Long id;
    private String fullName;
    private String documentType;
    private String documentNumber;
    private String email;
    private String phone;
    private String address;
    private Integer previousCancellations;
    private Integer totalBookingsClient;
    private Long hotelId;
    private String hotelName;
}

