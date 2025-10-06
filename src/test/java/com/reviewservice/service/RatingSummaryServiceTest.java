package com.reviewservice.service;

import com.reviewservice.dto.RatingSummaryDto;
import com.reviewservice.entity.RatingSummary;
import com.reviewservice.entity.Review;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.repository.RatingSummaryRepository;
import com.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingSummaryServiceTest {

    @Mock
    private RatingSummaryRepository ratingSummaryRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private RatingSummaryService ratingSummaryService;

    private RatingSummary ratingSummary;
    private List<Review> reviews;

    @BeforeEach
    void setUp() {
        ratingSummary = RatingSummary.builder()
                .id(1L)
                .entityType("PRODUCT")
                .entityId("PROD-123")
                .averageRating(4.5)
                .totalReviews(10)
                .fiveStarCount(6)
                .fourStarCount(3)
                .threeStarCount(1)
                .twoStarCount(0)
                .oneStarCount(0)
                .build();

        reviews = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            reviews.add(Review.builder().rating(5).build());
        }
        for (int i = 0; i < 3; i++) {
            reviews.add(Review.builder().rating(4).build());
        }
        reviews.add(Review.builder().rating(3).build());
    }

    @Test
    void updateRatingSummary_NewSummary_Success() {
        when(reviewRepository.findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(
                "PRODUCT", "PROD-123", "ACTIVE"))
                .thenReturn(reviews);
        when(ratingSummaryRepository.findByEntityTypeAndEntityId("PRODUCT", "PROD-123"))
                .thenReturn(Optional.empty());
        when(ratingSummaryRepository.save(any(RatingSummary.class)))
                .thenReturn(ratingSummary);

        ratingSummaryService.updateRatingSummary("PRODUCT", "PROD-123");

        verify(ratingSummaryRepository, times(1)).save(any(RatingSummary.class));
    }

    @Test
    void updateRatingSummary_ExistingSummary_Success() {
        when(reviewRepository.findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(
                "PRODUCT", "PROD-123", "ACTIVE"))
                .thenReturn(reviews);
        when(ratingSummaryRepository.findByEntityTypeAndEntityId("PRODUCT", "PROD-123"))
                .thenReturn(Optional.of(ratingSummary));
        when(ratingSummaryRepository.save(any(RatingSummary.class)))
                .thenReturn(ratingSummary);

        ratingSummaryService.updateRatingSummary("PRODUCT", "PROD-123");

        verify(ratingSummaryRepository, times(1)).save(any(RatingSummary.class));
    }

    @Test
    void updateRatingSummary_NoReviews_Success() {
        when(reviewRepository.findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(
                "PRODUCT", "PROD-123", "ACTIVE"))
                .thenReturn(new ArrayList<>());
        when(ratingSummaryRepository.findByEntityTypeAndEntityId("PRODUCT", "PROD-123"))
                .thenReturn(Optional.of(ratingSummary));
        when(ratingSummaryRepository.save(any(RatingSummary.class)))
                .thenReturn(ratingSummary);

        ratingSummaryService.updateRatingSummary("PRODUCT", "PROD-123");

        verify(ratingSummaryRepository, times(1)).save(any(RatingSummary.class));
    }

    @Test
    void getRatingSummary_Success() {
        when(ratingSummaryRepository.findByEntityTypeAndEntityId("PRODUCT", "PROD-123"))
                .thenReturn(Optional.of(ratingSummary));

        RatingSummaryDto result = ratingSummaryService.getRatingSummary("PRODUCT", "PROD-123");

        assertNotNull(result);
        assertEquals("PRODUCT", result.getEntityType());
        assertEquals("PROD-123", result.getEntityId());
        assertEquals(4.5, result.getAverageRating());
        assertEquals(10, result.getTotalReviews());
        assertNotNull(result.getRatingDistribution());
        verify(ratingSummaryRepository, times(1))
                .findByEntityTypeAndEntityId("PRODUCT", "PROD-123");
    }

    @Test
    void getRatingSummary_NotFound_ThrowsException() {
        when(ratingSummaryRepository.findByEntityTypeAndEntityId("PRODUCT", "PROD-123"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ratingSummaryService.getRatingSummary("PRODUCT", "PROD-123"));

        verify(ratingSummaryRepository, times(1))
                .findByEntityTypeAndEntityId("PRODUCT", "PROD-123");
    }
}
