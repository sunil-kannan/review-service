package com.reviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "rating_summaries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"entityType", "entityId"}),
    indexes = {
        @Index(name = "idx_entity_summary", columnList = "entityType,entityId"),
        @Index(name = "idx_avg_rating", columnList = "averageRating")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    @Column(nullable = false)
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer fiveStarCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer fourStarCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer threeStarCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer twoStarCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer oneStarCount = 0;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
