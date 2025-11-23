package com.hotelsa.backend.addon.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddonResponse {

    private Long id;
    private String name;
    private String description;
    private Integer price;
    private LocalDateTime createdAt;
}
