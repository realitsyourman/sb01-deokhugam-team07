package com.part3.team07.sb01deokhugamteam07.batch;

import static org.junit.jupiter.api.Assertions.*;

import com.part3.team07.sb01deokhugamteam07.entity.Period;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateRangeUtilTest {

  // 2025 년 4월 16일 00:00 (한국 시간) < 수요일
  Clock fixedClock = Clock.fixed(
      LocalDate.of(2025, 4, 16) // 날짜 객체
          .atStartOfDay(ZoneId.systemDefault()) // 하루 시작 시간과 시간대 설정
          .toInstant(), // UTC Instant 변환
      ZoneId.systemDefault() // 고정 시게의 시간대
  );

  @Test
  @DisplayName("DAILY 기준 날짜 범위가 정확히 계산되는지 확인")
  void getDateRange_shouldReturnCorrectRange_forDaily() {
    DateRangeUtil util = new DateRangeUtil(fixedClock);
    LocalDateTime[] range = util.getDateRange(Period.DAILY);

    assertEquals(LocalDateTime.of(2025, 4, 15, 0, 0), range[0]);
    assertEquals(LocalDateTime.of(2025, 4, 16, 0, 0), range[1]);
  }

  @Test
  @DisplayName("WEEKLY 기준 날짜 범위가 정확히 계산되는지 확인")
  void getDateRage_shouldReturnCorrectRange_forWeekly() {
    DateRangeUtil util = new DateRangeUtil(fixedClock);
    LocalDateTime[] range = util.getDateRange(Period.WEEKLY);

    assertEquals(LocalDateTime.of(2025, 4, 10, 0, 0), range[0]); // 6일 전
    assertEquals(LocalDateTime.of(2025, 4, 16, 0, 0), range[1]);
  }

  @Test
  @DisplayName("MONTHLY  기준 날짜 범위가 정확히 계산되는지 확인")
  void getDateRage_shouldReturnCorrectRange_forMonthly() {
    DateRangeUtil util = new DateRangeUtil(fixedClock);
    LocalDateTime[] range = util.getDateRange(Period.WEEKLY);

    assertEquals(LocalDateTime.of(2025, 4, 10, 0, 0), range[0]); // 6일 전
    assertEquals(LocalDateTime.of(2025, 4, 16, 0, 0), range[1]);
  }

  @Test
  @DisplayName("ALL_TIME 기준 날짜 범위가 정확히 계산되는지 확인")
  void getDateRage_shouldReturnCorrectRange_AllTime() {
    DateRangeUtil util = new DateRangeUtil(fixedClock);
    LocalDateTime[] range = util.getDateRange(Period.ALL_TIME);

    assertEquals(LocalDateTime.of(1999, 1, 1, 0, 0), range[0]);
    assertEquals(LocalDateTime.of(2025, 4, 16, 0, 0), range[1]);
  }
}