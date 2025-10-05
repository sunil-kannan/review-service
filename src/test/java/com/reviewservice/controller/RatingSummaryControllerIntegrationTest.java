package com.reviewservice.controller;

import com.reviewservice.entity.RatingSummary;
import com.reviewservice.entity.Review;
import com.reviewservice.repository.RatingSummaryRepository;
import com.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class RatingSummaryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingSummaryRepository ratingSummaryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        ratingSummaryRepository.deleteAll();
    }

    @Test
    void getRatingSummary_ExistingSummary_ReturnsOk() throws Exception {
        RatingSummary summary = RatingSummary.builder()
            .entityType("PRODUCT")
            .entityId("PROD-123")
            .averageRating(4.5)
            .totalReviews(10)
            .fiveStarCount(5)
            .fourStarCount(3)
            .threeStarCount(2)
            .twoStarCount(0)
            .oneStarCount(0)
            .build();
        ratingSummaryRepository.save(summary);

        mockMvc.perform(get("/api/v1/ratings")
                .param("entityType", "PRODUCT")
                .param("entityId", "PROD-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.entityType").value("PRODUCT"))
            .andExpect(jsonPath("$.entityId").value("PROD-123"))
            .andExpect(jsonPath("$.averageRating").value(4.5))
            .andExpect(jsonPath("$.totalReviews").value(10));
    }

    @Test
    void getRatingSummary_NonExistingSummary_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/ratings")
                .param("entityType", "PRODUCT")
                .param("entityId", "PROD-999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void refreshRatingSummary_WithReviews_ReturnsOk() throws Exception {
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

        mockMvc.perform(post("/api/v1/ratings/refresh")
                .param("entityType", "PRODUCT")
                .param("entityId", "PROD-123"))
            .andExpect(status().isOk());
    }
}
