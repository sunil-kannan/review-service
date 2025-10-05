package com.reviewservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseRequest {

    @NotBlank(message = "Responder ID is required")
    private String responderId;

    @NotBlank(message = "Responder type is required")
    private String responderType;

    @NotBlank(message = "Response is required")
    @Size(max = 1000, message = "Response must not exceed 1000 characters")
    private String response;
}
