package com.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewservice.dto.ReviewRequest;
import com.reviewservice.dto.ReviewUpdateRequest;
import com.reviewservice.entity.Review;
import com.reviewservice.repository.ReviewImageRepository;
import com.reviewservice.repository.ReviewRepository;
import com.reviewservice.repository.RatingSummaryRepository;
import com.reviewservice.service.ImageStorageService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
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

    @MockBean
    private ImageStorageService imageStorageService;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @BeforeEach
    void setUp() {
        reviewImageRepository.deleteAll();
        reviewRepository.deleteAll();
        ratingSummaryRepository.deleteAll();
    }

    @Test
    void createReview_ValidRequest_ReturnsCreated() throws Exception {
        mockMvc.perform(multipart("/api/v1/reviews")
                        .part(new MockPart("entityType", "PRODUCT".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("entityId", "PROD-123".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("userId", "USER-1".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("rating", "5".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("title", "Excellent Product".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("comment", "Very satisfied with the quality".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entityType").value("PRODUCT"))
                .andExpect(jsonPath("$.entityId").value("PROD-123"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.title").value("Excellent Product"));
    }

    @Test
    void createReview_WithImages_ReturnsCreated() throws Exception {
        MockMultipartFile image1 = new MockMultipartFile(
                "images", "test1.jpg", "image/jpeg", "test image content 1".getBytes());
        MockMultipartFile image2 = new MockMultipartFile(
                "images", "test2.png", "image/png", "test image content 2".getBytes());


        mockMvc.perform(multipart("/api/v1/reviews")
                        .file(image1)
                        .file(image2)
                        .part(new MockPart("entityType", "PRODUCT".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("entityId", "PROD-123".  getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("userId", "USER-1".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("rating", "5".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("title", "Product with images".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isCreated());
//                .andExpect(jsonPath("$.images").isArray())
//                .andExpect(jsonPath("$.images.length()").value(2))
//                .andExpect(jsonPath("$.images[0].fileName").value("test1.jpg"))
//                .andExpect(jsonPath("$.images[1].fileName").value("test2.png"));
    }

    @Test
    void createReview_InvalidRating_ReturnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/reviews")
                        .part(new MockPart("entityType", "PRODUCT".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("entityId", "PROD-123".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("userId", "USER-1".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("rating", "6".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("title", "Test".getBytes(StandardCharsets.UTF_8))))
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

        mockMvc.perform(multipart("/api/v1/reviews")
                        .part(new MockPart("entityType", "PRODUCT".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("entityId", "PROD-123".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("userId", "USER-1".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("rating", "5".getBytes(StandardCharsets.UTF_8)))
                        .part(new MockPart("title", "Test".getBytes(StandardCharsets.UTF_8))))
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
                .build();
        Review savedReview = reviewRepository.save(review);

        MockMultipartFile rating = new MockMultipartFile("rating", "", "text/plain", "5".getBytes());
        MockMultipartFile title = new MockMultipartFile("title", "", "text/plain", "Excellent".getBytes());
        MockMultipartFile comment = new MockMultipartFile("comment", "", "text/plain", "Updated review".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/reviews/{id}", savedReview.getId())
                        .file(rating)
                        .file(title)
                        .file(comment)
                        .param("userId", "USER-1")
                        .with(request -> { request.setMethod("PUT"); return request; }))
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
                .build();

        Review review2 = Review.builder()
                .entityType("PRODUCT")
                .entityId("PROD-123")
                .userId("USER-2")
                .rating(4)
                .status("ACTIVE")
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
