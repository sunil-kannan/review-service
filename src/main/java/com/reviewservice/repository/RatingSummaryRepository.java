package com.reviewservice.repository;

import com.reviewservice.entity.RatingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingSummaryRepository extends JpaRepository<RatingSummary, Long> {

    Optional<RatingSummary> findByEntityTypeAndEntityId(String entityType, String entityId);
}
