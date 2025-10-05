package com.reviewservice.controller;

import com.reviewservice.dto.*;
import com.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "APIs for managing reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new review", description = "Submit a review for a product or service with optional images")
    public ResponseEntity<ReviewResponseDto> createReview(
        @Valid @ModelAttribute ReviewRequest request,
        @RequestParam(required = false) List<MultipartFile> images
    ) throws IOException {
        ReviewResponseDto response = reviewService.createReview(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update a review", description = "Update an existing review with optional new images")
    public ResponseEntity<ReviewResponseDto> updateReview(
        @PathVariable Long reviewId,
        @RequestParam String userId,
        @Valid @ModelAttribute ReviewUpdateRequest request,
        @RequestParam(required = false) List<MultipartFile> images
    ) throws IOException {
        ReviewResponseDto response = reviewService.updateReview(reviewId, userId, request, images);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review", description = "Delete a review by ID")
    public ResponseEntity<Void> deleteReview(
        @PathVariable Long reviewId,
        @RequestParam String userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Retrieve a specific review with responses")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long reviewId) {
        ReviewResponseDto response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get reviews by entity", description = "Retrieve reviews for a specific entity with filters")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByEntity(
        @RequestParam @Parameter(description = "Entity type (e.g., PRODUCT, SERVICE)") String entityType,
        @RequestParam @Parameter(description = "Entity ID") String entityId,
        @RequestParam(required = false) @Parameter(description = "Minimum rating filter") Integer minRating,
        @RequestParam(required = false) @Parameter(description = "Maximum rating filter") Integer maxRating,
        @RequestParam(required = false) @Parameter(description = "Show only verified reviews") Boolean verifiedOnly,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByEntity(
            entityType, entityId, minRating, maxRating, verifiedOnly, pageable
        );
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user", description = "Retrieve all reviews submitted by a user")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByUser(
        @PathVariable String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{reviewId}/helpfulness")
    @Operation(summary = "Mark review helpfulness", description = "Mark a review as helpful or unhelpful")
    public ResponseEntity<Void> markHelpfulness(
        @PathVariable Long reviewId,
        @Valid @RequestBody HelpfulnessRequest request
    ) {
        reviewService.markReviewHelpfulness(reviewId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/responses")
    @Operation(summary = "Add response to review", description = "Add a response to a review (vendor/admin)")
    public ResponseEntity<ReviewResponseDetailDto> addResponse(
        @PathVariable Long reviewId,
        @Valid @RequestBody ResponseRequest request
    ) {
        ReviewResponseDetailDto response = reviewService.addResponse(reviewId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
