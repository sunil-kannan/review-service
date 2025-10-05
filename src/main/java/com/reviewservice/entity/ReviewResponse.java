package com.reviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_responses", indexes = {
    @Index(name = "idx_review_id_response", columnList = "reviewId"),
    @Index(name = "idx_responder_id", columnList = "responderId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reviewId;

    @Column(nullable = false)
    private String responderId;

    @Column(nullable = false)
    private String responderType;

    @Column(length = 1000, nullable = false)
    private String response;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
