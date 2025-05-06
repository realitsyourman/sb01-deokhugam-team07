package com.part3.team07.sb01deokhugamteam07.storage;

import com.part3.team07.sb01deokhugamteam07.entity.FileType;
import com.part3.team07.sb01deokhugamteam07.exception.storage.StorageAlreadyExistsException;
import com.part3.team07.sb01deokhugamteam07.exception.storage.StorageInitException;
import com.part3.team07.sb01deokhugamteam07.exception.storage.StorageSaveFailedException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "local")
@Component
public class LocalStorage implements Storage {

  private final Path root;

  public LocalStorage(
      @Value("${deokhugam.storage.local.root-path}") Path root
  ) {
    this.root = root;
  }

  @PostConstruct
  public void init() {
    if (!Files.exists(root)) {
      try {
        Files.createDirectories(root);
        log.info("스토리지 루트 디렉토리 생성 완료 - 경로: {}", root);
      } catch (IOException e) {
        log.error("스토리지 초기화 실패 - 디렉토리 생성 오류, 경로: {}", root, e);
        throw StorageInitException.withPath(root.toString());
      }
    } else {
      log.info("스토리지 루트 디렉토리가 이미 존재함 - 경로: {}", root);
    }
  }

  @Override
  public String put(FileType type, String fileName, byte[] bytes) {
    Path filePath = resolvePath(type, fileName);
    log.info("파일 저장 시도 - 경로: {}, 파일 유형: {}", filePath, type);

    try {
      Files.createDirectories(filePath.getParent());
      try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
        outputStream.write(bytes);
        log.info("파일 저장 성공 - 경로: {}", filePath);
        return filePath.toString();
      }
    } catch (FileAlreadyExistsException e) {
      log.warn("파일 저장 실패 - 이미 존재하는 파일: {}", filePath);
      throw StorageAlreadyExistsException.withFileName(fileName);
    } catch (IOException e) {
      log.error("파일 저장 실패 - 경로: {}, 오류: {}", filePath, e.getMessage(), e);
      throw StorageSaveFailedException.withFileName(fileName);
    }
  }

  public Path resolvePath(FileType type, String fileName) {
    Path path = root.resolve(getSubDirByType(type)).resolve(fileName);
    log.debug("파일 경로 생성 - 파일 유형: {}, 파일명: {}, 경로: {}", type, fileName, path);
    return path;
  }

  private String getSubDirByType(FileType type) {
    return switch (type) {
      case THUMBNAIL_IMAGE -> "thumbnail";
      case LOG -> "logs";
    };
  }
}
