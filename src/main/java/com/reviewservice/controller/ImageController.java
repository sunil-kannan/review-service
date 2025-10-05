package com.reviewservice.controller;

import com.reviewservice.entity.ReviewImage;
import com.reviewservice.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image Management", description = "APIs for retrieving review images")
public class ImageController {

    private final ImageStorageService imageStorageService;

    @GetMapping("/{imageId}")
    @Operation(summary = "Get image by ID", description = "Retrieve an image file by its ID")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        ReviewImage image = imageStorageService.getImage(imageId);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + image.getFileName() + "\"")
            .body(image.getImageData());
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete image", description = "Delete an image by ID")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        imageStorageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
