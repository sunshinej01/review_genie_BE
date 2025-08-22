-- V1__Create_initial_schema.sql
-- Review Genie 초기 데이터베이스 스키마 생성

-- 사용자 테이블
CREATE TABLE "user" (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- 상점 테이블
CREATE TABLE store (
    id SERIAL PRIMARY KEY,
    place_id VARCHAR(255) UNIQUE,
    store_name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    review_count INTEGER,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);

-- 키워드 테이블
CREATE TABLE keyword (
    keyword_id SERIAL PRIMARY KEY,
    keyword_name VARCHAR(255) NOT NULL
);

-- 리뷰 테이블
CREATE TABLE review (
    review_id SERIAL PRIMARY KEY,
    store_id INTEGER NOT NULL,
    content TEXT,
    sentiment VARCHAR(50),
    created_at TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id)
);

-- 경쟁사 테이블 (복합키)
CREATE TABLE competitor (
    store_id INTEGER NOT NULL,
    competitor_store_id INTEGER NOT NULL,
    PRIMARY KEY (store_id, competitor_store_id),
    FOREIGN KEY (store_id) REFERENCES store(id),
    FOREIGN KEY (competitor_store_id) REFERENCES store(id)
);

-- 상위 일반 키워드 테이블
CREATE TABLE top_general_keyword (
    top_keyword_id SERIAL PRIMARY KEY,
    store_id INTEGER NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    frequency INTEGER NOT NULL,
    last_updated TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id)
);

-- 핵심 키워드 감정 분석 테이블
CREATE TABLE core_keyword_sentiment (
    sentiment_id SERIAL PRIMARY KEY,
    store_id INTEGER NOT NULL,
    keyword_id INTEGER NOT NULL,
    positive_count INTEGER NOT NULL,
    negative_count INTEGER NOT NULL,
    last_updated TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id),
    FOREIGN KEY (keyword_id) REFERENCES keyword(keyword_id)
);

-- 인덱스 생성
CREATE INDEX idx_store_place_id ON store(place_id);
CREATE INDEX idx_review_store_id ON review(store_id);
CREATE INDEX idx_review_sentiment ON review(sentiment);
CREATE INDEX idx_top_keyword_store_id ON top_general_keyword(store_id);
CREATE INDEX idx_core_sentiment_store_id ON core_keyword_sentiment(store_id);
CREATE INDEX idx_core_sentiment_keyword_id ON core_keyword_sentiment(keyword_id);
