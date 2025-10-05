package com.reviewservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpfulnessRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Helpful flag is required")
    private Boolean helpful;
}
