package com.reviewservice.service;

import com.reviewservice.dto.RatingSummaryDto;
import com.reviewservice.entity.RatingSummary;
import com.reviewservice.entity.Review;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.repository.RatingSummaryRepository;
import com.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingSummaryService {

    private final RatingSummaryRepository ratingSummaryRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void updateRatingSummary(String entityType, String entityId) {
        log.info("Updating rating summary for entity: {} with ID: {}", entityType, entityId);

        List<Review> reviews = reviewRepository
            .findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(
                entityType, entityId, "ACTIVE"
            );

        RatingSummary summary = ratingSummaryRepository
            .findByEntityTypeAndEntityId(entityType, entityId)
            .orElse(RatingSummary.builder()
                .entityType(entityType)
                .entityId(entityId)
                .build());

        if (reviews.isEmpty()) {
            summary.setAverageRating(0.0);
            summary.setTotalReviews(0);
            summary.setFiveStarCount(0);
            summary.setFourStarCount(0);
            summary.setThreeStarCount(0);
            summary.setTwoStarCount(0);
            summary.setOneStarCount(0);
        } else {
            int total = reviews.size();
            double sum = reviews.stream().mapToInt(Review::getRating).sum();
            double average = sum / total;

            Map<Integer, Long> ratingCounts = reviews.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Review::getRating,
                    java.util.stream.Collectors.counting()
                ));

            summary.setAverageRating(Math.round(average * 10.0) / 10.0);
            summary.setTotalReviews(total);
            summary.setFiveStarCount(ratingCounts.getOrDefault(5, 0L).intValue());
            summary.setFourStarCount(ratingCounts.getOrDefault(4, 0L).intValue());
            summary.setThreeStarCount(ratingCounts.getOrDefault(3, 0L).intValue());
            summary.setTwoStarCount(ratingCounts.getOrDefault(2, 0L).intValue());
            summary.setOneStarCount(ratingCounts.getOrDefault(1, 0L).intValue());
        }

        ratingSummaryRepository.save(summary);
        log.info("Rating summary updated successfully");
    }

    @Transactional(readOnly = true)
    public RatingSummaryDto getRatingSummary(String entityType, String entityId) {
        log.info("Fetching rating summary for entity: {} with ID: {}", entityType, entityId);

        RatingSummary summary = ratingSummaryRepository
            .findByEntityTypeAndEntityId(entityType, entityId)
            .orElseThrow(() -> new ResourceNotFoundException("Rating summary not found"));

        return mapToDto(summary);
    }

    private RatingSummaryDto mapToDto(RatingSummary summary) {
        Map<Integer, Integer> distribution = new HashMap<>();
        distribution.put(5, summary.getFiveStarCount());
        distribution.put(4, summary.getFourStarCount());
        distribution.put(3, summary.getThreeStarCount());
        distribution.put(2, summary.getTwoStarCount());
        distribution.put(1, summary.getOneStarCount());

        return RatingSummaryDto.builder()
            .entityType(summary.getEntityType())
            .entityId(summary.getEntityId())
            .averageRating(summary.getAverageRating())
            .totalReviews(summary.getTotalReviews())
            .ratingDistribution(distribution)
            .updatedAt(summary.getUpdatedAt())
            .build();
    }
}
