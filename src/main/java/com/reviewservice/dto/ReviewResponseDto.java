package com.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {

    private Long id;
    private String entityType;
    private String entityId;
    private String userId;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean verified;
    private Integer helpfulCount;
    private Integer unhelpfulCount;
    private List<ImageMetadataDto> images;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewResponseDetailDto> responses;
}
