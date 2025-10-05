package com.reviewservice.repository;

import com.reviewservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByEntityTypeAndEntityIdAndStatus(
        String entityType,
        String entityId,
        String status,
        Pageable pageable
    );

    Page<Review> findByUserIdAndStatus(String userId, String status, Pageable pageable);

    Optional<Review> findByIdAndStatus(Long id, String status);

    boolean existsByEntityTypeAndEntityIdAndUserId(String entityType, String entityId, String userId);

    @Query("SELECT r FROM Review r WHERE r.entityType = :entityType " +
           "AND r.entityId = :entityId AND r.status = :status " +
           "AND (:minRating IS NULL OR r.rating >= :minRating) " +
           "AND (:maxRating IS NULL OR r.rating <= :maxRating)")
    Page<Review> findByEntityAndRatingRange(
        @Param("entityType") String entityType,
        @Param("entityId") String entityId,
        @Param("status") String status,
        @Param("minRating") Integer minRating,
        @Param("maxRating") Integer maxRating,
        Pageable pageable
    );

    @Query("SELECT r FROM Review r WHERE r.entityType = :entityType " +
           "AND r.entityId = :entityId AND r.status = :status " +
           "AND r.verified = true")
    Page<Review> findVerifiedReviews(
        @Param("entityType") String entityType,
        @Param("entityId") String entityId,
        @Param("status") String status,
        Pageable pageable
    );

    List<Review> findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(
        String entityType,
        String entityId,
        String status
    );
}
