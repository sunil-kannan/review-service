package com.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummaryDto {

    private String entityType;
    private String entityId;
    private Double averageRating;
    private Integer totalReviews;
    private Map<Integer, Integer> ratingDistribution;
    private LocalDateTime updatedAt;
}
