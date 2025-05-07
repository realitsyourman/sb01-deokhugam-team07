package com.part3.team07.sb01deokhugamteam07.service;

import com.part3.team07.sb01deokhugamteam07.entity.FileType;
import com.part3.team07.sb01deokhugamteam07.exception.storage.StorageSaveFailedException;
import com.part3.team07.sb01deokhugamteam07.storage.Storage;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

  private final Storage storage;

  public String save(MultipartFile thumbnailImage, FileType fileType) {
    String fileName = generateFileName(thumbnailImage);
    try {
      String fileUrl  = storage.put(fileType, fileName, thumbnailImage.getBytes());
      log.info("파일 저장 성공 - 파일명: {}, 파일 유형: {}", fileName, fileType);
      return fileUrl;
    } catch (IOException e) {
      log.error("파일 저장 실패 - 파일명: {}, 오류: {}", fileName, e.getMessage(), e);
      throw StorageSaveFailedException.withFileName(fileName);
    }
  }

  private String generateFileName(MultipartFile file) {
    String originalFileName = file.getOriginalFilename();
    String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
    String fileName = UUID.randomUUID().toString() + extension;

    log.debug("랜덤 파일명 생성 - 원본 파일명: {}, 생성된 파일명: {}", originalFileName, fileName);

    return fileName;
  }
}
