package com.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewservice.dto.ReviewRequest;
import com.reviewservice.dto.ReviewUpdateRequest;
import com.reviewservice.entity.Review;
import com.reviewservice.repository.ReviewRepository;
import com.reviewservice.repository.RatingSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RatingSummaryRepository ratingSummaryRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        ratingSummaryRepository.deleteAll();
    }

    @Test
    void createReview_ValidRequest_ReturnsCreated() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(5)
            .title("Excellent Product")
            .comment("Very satisfied with the quality")
            // .images(new ArrayList<>())
            .build();

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.entityType").value("PRODUCT"))
            .andExpect(jsonPath("$.entityId").value("PROD-123"))
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.title").value("Excellent Product"));
    }

    @Test
    void createReview_InvalidRating_ReturnsBadRequest() throws Exception {
        ReviewRequest request = ReviewRequest.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(6)
            .title("Test")
            .build();

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createReview_DuplicateReview_ReturnsConflict() throws Exception {
        Review existingReview = Review.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(4)
            .status("ACTIVE")
            .build();
        reviewRepository.save(existingReview);

        ReviewRequest request = ReviewRequest.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(5)
            .title("Test")
            .build();

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void getReviewById_ExistingReview_ReturnsOk() throws Exception {
        Review review = Review.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(5)
            .title("Great")
            .status("ACTIVE")
            // .images(new ArrayList<>())
            .build();
        Review savedReview = reviewRepository.save(review);

        mockMvc.perform(get("/api/v1/reviews/" + savedReview.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedReview.getId()))
            .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void getReviewById_NonExistingReview_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateReview_ValidRequest_ReturnsOk() throws Exception {
        Review review = Review.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(4)
            .title("Good")
            .status("ACTIVE")
            // .images(new ArrayList<>())
            .build();
        Review savedReview = reviewRepository.save(review);

        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
            .rating(5)
            .title("Excellent")
            .comment("Updated review")
            .build();

        mockMvc.perform(put("/api/v1/reviews/" + savedReview.getId())
                .param("userId", "USER-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.title").value("Excellent"));
    }

    @Test
    void deleteReview_ValidRequest_ReturnsNoContent() throws Exception {
        Review review = Review.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(4)
            .status("ACTIVE")
            // .images(new ArrayList<>())
            .build();
        Review savedReview = reviewRepository.save(review);

        mockMvc.perform(delete("/api/v1/reviews/" + savedReview.getId())
                .param("userId", "USER-1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getReviewsByEntity_ReturnsOk() throws Exception {
        Review review1 = Review.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-1")
            .rating(5)
            .status("ACTIVE")
            // .images(new ArrayList<>())
            .build();

        Review review2 = Review.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .userId("USER-2")
            .rating(4)
            .status("ACTIVE")
            // .images(new ArrayList<>())
            .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        mockMvc.perform(get("/api/v1/reviews")
                .param("entityType", "PRODUCT")
                .param("entityId", "PROD-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2));
    }
}
