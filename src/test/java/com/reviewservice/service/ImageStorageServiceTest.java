package com.reviewservice.service;

import com.reviewservice.entity.ReviewImage;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.repository.ReviewImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageStorageServiceTest {

    @Mock
    private ReviewImageRepository imageRepository;

    @InjectMocks
    private ImageStorageService imageStorageService;

    private ReviewImage testImage;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        testImage = ReviewImage.builder()
                .id(1L)
                .reviewId(100L)
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .imageData("test image data".getBytes())
                .build();

        mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void storeImage_ValidImage_Success() throws IOException {
        when(imageRepository.save(any(ReviewImage.class))).thenReturn(testImage);

        Long imageId = imageStorageService.storeImage(100L, mockFile);

        assertNotNull(imageId);
        assertEquals(1L, imageId);
        verify(imageRepository, times(1)).save(any(ReviewImage.class));
    }

    @Test
    void storeImage_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () ->
                imageStorageService.storeImage(100L, emptyFile));

        verify(imageRepository, never()).save(any(ReviewImage.class));
    }

    @Test
    void storeImage_InvalidFileType_ThrowsException() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());

        assertThrows(IllegalArgumentException.class, () ->
                imageStorageService.storeImage(100L, invalidFile));

        verify(imageRepository, never()).save(any(ReviewImage.class));
    }

    @Test
    void storeImage_FileTooLarge_ThrowsException() {
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "image", "large.jpg", "image/jpeg", largeContent);

        assertThrows(IllegalArgumentException.class, () ->
                imageStorageService.storeImage(100L, largeFile));

        verify(imageRepository, never()).save(any(ReviewImage.class));
    }

    @Test
    void storeImages_MultipleValid_Success() throws IOException {
        List<MultipartFile> files = List.of(
                new MockMultipartFile("img1", "test1.jpg", "image/jpeg", "content1".getBytes()),
                new MockMultipartFile("img2", "test2.png", "image/png", "content2".getBytes())
        );

        ReviewImage image1 = ReviewImage.builder().id(1L).build();
        ReviewImage image2 = ReviewImage.builder().id(2L).build();

        when(imageRepository.save(any(ReviewImage.class)))
                .thenReturn(image1)
                .thenReturn(image2);

        List<Long> imageIds = imageStorageService.storeImages(100L, files);

        assertNotNull(imageIds);
        assertEquals(2, imageIds.size());
        verify(imageRepository, times(2)).save(any(ReviewImage.class));
    }

    @Test
    void storeImages_TooMany_ThrowsException() {
        List<MultipartFile> files = List.of(
                mockFile, mockFile, mockFile, mockFile, mockFile,
                mockFile, mockFile, mockFile, mockFile, mockFile, mockFile
        );

        assertThrows(IllegalArgumentException.class, () ->
                imageStorageService.storeImages(100L, files));

        verify(imageRepository, never()).save(any(ReviewImage.class));
    }

    @Test
    void getImage_Exists_Success() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        ReviewImage result = imageStorageService.getImage(1L);

        assertNotNull(result);
        assertEquals(testImage.getId(), result.getId());
        assertEquals(testImage.getFileName(), result.getFileName());
        verify(imageRepository, times(1)).findById(1L);
    }

    @Test
    void getImage_NotFound_ThrowsException() {
        when(imageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                imageStorageService.getImage(999L));

        verify(imageRepository, times(1)).findById(999L);
    }

    @Test
    void getImagesByReviewId_Success() {
        List<ReviewImage> images = List.of(testImage);
        when(imageRepository.findByReviewId(100L)).thenReturn(images);

        List<ReviewImage> result = imageStorageService.getImagesByReviewId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(imageRepository, times(1)).findByReviewId(100L);
    }

    @Test
    void deleteImage_Success() {
        imageStorageService.deleteImage(1L);

        verify(imageRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteImagesByReviewId_Success() {
        imageStorageService.deleteImagesByReviewId(100L);

        verify(imageRepository, times(1)).deleteByReviewId(100L);
    }
}
