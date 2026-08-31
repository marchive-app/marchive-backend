-- dev 환경 테스트용 초기 데이터
INSERT INTO users (google_sub, email, nickname, created_at)
VALUES ('local-test-google-sub-12345', 'testuser@marchive.com', 'TestUser', NOW());

INSERT INTO ig_accounts (user_id, ig_user_id, ig_handle, created_at)
VALUES (1, 'test_ig_id_001', 'coffee_lover', NOW());

INSERT INTO posts (ig_code, author_handle, caption, posted_at, like_count, created_at)
VALUES ('TEST_CODE_001', 'coffee_lover', 'A cup of coffee today', NOW(), 10, NOW());

INSERT INTO post_media (post_id, media_type, media_key, ig_cdn_url, order_index, upload_status, ocr_status)
VALUES (1, 'image', '실제_S3_key_값', 'https://example.com/fake.jpg', 0, 'DONE', 'DONE');

INSERT INTO bookmarks (ig_account_id, post_id, bookmarked_at)
VALUES (1, 1, NOW());