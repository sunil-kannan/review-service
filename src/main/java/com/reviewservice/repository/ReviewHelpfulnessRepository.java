package com.reviewservice.repository;

import com.reviewservice.entity.ReviewHelpfulness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewHelpfulnessRepository extends JpaRepository<ReviewHelpfulness, Long> {

    Optional<ReviewHelpfulness> findByReviewIdAndUserId(Long reviewId, String userId);

    boolean existsByReviewIdAndUserId(Long reviewId, String userId);

    void deleteByReviewIdAndUserId(Long reviewId, String userId);
}
