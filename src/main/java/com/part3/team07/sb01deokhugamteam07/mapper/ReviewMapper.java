package com.part3.team07.sb01deokhugamteam07.mapper;

import com.part3.team07.sb01deokhugamteam07.dto.review.ReviewDto;
import com.part3.team07.sb01deokhugamteam07.entity.Review;

public class ReviewMapper {

    public static ReviewDto toDto(Review review, boolean likeByMe) {
        return new ReviewDto(
                review.getId(),
                review.getBook().getId(),
                review.getBook().getTitle(),
                review.getBook().getThumbnailUrl(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                likeByMe,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
