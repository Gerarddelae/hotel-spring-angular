package com.hotelsa.backend.addon.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddonResponse {

    private Long id;
    private String name;
    private String description;
    private Integer price;

    // Exponer createdAt si se hace así en otros response DTOs
    private LocalDateTime createdAt;
}

