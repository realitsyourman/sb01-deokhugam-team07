CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP           NOT NULL,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN             NOT NULL DEFAULT FALSE,
    email      VARCHAR(255) UNIQUE NOT NULL,
    nickname   VARCHAR(20)         NOT NULL,
    password   VARCHAR(255)         NOT NULL
);

CREATE TABLE books
(
    id                  UUID PRIMARY KEY,
    created_at          TIMESTAMP     NOT NULL,
    updated_at          TIMESTAMP,
    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
    title               VARCHAR(255)  NOT NULL,
    author              VARCHAR(255)  NOT NULL,
    description         TEXT          NOT NULL,
    publisher           VARCHAR(255)  NOT NULL,
    publish_date        DATE          NOT NULL,
    isbn                VARCHAR(255) UNIQUE,
    thumbnail_url TEXT,
    review_count        INTEGER       NOT NULL DEFAULT 0,
    rating              DECIMAL(2, 1) NOT NULL DEFAULT 0.0
);

CREATE TABLE reviews
(
    id            UUID PRIMARY KEY,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP,
    is_deleted    BOOLEAN   NOT NULL DEFAULT FALSE,
    user_id       UUID      NOT NULL,
    book_id       UUID      NOT NULL,
    content       TEXT      NOT NULL,
    rating        INTEGER   NOT NULL,
    like_count    INTEGER   NOT NULL,
    comment_count INTEGER   NOT NULL,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES
        users (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES
        books (id) ON DELETE CASCADE
);

CREATE TABLE comments
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN   NOT NULL DEFAULT FALSE,
    user_id    UUID      NOT NULL,
    review_id  UUID      NOT NULL,
    content    TEXT      NOT NULL,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES
        users (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_review FOREIGN KEY (review_id) REFERENCES
        reviews (id) ON DELETE CASCADE
);

CREATE TABLE notifications
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP,
    user_id    UUID         NOT NULL,
    review_id  UUID         NOT NULL,
    content    VARCHAR(255) NOT NULL,
    confirmed  BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE likes
(
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    review_id  UUID NOT NULL,
    is_deleted BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT unique_review_like UNIQUE (user_id, review_id)
);

CREATE TABLE dashboards
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP,
    "key"      UUID          NOT NULL,
    key_type   VARCHAR(255)      NOT NULL,
    period     VARCHAR(255)        NOT NULL,
    "value"    DECIMAL(5, 2) NOT NULL,
    value_type VARCHAR(255)    NOT NULL,
    rank       INTEGER
);

CREATE INDEX idx_books_is_deleted_title ON books (is_deleted, title);
CREATE INDEX idx_reviews_is_deleted_created_at ON reviews (is_deleted, created_at);
CREATE INDEX idx_reviews_is_deleted_rating ON reviews (is_deleted, rating);
