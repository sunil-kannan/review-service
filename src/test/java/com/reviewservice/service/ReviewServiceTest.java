package com.reviewservice.service;

import com.reviewservice.dto.ReviewRequest;
import com.reviewservice.dto.ReviewResponseDto;
import com.reviewservice.dto.ReviewUpdateRequest;
import com.reviewservice.entity.Review;
import com.reviewservice.exception.DuplicateReviewException;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.exception.UnauthorizedException;
import com.reviewservice.repository.ReviewHelpfulnessRepository;
import com.reviewservice.repository.ReviewRepository;
import com.reviewservice.repository.ReviewResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewHelpfulnessRepository helpfulnessRepository;

    @Mock
    private ReviewResponseRepository reviewResponseRepository;

    @Mock
    private RatingSummaryService ratingSummaryService;

    @InjectMocks
    private ReviewService reviewService;

    private ReviewRequest reviewRequest;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewRequest = ReviewRequest.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(5)
            .title("Great Product")
            .comment("Excellent quality and fast delivery")
            .images(new ArrayList<>())
            .build();

        review = Review.builder()
            .id(1L)
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(5)
            .title("Great Product")
            .comment("Excellent quality and fast delivery")
            .verified(false)
            .status("ACTIVE")
            .helpfulCount(0)
            .unhelpfulCount(0)
            .images(new ArrayList<>())
            .build();
    }

    @Test
    void createReview_Success() {
        when(reviewRepository.existsByEntityTypeAndEntityIdAndUserId(
            anyString(), anyString(), anyString())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDto result = reviewService.createReview(reviewRequest);

        assertNotNull(result);
        assertEquals(review.getId(), result.getId());
        assertEquals(review.getRating(), result.getRating());
        assertEquals(review.getTitle(), result.getTitle());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(ratingSummaryService, times(1))
            .updateRatingSummary(review.getEntityType(), review.getEntityId());
    }

    @Test
    void createReview_DuplicateReview_ThrowsException() {
        when(reviewRepository.existsByEntityTypeAndEntityIdAndUserId(
            anyString(), anyString(), anyString())).thenReturn(true);

        assertThrows(DuplicateReviewException.class, () ->
            reviewService.createReview(reviewRequest));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_Success() {
        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
            .rating(4)
            .title("Updated Title")
            .comment("Updated comment")
            .build();

        when(reviewRepository.findByIdAndStatus(1L, "ACTIVE"))
            .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDto result = reviewService.updateReview(1L, "USER-1", updateRequest);

        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(ratingSummaryService, times(1))
            .updateRatingSummary(review.getEntityType(), review.getEntityId());
    }

    @Test
    void updateReview_NotFound_ThrowsException() {
        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
            .rating(4)
            .title("Updated Title")
            .build();

        when(reviewRepository.findByIdAndStatus(1L, "ACTIVE"))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            reviewService.updateReview(1L, "USER-1", updateRequest));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_Unauthorized_ThrowsException() {
        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
            .rating(4)
            .title("Updated Title")
            .build();

        when(reviewRepository.findByIdAndStatus(1L, "ACTIVE"))
            .thenReturn(Optional.of(review));

        assertThrows(UnauthorizedException.class, () ->
            reviewService.updateReview(1L, "DIFFERENT-USER", updateRequest));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview_Success() {
        when(reviewRepository.findByIdAndStatus(1L, "ACTIVE"))
            .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.deleteReview(1L, "USER-1");

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(ratingSummaryService, times(1))
            .updateRatingSummary(review.getEntityType(), review.getEntityId());
    }

    @Test
    void deleteReview_NotFound_ThrowsException() {
        when(reviewRepository.findByIdAndStatus(1L, "ACTIVE"))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            reviewService.deleteReview(1L, "USER-1"));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewById_Success() {
        when(reviewRepository.findByIdAndStatus(1L, "ACTIVE"))
            .thenReturn(Optional.of(review));
        when(reviewResponseRepository.findByReviewIdOrderByCreatedAtDesc(1L))
            .thenReturn(new ArrayList<>());

        ReviewResponseDto result = reviewService.getReviewById(1L);

        assertNotNull(result);
        assertEquals(review.getId(), result.getId());
        verify(reviewRepository, times(1)).findByIdAndStatus(1L, "ACTIVE");
    }

    @Test
    void getReviewsByEntity_Success() {
        List<Review> reviews = List.of(review);
        Page<Review> reviewPage = new PageImpl<>(reviews);
        Pageable pageable = PageRequest.of(0, 10);

        when(reviewRepository.findByEntityTypeAndEntityIdAndStatus(
            "PRODUCT", "PROD-123", "ACTIVE", pageable))
            .thenReturn(reviewPage);

        Page<ReviewResponseDto> result = reviewService.getReviewsByEntity(
            "PRODUCT", "PROD-123", null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reviewRepository, times(1))
            .findByEntityTypeAndEntityIdAndStatus("PRODUCT", "PROD-123", "ACTIVE", pageable);
    }

    @Test
    void getReviewsByUser_Success() {
        List<Review> reviews = List.of(review);
        Page<Review> reviewPage = new PageImpl<>(reviews);
        Pageable pageable = PageRequest.of(0, 10);

        when(reviewRepository.findByUserIdAndStatus("USER-1", "ACTIVE", pageable))
            .thenReturn(reviewPage);

        Page<ReviewResponseDto> result = reviewService.getReviewsByUser("USER-1", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reviewRepository, times(1))
            .findByUserIdAndStatus("USER-1", "ACTIVE", pageable);
    }
}
