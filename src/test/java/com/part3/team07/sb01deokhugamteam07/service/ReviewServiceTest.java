package com.part3.team07.sb01deokhugamteam07.service;

import com.part3.team07.sb01deokhugamteam07.dto.review.ReviewDto;
import com.part3.team07.sb01deokhugamteam07.dto.review.ReviewLikeDto;
import com.part3.team07.sb01deokhugamteam07.dto.review.request.ReviewCreateRequest;
import com.part3.team07.sb01deokhugamteam07.dto.review.request.ReviewUpdateRequest;
import com.part3.team07.sb01deokhugamteam07.dto.review.response.CursorPageResponseReviewDto;
import com.part3.team07.sb01deokhugamteam07.entity.*;
import com.part3.team07.sb01deokhugamteam07.exception.book.BookNotFoundException;
import com.part3.team07.sb01deokhugamteam07.exception.review.ReviewAlreadyExistsException;
import com.part3.team07.sb01deokhugamteam07.exception.review.ReviewNotFoundException;
import com.part3.team07.sb01deokhugamteam07.exception.review.ReviewUnauthorizedException;
import com.part3.team07.sb01deokhugamteam07.exception.user.UserNotFoundException;
import com.part3.team07.sb01deokhugamteam07.repository.BookRepository;
import com.part3.team07.sb01deokhugamteam07.repository.LikeRepository;
import com.part3.team07.sb01deokhugamteam07.repository.ReviewRepository;
import com.part3.team07.sb01deokhugamteam07.repository.UserRepository;
import java.math.BigDecimal;

