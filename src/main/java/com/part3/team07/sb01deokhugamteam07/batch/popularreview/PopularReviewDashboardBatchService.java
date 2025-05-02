package com.part3.team07.sb01deokhugamteam07.batch.popularreview;

import com.part3.team07.sb01deokhugamteam07.batch.AssignRankUtil;
import com.part3.team07.sb01deokhugamteam07.batch.DateRangeUtil;
import com.part3.team07.sb01deokhugamteam07.dto.notification.request.NotificationCreateRequest;
import com.part3.team07.sb01deokhugamteam07.dto.notification.request.NotificationType;
import com.part3.team07.sb01deokhugamteam07.entity.Dashboard;
import com.part3.team07.sb01deokhugamteam07.entity.KeyType;
import com.part3.team07.sb01deokhugamteam07.entity.Period;
import com.part3.team07.sb01deokhugamteam07.entity.Review;
import com.part3.team07.sb01deokhugamteam07.repository.CommentRepository;
import com.part3.team07.sb01deokhugamteam07.repository.DashboardRepository;
import com.part3.team07.sb01deokhugamteam07.repository.LikeRepository;
import com.part3.team07.sb01deokhugamteam07.repository.ReviewRepository;
import com.part3.team07.sb01deokhugamteam07.service.NotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PopularReviewDashboardBatchService {

  private final DateRangeUtil dateRangeUtil;
  private final AssignRankUtil assignRank;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;
  private final LikeRepository likeRepository;
  private final DashboardRepository dashboardRepository;
  private final NotificationService notificationService;

  /**
   * 인기 리뷰 관련 데이터를 dashboard 테이블에 저장합니다.
   *
   * @param period 기간 정보 (e.g. DAILY, WEEKLY, MONTHLY, ALL_TIME)
   **/
  public void savePopularReviewDashboardData(Period period) {
    try {
      log.info("savePopularReviewDashboardData 호출: period={}", period);
      // 1. 전체 리뷰 조회 (is_deleted = false)
      List<Review> reviews = reviewRepository.findByIsDeletedFalseOrderByCreatedAtAsc();
      if (reviews.isEmpty()) {
        log.info("처리할 리뷰가 없습니다. period={}", period);
        return;
      }
      // 날짜 범위 계산
      LocalDate[] dateRange = dateRangeUtil.getDateRange(period);
      LocalDateTime startDateTime = dateRange[0].atStartOfDay();
      LocalDateTime endDateTime = dateRange[1].plusDays(1).atStartOfDay().minusNanos(1);

      // 결과 저장용 대시보드 리스트
      List<Dashboard> dashboards = new ArrayList<>();
      Map<UUID, Double> reviewScoreMap = new LinkedHashMap<>();

      // 2. 각 리뷰의 정보 가져오기
      for (Review review : reviews) {
        UUID reviewId = review.getId();

        // 2-1. 해당 기간 동안 받은 댓글 수
        long commentCount = commentRepository.countByReviewIdAndCreatedAtBetweenAndIsDeletedFalse(
            reviewId,
            startDateTime,
            endDateTime
        );

        // 2-2. 해당 기간 동안 좋아요를 받은 수
        long likeCount = likeRepository.countByReviewIdAndCreatedAtBetween(
            reviewId,
            startDateTime,
            endDateTime
        );

        // 3. 점수 계산
        double score = calculateScore(commentCount, likeCount);
        reviewScoreMap.put(reviewId, score);
      }

      // 4. SCORE 기준으로 전체 리뷰 순위 매기기 -> 정렬 후 rank 지정
      dashboards = assignRank.assignRank(reviewScoreMap, period, KeyType.REVIEW, dashboards);

      // 알림 생성
      List<Dashboard> topTen = dashboards.stream()
          .limit(10)
          .toList();
      for (Dashboard dashboard : topTen) {
        NotificationCreateRequest request = NotificationCreateRequest.builder()
            .type(NotificationType.REVIEW_RANKED)
            .reviewId(dashboard.getKey())
            .period(period)
            .rank(dashboard.getRank())
            .build();
        notificationService.create(request);
      }

      // 5. 데이터베이스에 저장
      dashboardRepository.saveAll(dashboards);
      log.info("대시보드에 인기 리뷰 데이터가 저장됐습니다.: period={}, 저장된 대시보드 개수={}", period, dashboards.size());
    } catch (DataAccessException e) {
      log.error("데이터베이스 접근 중 오류 발생: period={}, error={}", period, e.getMessage(), e);
    } catch (Exception e) {
      log.error("인기 리뷰 대시보드 작업 중 오류 발생: period={}, error={}", period, e.getMessage(), e);
    }
  }

  /**
   * 좋아요 수, 댓글 수를 바탕으로 리뷰의 점수를 계산하는 메서드. 점수 = (해당 기간의 좋아요 수 * 0.3) + (해당 기간의 댓글 수 * 0.7)
   *
   * @param likeCount    리뷰가 받은 좋아요 수
   * @param commentCount 리뷰에 작성된 댓글 수
   * @return 계산된 활동 점수
   **/
  private double calculateScore(long likeCount, long commentCount) {
    return (likeCount * 0.3) + (commentCount * 0.7);
  }


}
