package com.part3.team07.sb01deokhugamteam07.repository;

import com.part3.team07.sb01deokhugamteam07.config.QuerydslConfig;
import com.part3.team07.sb01deokhugamteam07.entity.Book;
import com.part3.team07.sb01deokhugamteam07.entity.Review;
import com.part3.team07.sb01deokhugamteam07.entity.User;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableJpaAuditing
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager em;


    @DisplayName("유저 ID와 책 ID로 리뷰 존재하면 ture를 반환한다.")
    @Test
    void existsByUserIdAndBookId_ExistingReview_ReturnsTrue() {
        //given
        User user = userRepository.save(createTestUser("testUser", "test@example.com"));
        Book book = bookRepository.save(createTestBook("Test Book"));
        Review review = reviewRepository.save(createTestReview(user, book));

        em.flush();
        em.clear();

        //when
        boolean exists = reviewRepository.existsByUserIdAndBookId(user.getId(), book.getId());

        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("없는 유저 ID와 책 ID 조합으로는 리뷰가 존재하지 않는다")
    void existsByUserIdAndBookId_NonExistingReview_ReturnsFalse() {
        // given
        User user = userRepository.save(createTestUser("testUser", "test@example.com"));
        Book book = bookRepository.save(createTestBook("Test Book"));
        //리뷰는 저장하지 않음

        em.flush();
        em.clear();

        // when
        boolean exists = reviewRepository.existsByUserIdAndBookId(user.getId(), book.getId());

        // then
        assertThat(exists).isFalse();
    }

    @DisplayName("해당 책에 있는 모든 리뷰를 조회할 수 있다.")
    @Test
    void findAllByBook() {
        //given
        User user1 = userRepository.save(createTestUser("닉네임1", "user1@abc.com"));
        User user2 = userRepository.save(createTestUser("닉네임2", "user2@abc.com"));
        Book book = bookRepository.save(createTestBook("book"));
        reviewRepository.save(createTestReview(user1, book));
        reviewRepository.save(createTestReview(user2, book));

        //when
        List<Review> reviews = reviewRepository.findAllByBook(book);

        //then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting("user.nickname")
                .containsExactlyInAnyOrder("닉네임1", "닉네임2");
    }

    @DisplayName("유저가 작성한 모든 리뷰를 조회할 수 있다.")
    @Test
    void findAllByUser() {
        //given
        User user = userRepository.save(createTestUser("닉네임", "user1@abc.com"));
        Book book1 = bookRepository.save(createTestBook("book1"));
        Book book2 = bookRepository.save(createTestBook("book2"));
        reviewRepository.save(createTestReview(user, book1));
        reviewRepository.save(createTestReview(user, book2));

        //when
        List<Review> reviews = reviewRepository.findAllByUser(user);

        //then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting("book.title")
                .containsExactlyInAnyOrder("book1", "book2");
    }

    @DisplayName("리뷰의 좋아요 수를 1 증가시킬 수 있다")
    @Test
    void incrementLikeCount() {
        //given
        User user = userRepository.save(createTestUser("user", "user@abc.com"));
        Book book = bookRepository.save(createTestBook("테스트 도서"));
        Review review = reviewRepository.save(createTestReview(user, book));
        UUID reviewId = review.getId();

        em.flush();
        em.clear();

        //when
        reviewRepository.incrementLikeCount(reviewId);
        em.flush();
        em.clear();

        //then
        Review result = reviewRepository.findById(reviewId).orElseThrow();
        assertThat(result.getLikeCount()).isEqualTo(1);
    }

    @DisplayName("리뷰의 좋아요 수가 0이 아닌 경우, 좋아요 수를 1 감소시킬 수 있다.")
    @Test
    void decrementLikeCount() {
        // given
        User user = userRepository.save(createTestUser("user", "user@abc.com"));
        Book book = bookRepository.save(createTestBook("테스트 도서"));
        Review review = reviewRepository.save(createTestReview(user, book));
        UUID reviewId = review.getId();

        // 0인 상태에서 시작하면 에러~, 1증가
        reviewRepository.incrementLikeCount(reviewId);
        em.flush();
        em.clear();

        // when
        reviewRepository.decrementLikeCount(reviewId);
        em.flush();
        em.clear();

        // then
        Review updated = reviewRepository.findById(reviewId).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(0);
    }

    @DisplayName("리뷰의 댓글 수를 1 증가시킬 수 있다")
    @Test
    void incrementCommentCount() {
        // given
        User user = userRepository.save(createTestUser("user", "user@abc.com"));
        Book book = bookRepository.save(createTestBook("테스트 도서"));
        Review review = reviewRepository.save(createTestReview(user, book));
        UUID reviewId = review.getId();

        em.flush();
        em.clear();

        // when
        reviewRepository.incrementCommentCount(reviewId);
        em.flush();
        em.clear();

        // then
        Review updated = reviewRepository.findById(reviewId).orElseThrow();
        assertThat(updated.getCommentCount()).isEqualTo(1);
    }

    @DisplayName("리뷰의 댓글 수가 0이 아닌 경우, 댓글 수를 1 감소시킬 수 있다.")
    @Test
    void decrementCommentCount() {
        // given
        User user = userRepository.save(createTestUser("user", "user@abc.com"));
        Book book = bookRepository.save(createTestBook("테스트 도서"));
        Review review = reviewRepository.save(createTestReview(user, book));
        UUID reviewId = review.getId();

        // 초기값 1 만들어주기
        reviewRepository.incrementCommentCount(reviewId);
        em.flush();
        em.clear();

        // when
        reviewRepository.decrementCommentCount(reviewId);
        em.flush();
        em.clear();

        // then
        Review updated = reviewRepository.findById(reviewId).orElseThrow();
        assertThat(updated.getCommentCount()).isEqualTo(0);
    }

    @DisplayName("findByIdAndIsDeletedFalse - 삭제되지 않은 리뷰 조회 성공")
    @Test
    void findByIdAndIsDeletedFalse_success() {
        // given
        User user = userRepository.save(createTestUser("user", "user@abc.com"));
        Book book = bookRepository.save(createTestBook("테스트 도서"));
        Review review = reviewRepository.save(createTestReview(user, book));
        UUID reviewId = review.getId();

        em.flush();
        em.clear();


        // when
        Optional<Review> result = reviewRepository.findByIdAndIsDeletedFalse(reviewId);
        em.flush();
        em.clear();

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("정말 좋은 책입니다!");
    }

    @DisplayName("findByIdAndIsDeletedFalse - 논리 삭제된 리뷰 조회 실패")
    @Test
    void findByIdAndIsDeletedFalse_deletedReview() {
        // given
        User user = userRepository.save(createTestUser("user", "user@abc.com"));
        Book book = bookRepository.save(createTestBook("테스트 도서"));
        Review review = reviewRepository.save(createTestReview(user, book));
        UUID reviewId = review.getId();

        review.softDelete();
        reviewRepository.save(review);

        em.flush();
        em.clear();

        // when
        Optional<Review> result = reviewRepository.findByIdAndIsDeletedFalse(reviewId);
        em.flush();
        em.clear();

        // then
        assertThat(result).isEmpty();
    }


    private User createTestUser(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password("password123!@#") // 테스트용 비밀번호
                .build();
    }

    private Book createTestBook(String title) {
        return Book.builder()
                .title(title)
                .author("test-author")
                .description("test-description")
                .publisher("test-publisher")
                .publishDate(LocalDate.of(2024, 4, 20))
                .isbn(UUID.randomUUID().toString())
                .thumbnailUrl("test-thumbnail-url")
                .reviewCount(0)
                .rating(BigDecimal.ZERO)
                .build();
    }

    private Review createTestReview(User user, Book book) {
        return Review.builder()
                .user(user)
                .book(book)
                .content("정말 좋은 책입니다!")
                .rating(5)
                .likeCount(0)
                .commentCount(0)
                .build();
    }

}