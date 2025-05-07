package com.part3.team07.sb01deokhugamteam07.batch;

import com.part3.team07.sb01deokhugamteam07.entity.Period;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class DateRangeUtil {
  private final Clock clock;

  // 운영용 생성자
  public DateRangeUtil() {
    this.clock = Clock.systemDefaultZone();
  }

  // 테스트용 생성자
  public DateRangeUtil(Clock clock) {
    this.clock = clock;
  }

  public LocalDateTime[] getDateRange(Period period) {
    LocalDateTime now = LocalDateTime.now(clock);

    return switch (period){
      case DAILY -> new LocalDateTime[] {now.minusHours(24), now};
      case WEEKLY -> new LocalDateTime[] {now.minusDays(6), now};
      case MONTHLY -> {
        LocalDate firstDayOfMonth  = now.toLocalDate().withDayOfMonth(1);
        yield new LocalDateTime[] {firstDayOfMonth.atStartOfDay(), now};
      }
      case ALL_TIME -> new LocalDateTime[] { LocalDate.of(1999, 1, 1).atStartOfDay(), now};
    };
  }
}
