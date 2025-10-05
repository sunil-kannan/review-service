package com.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageMetadataDto {

    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
