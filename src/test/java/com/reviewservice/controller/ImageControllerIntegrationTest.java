package com.reviewservice.controller;

import com.reviewservice.entity.ReviewImage;
import com.reviewservice.repository.ReviewImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    private ReviewImage testImage;

    @BeforeEach
    void setUp() {
        reviewImageRepository.deleteAll();

        testImage = ReviewImage.builder()
                .reviewId(1L)
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .imageData("test image content".getBytes())
                .build();
        testImage = reviewImageRepository.save(testImage);
    }

    @Test
    void getImage_ExistingImage_ReturnsImage() throws Exception {
        mockMvc.perform(get("/api/v1/images/" + testImage.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes("test image content".getBytes()));
    }

    @Test
    void getImage_NonExistingImage_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/images/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImage_ExistingImage_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/images/" + testImage.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteImage_NonExistingImage_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/images/999"))
                .andExpect(status().isNoContent());
    }
}
