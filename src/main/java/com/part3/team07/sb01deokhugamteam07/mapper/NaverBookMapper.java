package com.part3.team07.sb01deokhugamteam07.mapper;

import com.part3.team07.sb01deokhugamteam07.client.dto.NaverBookRssResponse;
import com.part3.team07.sb01deokhugamteam07.dto.book.NaverBookDto;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NaverBookMapper {

  public NaverBookDto toDto(NaverBookRssResponse.Item item) {
    return new NaverBookDto(
        item.getTitle(),
        item.getAuthor(),
        item.getDescription(),
        item.getPublisher(),
        parsePubDate(item.getPubdate()),
        extractIsbn13(item.getIsbn()),
        downloadThumbnail(item.getImage())
    );
  }

  private LocalDate parsePubDate(String pubdate) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    try {
      return LocalDate.parse(pubdate, formatter);
    } catch (Exception e) {
      log.warn("pubdate 파싱 실패: {}. Returning null.", pubdate, e);
      return null;
    }
  }

  private String extractIsbn13(String isbn) {
    return Arrays.stream(isbn.split(" "))
        .filter(s -> s.length() == 13)
        .findFirst()
        .orElseGet(() -> {
          log.warn("ISBN-13 추출 실패: {}", isbn);
          return isbn;
        });
  }

  private byte[] downloadThumbnail(String url) {
    try (InputStream in = new URL(url).openStream()) {
      return in.readAllBytes();
    } catch (IOException e) {
      log.warn("thumbnail 다운로드 실패 URL: {}", url, e);
      return null;
    }
  }
}
