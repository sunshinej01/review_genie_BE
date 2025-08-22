-- data.sql
-- Review Genie 초기 데이터 삽입
-- Spring Boot 애플리케이션 시작 시 자동으로 실행됩니다.

-- ===== 사용자 데이터 삽입 =====
INSERT INTO "user" (user_id, username, password) VALUES 
(1, 'Linsey', 'password1'),
(2, 'Sunshine', 'password2'),
(3, 'Joe', 'password3');

-- ===== 매장 데이터 삽입 =====
INSERT INTO store (id, place_id, store_name, location, review_count, user_id) VALUES 
(1, '01', '카페페퍼', '서울 송파구 100번길', 205, 1),
(2, '02', '런던베이글뮤지엄 잠실점', '서울 송파구 200번길', 200, 2),
(3, '03', '파이홀', '서울 송파구 300번길', 180, 3);

-- ===== 키워드 데이터 삽입 =====
INSERT INTO keyword (keyword_id, keyword_name) VALUES 
(1, '포장'),
(2, '메뉴'),
(3, '인테리어'),
(4, '맛'),
(5, '가격'),
(6, '예약'),
(7, '청결');

-- ===== 리뷰 데이터 삽입 (샘플 20개) =====
INSERT INTO review (review_id, store_id, content, sentiment, created_at) VALUES 
(1, 1, '다양한 케이크류 디저트를 파는 예쁜 카페 카페페퍼 방문했어요💚 카페는 러블리하게 꾸며져있어서 데이트나 친구와 예쁜 카페 투어하기 딱이었구, 디저트 종류도 생각보다 많아서 고르는데 한참 걸렸네요~^^', 'POSITIVE', '2025-08-20T15:41:56.1418703'),
(2, 1, '흑임자 케이크 짱 맛있어서 순삭했어요. 다음에 다른 케이크도 먹으러와야겠어요. 말차라떼도 맛있어요! 🥰', 'POSITIVE', '2025-08-20T15:41:56.1438765'),
(3, 1, '딸기 크림치즈 (9,500) 딸기가 생이라서 맛있었고 크림치즈도 꾸덕! 근데 좀 달고 진해서 아쉬웠어요', 'POSITIVE', '2025-08-20T15:41:56.1458858'),
(4, 1, '실한망고케이그 블루베리치즈케익까지 디피도 에뷰네오✨ 디저트천국', 'POSITIVE', '2025-08-20T15:41:56.1468575'),
(5, 1, '전국에서 젤 맛있는 글루텐프리 디저트 카페🍒 애플파이 먹어본 애플파이 중에 가장 맛있었고 아직도 뛰어넘은 애플파이가 없는..👍🏻', 'POSITIVE', '2025-08-20T15:41:56.1478641'),
(6, 1, '음료랑 디저트가 맛있어료', 'POSITIVE', '2025-08-20T15:41:56.1484743'),
(7, 1, '글루텐프리 디저트가 궁금해 방문했는데, 기대 이상으로 맛있게 잘 먹었습니다. 얼그레이 오렌지 케이크가 인상 깊었는데, 얼그레이와 오렌지의 조화가 잘 어우러져서 놀랬어요.', 'POSITIVE', '2025-08-20T15:41:56.1495047'),
(8, 1, '디자트와 음료 맜있었고 인테리어도 예쁘게 해놔서 사진 찍기도 좋을 것 같아요!', 'POSITIVE', '2025-08-20T15:41:56.1495047'),
(9, 1, '주말에 갔더니 캐치테이블로 웨이팅 30분 좀 안 되게 하고 들어갔어요! 자리도 이전 손님이 나가면 바로 직원분이 소독제로 닦아줘서 좋았어요 ㅎㅎ', 'POSITIVE', '2025-08-20T15:41:56.1505443'),
(10, 1, '케잌 맛집입니당!! 페퍼라떼랑 벚꽃라떼중에 고민된다면 커피좋아 -> 페퍼라떼 / 달다구리조아 -> 벚꽃라떼 추천합니다>_<', 'POSITIVE', '2025-08-20T15:41:56.1505443'),
(11, 2, '런던베이글뮤지엄 잠실점에서 베이글을 먹었는데 정말 맛있었어요! 크림치즈와 함께 먹으니 더욱 맛있었습니다.', 'POSITIVE', '2025-08-20T15:41:57.0000000'),
(12, 2, '베이글 종류가 정말 다양해서 고르기 어려웠어요. 하지만 모든 베이글이 다 맛있었습니다!', 'POSITIVE', '2025-08-20T15:41:57.1000000'),
(13, 2, '런던베이글뮤지엄의 분위기가 정말 좋았어요. 영국풍 인테리어가 인상적이었습니다.', 'POSITIVE', '2025-08-20T15:41:57.2000000'),
(14, 2, '베이글 가격이 조금 비싸긴 하지만 맛과 품질을 생각하면 충분히 가치가 있다고 생각해요.', 'POSITIVE', '2025-08-20T15:41:57.3000000'),
(15, 2, '런던베이글뮤지엄에서 아침 식사로 베이글을 먹었는데 정말 만족스러웠어요!', 'POSITIVE', '2025-08-20T15:41:57.4000000'),
(16, 3, '파이홀에서 파이를 먹었는데 정말 맛있었어요! 크림이 부드럽고 달콤해서 좋았습니다.', 'POSITIVE', '2025-08-20T15:41:58.0000000'),
(17, 3, '파이홀의 인테리어가 정말 예뻐요! 사진 찍기 좋은 곳이에요.', 'POSITIVE', '2025-08-20T15:41:58.1000000'),
(18, 3, '파이 종류가 많아서 고르기 어려웠지만, 모든 파이가 다 맛있었어요!', 'POSITIVE', '2025-08-20T15:41:58.2000000'),
(19, 3, '파이홀에서 차와 함께 파이를 먹었는데 정말 완벽한 조합이었어요!', 'POSITIVE', '2025-08-20T15:41:58.3000000'),
(20, 3, '파이홀의 서비스가 정말 친절했어요! 다음에 또 가고 싶습니다.', 'POSITIVE', '2025-08-20T15:41:58.4000000');

