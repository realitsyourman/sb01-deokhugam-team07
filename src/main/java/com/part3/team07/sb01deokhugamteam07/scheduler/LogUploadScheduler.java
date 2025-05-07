package com.part3.team07.sb01deokhugamteam07.scheduler;

import com.part3.team07.sb01deokhugamteam07.entity.FileType;
import com.part3.team07.sb01deokhugamteam07.storage.S3Storage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class LogUploadScheduler {

  private final S3Storage s3Storage;

  @Scheduled(cron = "0 0 2 * * *")
  public void uploadDailyLogToS3() {
    String date = LocalDate.now().minusDays(1)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String logPath = System.getProperty("user.dir") + "/.logs/application." + date + ".log";
    File logFile = new File(logPath);

    if (!logFile.exists()) {
      log.warn("업로드할 로그 파일 없음: {}", logPath);
      return;
    }

    try {
      byte[] bytes = Files.readAllBytes(logFile.toPath());
      s3Storage.put(FileType.LOG, "application." + date + ".log", bytes);
    } catch (IOException e) {
      log.error("로그 파일 읽기 실패: {}", logPath, e);
    }
  }
}
