package com.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDetailDto {

    private Long id;
    private String responderId;
    private String responderType;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
