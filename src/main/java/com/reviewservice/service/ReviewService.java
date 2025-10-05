package com.reviewservice.service;

import com.reviewservice.dto.*;
import com.reviewservice.entity.Review;
import com.reviewservice.entity.ReviewHelpfulness;
import com.reviewservice.entity.ReviewImage;
import com.reviewservice.entity.ReviewResponse;
import com.reviewservice.exception.DuplicateReviewException;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.exception.UnauthorizedException;
import com.reviewservice.repository.ReviewHelpfulnessRepository;
import com.reviewservice.repository.ReviewRepository;
import com.reviewservice.repository.ReviewResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulnessRepository helpfulnessRepository;
    private final ReviewResponseRepository reviewResponseRepository;
    private final RatingSummaryService ratingSummaryService;
    private final ImageStorageService imageStorageService;

    @Transactional
    public ReviewResponseDto createReview(ReviewRequest request, List<MultipartFile> images) throws IOException {
        log.info("Creating review for entity: {} with ID: {}", request.getEntityType(), request.getEntityId());

        if (reviewRepository.existsByEntityTypeAndEntityIdAndUserId(
            request.getEntityType(), request.getEntityId(), request.getUserId())) {
            throw new DuplicateReviewException(
                "User has already reviewed this entity"
            );
        }

        Review review = Review.builder()
            .entityType(request.getEntityType())
            .entityId(request.getEntityId())
            .userId(request.getUserId())
            .rating(request.getRating())
            .title(request.getTitle())
            .comment(request.getComment())
            .verified(false)
            .status("ACTIVE")
            .build();

        Review savedReview = reviewRepository.save(review);

        if (images != null && !images.isEmpty()) {
            imageStorageService.storeImages(savedReview.getId(), images);
        }

        ratingSummaryService.updateRatingSummary(
            savedReview.getEntityType(),
            savedReview.getEntityId()
        );

        log.info("Review created successfully with ID: {}", savedReview.getId());
        return mapToDtoWithImages(savedReview);
    }

    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, String userId, ReviewUpdateRequest request, List<MultipartFile> newImages) throws IOException {
        log.info("Updating review ID: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findByIdAndStatus(reviewId, "ACTIVE")
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to update this review");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);

        if (newImages != null && !newImages.isEmpty()) {
            imageStorageService.storeImages(updatedReview.getId(), newImages);
        }

        ratingSummaryService.updateRatingSummary(
            updatedReview.getEntityType(),
            updatedReview.getEntityId()
        );

        log.info("Review updated successfully");
        return mapToDtoWithImages(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userId) {
        log.info("Deleting review ID: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findByIdAndStatus(reviewId, "ACTIVE")
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to delete this review");
        }

        review.setStatus("DELETED");
        reviewRepository.save(review);

        ratingSummaryService.updateRatingSummary(
            review.getEntityType(),
            review.getEntityId()
        );

        log.info("Review deleted successfully");
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long reviewId) {
        Review review = reviewRepository.findByIdAndStatus(reviewId, "ACTIVE")
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        return mapToDtoWithResponsesAndImages(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByEntity(
        String entityType,
        String entityId,
        Integer minRating,
        Integer maxRating,
        Boolean verifiedOnly,
        Pageable pageable
    ) {
        log.info("Fetching reviews for entity: {} with ID: {}", entityType, entityId);

        Page<Review> reviews;

        if (verifiedOnly != null && verifiedOnly) {
            reviews = reviewRepository.findVerifiedReviews(entityType, entityId, "ACTIVE", pageable);
        } else if (minRating != null || maxRating != null) {
            reviews = reviewRepository.findByEntityAndRatingRange(
                entityType, entityId, "ACTIVE", minRating, maxRating, pageable
            );
        } else {
            reviews = reviewRepository.findByEntityTypeAndEntityIdAndStatus(
                entityType, entityId, "ACTIVE", pageable
            );
        }

        return reviews.map(this::mapToDtoWithImages);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByUser(String userId, Pageable pageable) {
        log.info("Fetching reviews for user: {}", userId);
        return reviewRepository.findByUserIdAndStatus(userId, "ACTIVE", pageable)
            .map(this::mapToDtoWithImages);
    }

    @Transactional
    public void markReviewHelpfulness(Long reviewId, HelpfulnessRequest request) {
        log.info("Marking review {} as {} by user {}",
            reviewId, request.getHelpful() ? "helpful" : "unhelpful", request.getUserId());

        Review review = reviewRepository.findByIdAndStatus(reviewId, "ACTIVE")
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        ReviewHelpfulness existing = helpfulnessRepository
            .findByReviewIdAndUserId(reviewId, request.getUserId())
            .orElse(null);

        if (existing != null) {
            if (existing.getHelpful()) {
                review.setHelpfulCount(review.getHelpfulCount() - 1);
            } else {
                review.setUnhelpfulCount(review.getUnhelpfulCount() - 1);
            }
            helpfulnessRepository.delete(existing);
        }

        ReviewHelpfulness helpfulness = ReviewHelpfulness.builder()
            .reviewId(reviewId)
            .userId(request.getUserId())
            .helpful(request.getHelpful())
            .build();

        helpfulnessRepository.save(helpfulness);

        if (request.getHelpful()) {
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        } else {
            review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        }

        reviewRepository.save(review);
        log.info("Helpfulness marked successfully");
    }

    @Transactional
    public ReviewResponseDetailDto addResponse(Long reviewId, ResponseRequest request) {
        log.info("Adding response to review ID: {}", reviewId);

        Review review = reviewRepository.findByIdAndStatus(reviewId, "ACTIVE")
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        ReviewResponse response = ReviewResponse.builder()
            .reviewId(reviewId)
            .responderId(request.getResponderId())
            .responderType(request.getResponderType())
            .response(request.getResponse())
            .build();

        ReviewResponse savedResponse = reviewResponseRepository.save(response);
        log.info("Response added successfully");

        return mapResponseToDto(savedResponse);
    }

    private ReviewResponseDto mapToDtoWithImages(Review review) {
        List<ReviewImage> images = imageStorageService.getImagesByReviewId(review.getId());

        return ReviewResponseDto.builder()
            .id(review.getId())
            .entityType(review.getEntityType())
            .entityId(review.getEntityId())
            .userId(review.getUserId())
            .rating(review.getRating())
            .title(review.getTitle())
            .comment(review.getComment())
            .verified(review.getVerified())
            .helpfulCount(review.getHelpfulCount())
            .unhelpfulCount(review.getUnhelpfulCount())
            .images(images.stream()
                .map(img -> ImageMetadataDto.builder()
                    .id(img.getId())
                    .fileName(img.getFileName())
                    .contentType(img.getContentType())
                    .fileSize(img.getFileSize())
                    .uploadedAt(img.getUploadedAt())
                    .build())
                .collect(Collectors.toList()))
            .status(review.getStatus())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }

    private ReviewResponseDto mapToDtoWithResponsesAndImages(Review review) {
        List<ReviewResponse> responses = reviewResponseRepository
            .findByReviewIdOrderByCreatedAtDesc(review.getId());

        ReviewResponseDto dto = mapToDtoWithImages(review);
        dto.setResponses(responses.stream()
            .map(this::mapResponseToDto)
            .collect(Collectors.toList()));

        return dto;
    }

    private ReviewResponseDetailDto mapResponseToDto(ReviewResponse response) {
        return ReviewResponseDetailDto.builder()
            .id(response.getId())
            .responderId(response.getResponderId())
            .responderType(response.getResponderType())
            .response(response.getResponse())
            .createdAt(response.getCreatedAt())
            .updatedAt(response.getUpdatedAt())
            .build();
    }
}
