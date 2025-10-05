package com.reviewservice.repository;

import com.reviewservice.entity.ReviewResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewResponseRepository extends JpaRepository<ReviewResponse, Long> {

    List<ReviewResponse> findByReviewIdOrderByCreatedAtDesc(Long reviewId);

    List<ReviewResponse> findByResponderIdOrderByCreatedAtDesc(String responderId);
}