-- ===== 경쟁사 데이터 삽입 (JOIN 사용) =====
-- 서로 다른 카테고리의 매장들을 경쟁사로 설정
INSERT INTO competitor (store_id, competitor_store_id)
SELECT DISTINCT s1.id, s2.id
FROM store s1
CROSS JOIN store s2
WHERE s1.id != s2.id
  AND s1.id = 1  -- 카페페퍼
  AND s2.id IN (2, 3);  -- 런던베이글뮤지엄, 파이홀

-- 런던베이글뮤지엄의 경쟁사 (카페페퍼, 파이홀)
INSERT INTO competitor (store_id, competitor_store_id)
SELECT DISTINCT s1.id, s2.id
FROM store s1
CROSS JOIN store s2
WHERE s1.id != s2.id
  AND s1.id = 2  -- 런던베이글뮤지엄
  AND s2.id IN (1, 3);  -- 카페페퍼, 파이홀

-- 파이홀의 경쟁사 (카페페퍼, 런던베이글뮤지엄)
INSERT INTO competitor (store_id, competitor_store_id)
SELECT DISTINCT s1.id, s2.id
FROM store s1
CROSS JOIN store s2
WHERE s1.id != s2.id
  AND s1.id = 3  -- 파이홀
  AND s2.id IN (1, 2);  -- 카페페퍼, 런던베이글뮤지엄

-- ===== 시퀀스 값 업데이트 =====
-- PostgreSQL에서 다음 INSERT 시 올바른 ID가 생성되도록 시퀀스 값을 업데이트
SELECT setval('user_user_id_seq', (SELECT MAX(user_id) FROM "user"));
SELECT setval('store_id_seq', (SELECT MAX(id) FROM store));
SELECT setval('keyword_keyword_id_seq', (SELECT MAX(keyword_id) FROM keyword));
SELECT setval('review_review_id_seq', (SELECT MAX(review_id) FROM review));
