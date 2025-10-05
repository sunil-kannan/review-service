package com.reviewservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_helpfulness",
    uniqueConstraints = @UniqueConstraint(columnNames = {"reviewId", "userId"}),
    indexes = {
        @Index(name = "idx_review_id", columnList = "reviewId"),
        @Index(name = "idx_user_id_helpfulness", columnList = "userId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHelpfulness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reviewId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Boolean helpful;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
