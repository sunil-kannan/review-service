package com.reviewservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotBlank(message = "Entity type is required")
    @Size(max = 50, message = "Entity type must not exceed 50 characters")
    private String entityType;

    @NotBlank(message = "Entity ID is required")
    @Size(max = 100, message = "Entity ID must not exceed 100 characters")
    private String entityId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;
}