import com.part3.team07.sb01deokhugamteam07.type.ReviewDirection;
import com.part3.team07.sb01deokhugamteam07.type.ReviewOrderBy;
import com.querydsl.core.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentService commentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    private UUID userId;
    private UUID bookId;
    private UUID reviewId;
    private User user;
    private Book book;
    private Review review;
    private Like like;
    private ReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        user = User.builder()
                .nickname("User")
                .email("user@abc.com")
                .password("user1234")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        book = Book.builder()
                .title("Book")
                .author("Author")
                .description("book_book")
                .publisher("Publisher")
                .publishDate(LocalDate.now())
                .isbn(UUID.randomUUID().toString())
                .thumbnailUrl("url")
                .reviewCount(0)
                .rating(BigDecimal.ZERO)
                .build();
        ReflectionTestUtils.setField(book, "id", bookId);

        review = Review.builder()
                .user(user)
                .book(book)
                .content("content")
                .rating(5)
                .likeCount(0)
                .commentCount(0)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);

        like = Like.builder()
                .userId(userId)
                .reviewId(reviewId)
                .build();


        reviewDto = new ReviewDto(
                reviewId,
                bookId,
                book.getTitle(),
                book.getThumbnailUrl(),
                userId,
                user.getNickname(),
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                false, // likeByMe 기본 false
                null, // createdAt
                null  // updatedAt
        );
    }


    @DisplayName("도서 별 1개의 리뷰만 등록할 수 있다.")
    @Test
    void review_crate_success() {
        //given
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId,"content", 5);

        given(reviewRepository.existsByUserIdAndBookId(userId, bookId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", reviewId);
            return saved;
        });

        //when
        ReviewDto result = reviewService.create(request);

        //then
        assertThat(result).isEqualTo(reviewDto);
        verify(reviewRepository).save(any(Review.class));
    }

    @DisplayName("이미 리뷰가 존재하는 경우 생성에 실패한다")
    @Test
    void createReview_Fail_DuplicateReview() {
        //given
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "리뷰 내용", 5);

        given(reviewRepository.existsByUserIdAndBookId(userId, bookId)).willReturn(true);

        //when then
        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(ReviewAlreadyExistsException.class);
    }

    @DisplayName("존재하지 않는 유저로 리뷰 생성 시 실패한다")
    @Test
    void createReview_Fail_UserNotFound() {
        //given
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "리뷰 내용", 5);

        given(reviewRepository.existsByUserIdAndBookId(userId, bookId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @DisplayName("존재하지 않는 책으로 리뷰 생성 시 실패한다")
    @Test
    void createReview_Fail_BookNotFound() {
        //given
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "리뷰 내용", 5);

        given(reviewRepository.existsByUserIdAndBookId(userId, bookId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(BookNotFoundException.class);
    }

    @DisplayName("리뷰 Id로 리뷰 상세조회를 할 수 있다.")
    @Test
    void find() {
        //given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

        //when
        ReviewDto result = reviewService.find(reviewId, userId);

        //then
        assertThat(result).isEqualTo(reviewDto);
    }

    @DisplayName("리뷰 조회 시, 사용자가 좋아요를 눌렀으면 likeByMe가 true로 설정된다.")
    @Test
    void find_likeByMe_true() {
        // given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(likeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);

        // when
        ReviewDto result = reviewService.find(reviewId, userId);

        // then
        assertThat(result.likeByMe()).isTrue();
    }

    @DisplayName("리뷰 Id로 조회 시 존재하지 않으면 예외가 발생한다.")
    @Test
    void find_ReviewNotFound() {
        //given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> reviewService.find(reviewId, userId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @DisplayName("본인이 작성한 리뷰를 수정할 수 있다.")
    @Test
    void update() {
        //given
        ReviewUpdateRequest request = new ReviewUpdateRequest("수정한 내용", 3);
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

        //when
        ReviewDto result = reviewService.update(userId, reviewId, request);

        //then
        assertThat(result.content()).isEqualTo("수정한 내용");
        assertThat(result.rating()).isEqualTo(3);
    }

    @DisplayName("본인이 작성하지 않은 리뷰는 수정할 수 없다.")
    @Test
    void updateReview_ShouldFail_WhenUserIsNotAuthor() {
        //given
        UUID otherUserId = UUID.randomUUID();
        ReviewUpdateRequest request = new ReviewUpdateRequest("수정한 내용", 3);
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

        //when then
        assertThatThrownBy(()-> reviewService.update(otherUserId, reviewId, request))
                .isInstanceOf(ReviewUnauthorizedException.class);
    }

    @DisplayName("존재하지 않은 리뷰는 수정할 수 없다")
    @Test
    void updateReview_ShouldFail_WhenReviewDoesNotExist() {
        //given
        UUID otherReviewId = UUID.randomUUID();
        ReviewUpdateRequest request = new ReviewUpdateRequest("수정한 내용", 3);

        //when then
        assertThatThrownBy(()-> reviewService.update(userId, otherReviewId, request))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @DisplayName("리뷰 논리 삭제를 할 수 있다.")
    @Test
    void softDelete() {
        //given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(likeRepository.findAllByReviewId(reviewId)).willReturn(List.of(like));

        //when
        reviewService.softDelete(userId, reviewId);

        //then
        assertThat(review.isDeleted()).isTrue();
        verify(likeRepository).findAllByReviewId(reviewId);
    }

    @DisplayName("존재하지 않은 리뷰는 논리 삭제할 수 없다.")
    @Test
    void softDelete_fail_whenUserIsNotAuthor() {
        //given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> reviewService.softDelete(userId, reviewId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @DisplayName("본인이 작성하지 않은 리뷰는 논리 삭제할 수 없다.")
    @Test
    void softDelete_fail_whenReviewNotFound() {
        //given
        UUID otherUserId = UUID.randomUUID();
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

        //when then
        assertThatThrownBy(() -> reviewService.softDelete(otherUserId, reviewId))
                .isInstanceOf(ReviewUnauthorizedException.class);
    }

    @DisplayName("book 기준으로 모든 리뷰를 논리 삭제한다.")
    @Test
    void softDeleteAllByBook() {
        // given
        Review review1 = Review.builder()
                .user(user)
                .book(book)
                .content("리뷰1")
                .rating(4)
                .likeCount(0)
                .commentCount(0)
                .build();
        Review review2 = Review.builder()
                .user(user)
                .book(book)
                .content("리뷰2")
                .rating(5)
                .likeCount(0)
                .commentCount(0)
                .build();
        given(reviewRepository.findAllByBook(book)).willReturn(List.of(review1, review2));

        // when
        reviewService.softDeleteAllByBook(book);

        // then
        assertThat(review1.isDeleted()).isTrue();
        assertThat(review2.isDeleted()).isTrue();
    }

    @DisplayName("user 기준으로 모든 리뷰를 논리 삭제한다.")
    @Test
    void softDeleteAllByUser() {
        // given
        Review review1 = Review.builder()
                .user(user)
                .book(book)
                .content("리뷰1")
                .rating(4)
                .likeCount(0)
                .commentCount(0)
                .build();
        Review review2 = Review.builder()
                .user(user)
                .book(book)
                .content("리뷰2")
                .rating(5)
                .likeCount(0)
                .commentCount(0)
                .build();
        given(reviewRepository.findAllByUser(user)).willReturn(List.of(review1, review2));

        // when
        reviewService.softDeleteAllByUser(user);

        // then
        assertThat(review1.isDeleted()).isTrue();
        assertThat(review2.isDeleted()).isTrue();
    }


    @DisplayName("리뷰를 물리 삭제할 수 있다.")
    @Test
    void hardDelete() {
        //given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

        //when
        reviewService.hardDelete(userId, reviewId);

        //then
        verify(reviewRepository).delete(review);
    }

    @DisplayName("리뷰 물리 삭제 실패 - 작성자가 아닐 경우 예외 발생")
    @Test
    void hardDelete_Failure_Unauthorized() {
        //given
        UUID otherUserId = UUID.randomUUID();
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));

        //when then
        assertThatThrownBy(() -> reviewService.hardDelete(otherUserId, reviewId))
                .isInstanceOf(ReviewUnauthorizedException.class);
    }

    @DisplayName("좋아요가 존재하지 않으면 추가한다")
    @Test
    void toggleLike_shouldAddLikeWhenNotExists() {
        //given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(likeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.empty());

        //when
        ReviewLikeDto result = reviewService.toggleLike(reviewId, userId);

        //then
        assertThat(result.liked()).isTrue();
        verify(reviewRepository).incrementLikeCount(reviewId);
    }

    @Test
    @DisplayName("좋아요가 존재하면 삭제한다")
    void toggleLike_shouldCancelLikeWhenExists() {
        // given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(likeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.of(like));

        // when
        ReviewLikeDto result = reviewService.toggleLike(reviewId, userId);

        // then
        assertThat(result.liked()).isFalse();
        verify(reviewRepository).decrementLikeCount(reviewId);
    }

    @Test
    @DisplayName("좋아요 추가,해제 시 리뷰가 존재하지 않으면 예외가 발생한다")
    void toggleLike_shouldThrowIfReviewNotFound() {
        // given
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.toggleLike(reviewId, userId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @DisplayName("리뷰 목록 조회 가능하다.")
    @Test
    void findAllReviews_success() {
        // given
        String keyword = null;
        String cursor = null;
        LocalDateTime after = null;
        int limit = 20;
        ReviewOrderBy orderBy = ReviewOrderBy.CREATED_AT;
        ReviewDirection direction = ReviewDirection.DESC;

        Tuple tuple = mock(Tuple.class);
        given(tuple.get(QReview.review)).willReturn(review);
        given(tuple.get(QLike.like)).willReturn(like);

        List<Tuple> mockResult = List.of(tuple);
        given(reviewRepository.findAll(userId, bookId, keyword, orderBy, direction, cursor, after, limit + 1, userId))
                .willReturn(mockResult);

        // when
        CursorPageResponseReviewDto response = reviewService.findAll(userId, bookId, keyword, orderBy, direction, cursor, after, limit, userId);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(reviewId);
        assertThat(response.content().get(0).likeByMe()).isTrue();

        verify(reviewRepository).findAll(any(), any(), any(), any(), any(), any(), any(), anyInt(), any());
    }

    @DisplayName("필터 조건과 함께 리뷰 목록을 조회할 수 있다.")
    @Test
    void findAll_withFilters_success() {
        // given
        String keyword = "좋아요";
        String cursor = UUID.randomUUID().toString();
        LocalDateTime after = LocalDateTime.now().minusDays(1);
        int limit = 10;

        Tuple tuple = mock(Tuple.class);
        given(tuple.get(QReview.review)).willReturn(review);
        given(tuple.get(QLike.like)).willReturn(like);

        given(reviewRepository.findAll(userId, bookId, keyword, ReviewOrderBy.CREATED_AT, ReviewDirection.DESC, cursor, after, limit + 1, userId))
                .willReturn(List.of(tuple));

        // when
        CursorPageResponseReviewDto result = reviewService.findAll(userId, bookId, keyword, ReviewOrderBy.CREATED_AT, ReviewDirection.DESC, cursor, after, limit, userId);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.content().get(0).likeByMe()).isTrue();
    }

    @DisplayName("limit보다 많은 결과가 조회되면 hasNext는 true이고 커서가 설정된다.")
    @Test
    void findAll_hasNext_and_cursorSet() {
        // given
        int limit = 2;

        // 튜플 3개 준비 (limit + 1)
        Tuple t1 = mock(Tuple.class);
        Tuple t2 = mock(Tuple.class);
        Tuple t3 = mock(Tuple.class);

        Review r1 = Review.builder()
                .user(user)
                .book(book)
                .content("c1")
                .rating(3)
                .build();
        Review r2 = Review.builder()
                .user(user)
                .book(book)
                .content("c2")
                .rating(4)
                .build();

        LocalDateTime created1 = LocalDateTime.now().minusMinutes(10);
        LocalDateTime created2 = LocalDateTime.now().minusMinutes(5);

        ReflectionTestUtils.setField(r1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(r2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(r1, "createdAt", created1);
        ReflectionTestUtils.setField(r2, "createdAt", created2);

        given(t1.get(QReview.review)).willReturn(r1);
        given(t1.get(QLike.like)).willReturn(like);
        given(t2.get(QReview.review)).willReturn(r2);
        given(t2.get(QLike.like)).willReturn(null);

        given(reviewRepository.findAll(
                any(), any(), any(), any(), any(),
                any(), any(), anyInt(), any()
        )).willReturn(List.of(t1, t2, t3)); // limit + 1개

        // when
        CursorPageResponseReviewDto result = reviewService.findAll(
                null, null, null,
                ReviewOrderBy.CREATED_AT, ReviewDirection.DESC,
                null, null, limit, userId
        );

        // then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(limit);
        assertThat(result.nextCursor()).isEqualTo(created2.toString());
    }
}