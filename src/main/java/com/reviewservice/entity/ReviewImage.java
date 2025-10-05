package com.reviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_images", indexes = {
    @Index(name = "idx_review_image_review_id", columnList = "reviewId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reviewId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Lob
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] imageData;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
