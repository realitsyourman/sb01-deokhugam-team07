package com.part3.team07.sb01deokhugamteam07.storage;

import com.part3.team07.sb01deokhugamteam07.entity.FileType;
import com.part3.team07.sb01deokhugamteam07.exception.storage.StorageAlreadyExistsException;
import com.part3.team07.sb01deokhugamteam07.exception.storage.StorageSaveFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
@Component
@RequiredArgsConstructor
public class S3Storage implements Storage {

  private final S3Client s3Client;

  @Value("${deokhugam.storage.s3.bucket}")
  private String bucketName;

  @Value("${deokhugam.storage.s3.region}")
  private String region;

  @Override
  public String put(FileType type, String fileName, byte[] bytes) {
    String key = resolvePath(type, fileName);
    log.info("S3 파일 업로드 시도 - key: {}, 타입: {}", key, type);

    if (type != FileType.LOG && exists(key)) {
      log.warn("S3 업로드 실패 - 이미 존재하는 파일: {}", key);
      throw StorageAlreadyExistsException.withFileName(fileName);
    }

    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      s3Client.putObject(request, RequestBody.fromBytes(bytes));
      log.info("S3 파일 업로드 성공 - key: {}", key);

      if (type == FileType.THUMBNAIL_IMAGE) {
        String url = getObjectUrl(key);
        log.debug("S3 썸네일 이미지 URL 반환 - {}", url);
        return url;
      }
      return null;
    } catch (AwsServiceException e) {
      log.error("S3 업로드 실패 - key: {}, 오류: {}", key, e.getMessage(), e);
      throw StorageSaveFailedException.withFileName(fileName);
    }
  }

  private boolean exists(String key) {
    try {
      s3Client.headObject(
          HeadObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .build()
      );
      log.debug("S3 객체 존재 확인 - 존재함: {}", key);
      return true;
    } catch (NoSuchKeyException e) {
      log.debug("S3 객체 존재 확인 - 존재하지 않음: {}", key);
      return false;
    } catch (AwsServiceException e) {
      log.error("S3 객체 존재 확인 실패 - key: {}, 오류: {}", key, e.getMessage(), e);
      throw StorageSaveFailedException.withFileName(key);
    }
  }

  public String resolvePath(FileType type, String fileName) {
    String path = getSubDirByType(type) + "/" + fileName;
    log.debug("S3 저장 경로 생성 - 타입: {}, 파일명: {}, 경로: {}", type, fileName, path);
    return path;
  }

  private String getSubDirByType(FileType type) {
    return switch (type) {
      case THUMBNAIL_IMAGE -> "thumbnail";
      case LOG -> "logs";
    };
  }

  private String getObjectUrl(String key) {
    String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    log.debug("S3 객체 URL 생성 - {}", url);
    return url;
  }
}
