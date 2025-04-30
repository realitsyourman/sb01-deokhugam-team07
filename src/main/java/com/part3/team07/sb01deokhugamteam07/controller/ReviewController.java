package com.part3.team07.sb01deokhugamteam07.controller;


import com.part3.team07.sb01deokhugamteam07.dto.review.ReviewDto;
import com.part3.team07.sb01deokhugamteam07.dto.review.ReviewLikeDto;
import com.part3.team07.sb01deokhugamteam07.dto.review.request.ReviewCreateRequest;
import com.part3.team07.sb01deokhugamteam07.dto.review.request.ReviewUpdateRequest;
import com.part3.team07.sb01deokhugamteam07.dto.review.response.CursorPageResponsePopularReviewDto;
import com.part3.team07.sb01deokhugamteam07.entity.Period;
import com.part3.team07.sb01deokhugamteam07.service.DashboardService;
import com.part3.team07.sb01deokhugamteam07.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final DashboardService dashboardService;

    @PostMapping
    public ResponseEntity<ReviewDto> create(@RequestBody @Validated ReviewCreateRequest request) {
        log.info("리뷰 생성 요청: {}", request);
        ReviewDto createdReview = reviewService.create(request);
        log.debug("리뷰 생성 응답: {}", createdReview);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReview);
    }

    @GetMapping("{reviewId}")
    public ResponseEntity<ReviewDto> find(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        log.info("리뷰 상세 조회 요청: {}", reviewId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewService.find(reviewId, userId));
    }

    @PatchMapping("{reviewId}")
    public ResponseEntity<ReviewDto> update(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @RequestBody @Valid ReviewUpdateRequest request) {
        log.info("리뷰 수정 요청: {}", reviewId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewService.update(userId, reviewId, request));
    }

    @DeleteMapping("{reviewId}")
    public ResponseEntity<Void> softDelete(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        log.info("리뷰 논리 삭제 요청: {}", reviewId);
        reviewService.softDelete(userId, reviewId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @DeleteMapping({"/{reviewId}/hard"})
    public ResponseEntity<Void> hardDelete(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        log.info("리뷰 물리 삭제 요청: {}", reviewId);
        reviewService.hardDelete(userId, reviewId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> toggleLike(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        log.info("좋아요 등록, 취소 요청: {}", reviewId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewService.toggleLike(reviewId, userId));
    }

    @GetMapping("/popular")
    public ResponseEntity<CursorPageResponsePopularReviewDto> findPopularReviews(
        @RequestParam Period period,
        @RequestParam(required = false, defaultValue = "asc") @Pattern(regexp = "(?i)ASC|DESC", message = "direction은 ASC 또는 DESC만 가능합니다.") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) String after,
        @RequestParam(required = false, defaultValue = "50") @Min(1) int limit
    ){
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(dashboardService.getPopularReviews(period, direction, cursor, after, limit));
    }

}
