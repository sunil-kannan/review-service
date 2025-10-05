package com.reviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_entity_type_id", columnList = "entityType,entityId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_rating", columnList = "rating"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 100)
    private String title;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer unhelpfulCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime moderatedAt;

    @Column
    private String moderatorId;

    @Column(length = 500)
    private String moderationNote;
}
