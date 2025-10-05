package com.reviewservice.service;

import com.reviewservice.entity.ReviewImage;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.repository.ReviewImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageService {

    private final ReviewImageRepository imageRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp"
    );

    @Transactional
    public Long storeImage(Long reviewId, MultipartFile file) throws IOException {
        log.info("Storing image for review ID: {}", reviewId);

        validateImage(file);

        ReviewImage image = ReviewImage.builder()
            .reviewId(reviewId)
            .fileName(file.getOriginalFilename())
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .imageData(file.getBytes())
            .build();

        ReviewImage savedImage = imageRepository.save(image);
        log.info("Image stored successfully with ID: {}", savedImage.getId());

        return savedImage.getId();
    }

    @Transactional
    public List<Long> storeImages(Long reviewId, List<MultipartFile> files) throws IOException {
        log.info("Storing {} images for review ID: {}", files.size(), reviewId);

        if (files.size() > 10) {
            throw new IllegalArgumentException("Maximum 10 images allowed per review");
        }

        return files.stream()
            .map(file -> {
                try {
                    return storeImage(reviewId, file);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store image: " + file.getOriginalFilename(), e);
                }
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public ReviewImage getImage(Long imageId) {
        return imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    }

    @Transactional(readOnly = true)
    public List<ReviewImage> getImagesByReviewId(Long reviewId) {
        return imageRepository.findByReviewId(reviewId);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image ID: {}", imageId);
        imageRepository.deleteById(imageId);
    }

    @Transactional
    public void deleteImagesByReviewId(Long reviewId) {
        log.info("Deleting all images for review ID: {}", reviewId);
        imageRepository.deleteByReviewId(reviewId);
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: jpg, jpeg, png, gif, webp");
        }
    }
}
