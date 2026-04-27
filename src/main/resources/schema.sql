CREATE TYPE "deleted_status" AS ENUM (
    'ACTIVE',
    'DELETED'
    );

CREATE TYPE "period_type" AS ENUM (
    'DAILY',
    'WEEKLY',
    'MONTHLY',
    'ALL_TIME'
    );

CREATE TABLE "users"
(
    "id"         UUID PRIMARY KEY,
    "email"      VARCHAR(255) NOT NULL UNIQUE,
    "password"   VARCHAR(255)        NOT NULL,
    "nickname"   VARCHAR(20)         NOT NULL,
    "status"     deleted_status         NOT NULL DEFAULT 'ACTIVE',
    "created_at" TIMESTAMPtz           NOT NULL DEFAULT now(),
    "updated_at" TIMESTAMP,
    "deleted_at" TIMESTAMP
);

CREATE TABLE "books"
(
    "id"             UUID PRIMARY KEY,
    "title"          VARCHAR(255)     NOT NULL,
    "author"         VARCHAR(255)     NOT NULL,
    "description"    TEXT             NOT NULL,
    "publisher"      VARCHAR(255)     NOT NULL,
    "published_date" DATE             NOT NULL,
    "isbn"           VARCHAR(20)      NOT NULL UNIQUE,
    "thumbnail_url"  VARCHAR(500),
    "review_count"   INTEGER          NOT NULL DEFAULT 0 CHECK ("review_count" >= 0),
    "rating"         DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK ("rating" BETWEEN 0 AND 5),
    "book_status"    deleted_status      NOT NULL DEFAULT 'ACTIVE',
    "deleted_at"     TIMESTAMP,
    "created_at"     TIMESTAMPtz        NOT NULL DEFAULT now(),
    "updated_at"     TIMESTAMP
);

CREATE TABLE "reviews"
(
    "id"            UUID PRIMARY KEY,
    "book_id"       UUID          NOT NULL,
    "user_id"       UUID          NOT NULL,
    "rating"        INTEGER       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    "content"       TEXT          NOT NULL,
    "like_count"    INTEGER       NOT NULL DEFAULT 0,
    "comment_count" INTEGER       NOT NULL DEFAULT 0,
    "status"        deleted_status NOT NULL DEFAULT 'ACTIVE',
    "created_at"    TIMESTAMPtz     NOT NULL DEFAULT now(),
    "updated_at"    TIMESTAMP,
    "deleted_at"    TIMESTAMP,

    FOREIGN KEY ("book_id") REFERENCES "books" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE
);
ALTER TABLE "reviews"
    ADD CONSTRAINT "uk_book_user" UNIQUE (book_id, user_id);

CREATE TABLE "comments"
(
    "id"         UUID PRIMARY KEY,
    "review_id"  UUID           NOT NULL,
    "user_id"    UUID           NOT NULL,
    "content"    VARCHAR(500)   NOT NULL,
    "status"     deleted_status NOT NULL DEFAULT 'ACTIVE',
    "created_at" TIMESTAMPtz      NOT NULL DEFAULT now(),
    "updated_at" TIMESTAMP,

    FOREIGN KEY ("review_id") REFERENCES "reviews" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("user_id") REFERENCES "users" ("id")  ON DELETE CASCADE
);

CREATE TABLE "notifications"
(
    "id"             UUID PRIMARY KEY,
    "user_id"        UUID         NOT NULL,
    "review_id"      UUID         NOT NULL,
    "review_content" TEXT         NOT NULL,
    "message"        VARCHAR(255) NOT NULL,
    "confirmed"      BOOLEAN      NOT NULL DEFAULT false,
    "created_at"     TIMESTAMPtz  NOT NULL DEFAULT now(),
    "updated_at"     TIMESTAMPtz,

    FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("review_id") REFERENCES "reviews" ("id")  ON DELETE CASCADE
);

CREATE TABLE "review_likes"
(
    "id"         UUID        PRIMARY KEY,
    "review_id"  UUID        NOT NULL,
    "user_id"    UUID        NOT NULL,
    "liked_at"   TIMESTAMPtz NOT NULL,
    "created_at" TIMESTAMPtz NOT NULL DEFAULT now(),
    "updated_at" TIMESTAMPtz,

    FOREIGN KEY ("review_id") REFERENCES "reviews" ("id")  ON DELETE CASCADE,
    FOREIGN KEY ("user_id") REFERENCES "users" ("id")  ON DELETE CASCADE
);
ALTER TABLE "review_likes"
    ADD CONSTRAINT "uk_review_user_like" UNIQUE (review_id, user_id);
CREATE INDEX "idx_review_likes_liked_at_review_id" ON "review_likes" (liked_at, review_id);
CREATE INDEX "idx_review_likes_liked_at_user_id" ON "review_likes" (liked_at, user_id);


CREATE TABLE "power_users"
(
    "id"               UUID PRIMARY KEY,
    "snapshot_id"      UUID             NOT NULL,
    "user_id"          UUID             NOT NULL,
    "period_type"      period_type      NOT NULL,
    "period_start"     TIMESTAMPtz        NOT NULL,
    "period_end"       TIMESTAMPtz        NOT NULL,
    "rank"             BIGINT           NOT NULL,
    "score"            DOUBLE PRECISION NOT NULL,
    "review_score_sum" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "like_count"       BIGINT           NOT NULL DEFAULT 0,
    "comment_count"    BIGINT           NOT NULL DEFAULT 0,
    "aggregated_at"    TIMESTAMPtz        NOT NULL DEFAULT now(),
    "created_at"       TIMESTAMPtz        NOT NULL DEFAULT now(),
    "updated_at"       TIMESTAMP,

    FOREIGN KEY ("user_id") REFERENCES "users" ("id")  ON DELETE CASCADE
);

ALTER TABLE "power_users"
    ADD CONSTRAINT "uk_power_users_period" UNIQUE (user_id, period_type, period_start, period_end);

CREATE INDEX "idx_power_users_period_start_rank" ON "power_users" (period_type, period_start, rank);
CREATE INDEX "idx_power_users_snapshot_rank" ON "power_users" (snapshot_id, rank);


CREATE TABLE "popular_reviews"
(
    "id"            UUID PRIMARY KEY,
    "review_id"     UUID             NOT NULL,
    "period_type"   period_type      NOT NULL,
    "period_start"  TIMESTAMPtz        NOT NULL,
    "period_end"    TIMESTAMPtz        NOT NULL,
    "rank"          BIGINT           NOT NULL,
    "score"         DOUBLE PRECISION NOT NULL,
    "like_count"    BIGINT           NOT NULL,
    "comment_count" BIGINT           NOT NULL,
    "snapshot_id"   UUID             NOT NULL,
    "aggregated_at" TIMESTAMPtz        NOT NULL,
    "created_at"    TIMESTAMPtz        NOT NULL DEFAULT now(),
    "updated_at"    TIMESTAMP,

    FOREIGN KEY ("review_id") REFERENCES "reviews" ("id")  ON DELETE CASCADE
);
ALTER TABLE "popular_reviews"
    ADD CONSTRAINT "uk_popular_reviews_period" UNIQUE (review_id, period_type, period_start, snapshot_id);

CREATE INDEX "idx_popular_reviews_period_start_rank" ON "popular_reviews" (period_type, period_start, rank);
CREATE INDEX "idx_popular_reviews_snapshot_rank" ON "popular_reviews" (snapshot_id, rank);
