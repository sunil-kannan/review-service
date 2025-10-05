package com.reviewservice.controller;

import com.reviewservice.dto.RatingSummaryDto;
import com.reviewservice.service.RatingSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
@Tag(name = "Rating Summary", description = "APIs for retrieving rating summaries and statistics")
public class RatingSummaryController {

    private final RatingSummaryService ratingSummaryService;

    @GetMapping
    @Operation(summary = "Get rating summary", description = "Retrieve rating summary for an entity")
    public ResponseEntity<RatingSummaryDto> getRatingSummary(
        @RequestParam @Parameter(description = "Entity type") String entityType,
        @RequestParam @Parameter(description = "Entity ID") String entityId
    ) {
        RatingSummaryDto summary = ratingSummaryService.getRatingSummary(entityType, entityId);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh rating summary", description = "Manually refresh rating summary for an entity")
    public ResponseEntity<Void> refreshRatingSummary(
        @RequestParam @Parameter(description = "Entity type") String entityType,
        @RequestParam @Parameter(description = "Entity ID") String entityId
    ) {
        ratingSummaryService.updateRatingSummary(entityType, entityId);
        return ResponseEntity.ok().build();
    }
}
