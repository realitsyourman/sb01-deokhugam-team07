package com.part3.team07.sb01deokhugamteam07.scheduler;

import com.part3.team07.sb01deokhugamteam07.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookReviewScheduler {

  private final BookService bookService;

  @Scheduled(cron = "0 0 * * * *")
  public void updateBookReviews() {
    log.info("도서 리뷰 스탯 업데이트 스케쥴러 작업 시작.");
    try {
      bookService.updateReviewStats();
      log.info("도서 리뷰 스탯 업데이트 완료.");
    } catch (Exception e) {
      log.error("도서 리뷰 스탯 업데이트 중 오류 발생.", e);
    }
  }
}
