-- Kairos 勤怠管理システム テストデータ
-- 作成日: 2025-06-26
-- 説明: 開発・テスト用のサンプルデータを挿入

-- データベース設定
SET client_encoding = 'UTF8';
SET timezone = 'Asia/Tokyo';

-- ==================================================
-- 1. 勤怠作成ルール (Report Creation Rules)
-- ==================================================

INSERT INTO report_creation_rules (user_id, closing_day, time_calculation_unit_minutes, created_at, updated_at) VALUES
-- 一般的なパターン
('tanaka@example.com', 25, 15, NOW(), NOW()),        -- 田中さん: 25日締め、15分単位
('sato@example.com', 31, 30, NOW(), NOW()),          -- 佐藤さん: 月末締め、30分単位
('suzuki@example.com', 20, 15, NOW(), NOW()),        -- 鈴木さん: 20日締め、15分単位
('yamada@example.com', 15, 10, NOW(), NOW()),        -- 山田さん: 15日締め、10分単位
('watanabe@example.com', 31, 60, NOW(), NOW()),      -- 渡辺さん: 月末締め、1時間単位

-- 管理者・特殊ケース
('admin@example.com', 31, 15, NOW(), NOW()),         -- 管理者
('manager@example.com', 25, 30, NOW(), NOW());       -- マネージャー

-- ==================================================
-- 2. デフォルト勤怠ルール (Default Work Rules)
-- ==================================================

INSERT INTO default_work_rules (work_place_id, latitude, longitude, user_id, standard_start_time, standard_end_time, break_start_time, break_end_time, created_at, updated_at) VALUES
-- 本社 (東京駅周辺)
(1, 35.681236, 139.767125, 'tanaka@example.com', '09:00:00', '18:00:00', '12:00:00', '13:00:00', NOW(), NOW()),
(1, 35.681236, 139.767125, 'sato@example.com', '09:30:00', '18:30:00', '12:00:00', '13:00:00', NOW(), NOW()),
(1, 35.681236, 139.767125, 'suzuki@example.com', '08:30:00', '17:30:00', '12:00:00', '13:00:00', NOW(), NOW()),

-- 支社 (新宿)
(2, 35.689487, 139.691706, 'yamada@example.com', '09:00:00', '17:30:00', '12:30:00', '13:30:00', NOW(), NOW()),
(2, 35.689487, 139.691706, 'watanabe@example.com', '10:00:00', '19:00:00', NULL, NULL, NOW(), NOW()), -- 休憩なし

-- リモートワーク拠点 (渋谷)
(3, 35.658034, 139.701636, 'admin@example.com', '10:00:00', '19:00:00', '13:00:00', '14:00:00', NOW(), NOW()),
(3, 35.658034, 139.701636, 'manager@example.com', '09:00:00', '18:00:00', '12:00:00', '13:00:00', NOW(), NOW());

-- ==================================================
-- 3. 勤怠ルール (Work Rules) - 所属期間付き
-- ==================================================

INSERT INTO work_rules (work_place_id, latitude, longitude, user_id, standard_start_time, standard_end_time, break_start_time, break_end_time, membership_start_date, membership_end_date, created_at, updated_at) VALUES
-- 田中さんの所属履歴（本社）
(1, 35.681236, 139.767125, 'tanaka@example.com', '09:00:00', '18:00:00', '12:00:00', '13:00:00', '2024-04-01', '2024-12-31', NOW(), NOW()),
(1, 35.681236, 139.767125, 'tanaka@example.com', '09:00:00', '17:30:00', '12:00:00', '13:00:00', '2025-01-01', '2025-12-31', NOW(), NOW()),

-- 佐藤さんの所属履歴（本社→支社）
(1, 35.681236, 139.767125, 'sato@example.com', '09:30:00', '18:30:00', '12:00:00', '13:00:00', '2024-01-01', '2024-09-30', NOW(), NOW()),
(2, 35.689487, 139.691706, 'sato@example.com', '09:00:00', '17:30:00', '12:30:00', '13:30:00', '2024-10-01', '2025-12-31', NOW(), NOW()),

-- 鈴木さんの所属履歴（本社）
(1, 35.681236, 139.767125, 'suzuki@example.com', '08:30:00', '17:30:00', '12:00:00', '13:00:00', '2024-06-01', '2025-12-31', NOW(), NOW()),

-- 山田さんの所属履歴（支社）
(2, 35.689487, 139.691706, 'yamada@example.com', '09:00:00', '17:30:00', '12:30:00', '13:30:00', '2024-01-01', '2025-12-31', NOW(), NOW()),

-- 渡辺さんの所属履歴（支社、フレックス制）
(2, 35.689487, 139.691706, 'watanabe@example.com', '10:00:00', '19:00:00', NULL, NULL, '2024-07-01', '2025-12-31', NOW(), NOW());

-- ==================================================
-- 4. 位置情報 (Locations)
-- ==================================================

INSERT INTO locations (latitude, longitude, recorded_at, user_id, created_at) VALUES
-- 田中さんの位置履歴（2025年6月）
(35.681236, 139.767125, '2025-06-01 08:55:00+09', 'tanaka@example.com', NOW()),
(35.681236, 139.767125, '2025-06-01 18:05:00+09', 'tanaka@example.com', NOW()),
(35.681236, 139.767125, '2025-06-02 09:02:00+09', 'tanaka@example.com', NOW()),
(35.681236, 139.767125, '2025-06-02 17:58:00+09', 'tanaka@example.com', NOW()),
(35.681236, 139.767125, '2025-06-03 08:58:00+09', 'tanaka@example.com', NOW()),
(35.681236, 139.767125, '2025-06-03 17:32:00+09', 'tanaka@example.com', NOW()),

-- 佐藤さんの位置履歴（2025年6月）- 支社勤務
(35.689487, 139.691706, '2025-06-01 08:45:00+09', 'sato@example.com', NOW()),
(35.689487, 139.691706, '2025-06-01 17:35:00+09', 'sato@example.com', NOW()),
(35.689487, 139.691706, '2025-06-02 09:10:00+09', 'sato@example.com', NOW()),
(35.689487, 139.691706, '2025-06-02 17:25:00+09', 'sato@example.com', NOW()),

-- 鈴木さんの位置履歴（2025年6月）
(35.681236, 139.767125, '2025-06-01 08:25:00+09', 'suzuki@example.com', NOW()),
(35.681236, 139.767125, '2025-06-01 17:28:00+09', 'suzuki@example.com', NOW()),
(35.681236, 139.767125, '2025-06-02 08:32:00+09', 'suzuki@example.com', NOW()),
(35.681236, 139.767125, '2025-06-02 17:35:00+09', 'suzuki@example.com', NOW()),

-- 山田さんの位置履歴（2025年6月）- 支社勤務
(35.689487, 139.691706, '2025-06-01 08:55:00+09', 'yamada@example.com', NOW()),
(35.689487, 139.691706, '2025-06-01 17:32:00+09', 'yamada@example.com', NOW()),
(35.689487, 139.691706, '2025-06-02 09:05:00+09', 'yamada@example.com', NOW()),
(35.689487, 139.691706, '2025-06-02 17:28:00+09', 'yamada@example.com', NOW()),

-- 渡辺さんの位置履歴（2025年6月）- フレックス勤務
(35.689487, 139.691706, '2025-06-01 10:15:00+09', 'watanabe@example.com', NOW()),
(35.689487, 139.691706, '2025-06-01 19:05:00+09', 'watanabe@example.com', NOW()),
(35.689487, 139.691706, '2025-06-02 09:45:00+09', 'watanabe@example.com', NOW()),
(35.689487, 139.691706, '2025-06-02 18:50:00+09', 'watanabe@example.com', NOW());

-- ==================================================
-- 5. 勤怠表 (Reports)
-- ==================================================

INSERT INTO reports (year_month, user_id, status, work_days, paid_leave_days, compensatory_leave_days, special_leave_days, total_work_time_minutes, total_overtime_minutes, total_holiday_work_minutes, created_at, updated_at) VALUES
-- 2025年5月の勤怠表（確定済み）
('2025-05', 'tanaka@example.com', 'APPROVED', 20.0, 1.0, 0.0, 0.0, 9600, 180, 0, NOW(), NOW()),      -- 田中: 160時間＋3時間残業
('2025-05', 'sato@example.com', 'APPROVED', 19.0, 2.0, 0.0, 0.0, 9120, 120, 0, NOW(), NOW()),        -- 佐藤: 152時間＋2時間残業
('2025-05', 'suzuki@example.com', 'SUBMITTED', 21.0, 0.0, 0.5, 0.0, 10080, 240, 0, NOW(), NOW()),    -- 鈴木: 168時間＋4時間残業
('2025-05', 'yamada@example.com', 'APPROVED', 18.0, 1.5, 1.0, 0.0, 8640, 90, 0, NOW(), NOW()),      -- 山田: 144時間＋1.5時間残業

-- 2025年6月の勤怠表（進行中）
('2025-06', 'tanaka@example.com', 'NOT_SUBMITTED', 3.0, 0.0, 0.0, 0.0, 1440, 15, 0, NOW(), NOW()),   -- 田中: 24時間＋15分残業
('2025-06', 'sato@example.com', 'NOT_SUBMITTED', 2.0, 0.0, 0.0, 0.0, 960, 0, 0, NOW(), NOW()),       -- 佐藤: 16時間
('2025-06', 'suzuki@example.com', 'NOT_SUBMITTED', 2.0, 0.0, 0.0, 0.0, 1020, 20, 0, NOW(), NOW()),   -- 鈴木: 17時間＋20分残業
('2025-06', 'yamada@example.com', 'NOT_SUBMITTED', 2.0, 0.0, 0.0, 0.0, 960, 0, 0, NOW(), NOW()),     -- 山田: 16時間
('2025-06', 'watanabe@example.com', 'NOT_SUBMITTED', 2.0, 0.0, 0.0, 0.0, 1080, 30, 0, NOW(), NOW()); -- 渡辺: 18時間＋30分残業

-- ==================================================
-- 6. 勤務日詳細 (Report Details)
-- ==================================================

INSERT INTO report_details (report_year_month, report_user_id, work_date, is_holiday, leave_type, start_date_time, end_date_time, working_hours_minutes, overtime_hours_minutes, holiday_work_hours_minutes, note, created_at, updated_at) VALUES

-- 田中さんの2025年6月詳細
('2025-06', 'tanaka@example.com', '2025-06-01', false, NULL, '2025-06-01 09:00:00', '2025-06-01 18:00:00', 480, 0, 0, '通常勤務', NOW(), NOW()),
('2025-06', 'tanaka@example.com', '2025-06-02', false, NULL, '2025-06-02 09:00:00', '2025-06-02 18:15:00', 480, 15, 0, '少し残業', NOW(), NOW()),
('2025-06', 'tanaka@example.com', '2025-06-03', false, NULL, '2025-06-03 09:00:00', '2025-06-03 18:00:00', 480, 0, 0, '通常勤務', NOW(), NOW()),

-- 佐藤さんの2025年6月詳細
('2025-06', 'sato@example.com', '2025-06-01', false, NULL, '2025-06-01 09:00:00', '2025-06-01 17:30:00', 480, 0, 0, '定時退社', NOW(), NOW()),
('2025-06', 'sato@example.com', '2025-06-02', false, NULL, '2025-06-02 09:00:00', '2025-06-02 17:30:00', 480, 0, 0, '定時退社', NOW(), NOW()),

-- 鈴木さんの2025年6月詳細
('2025-06', 'suzuki@example.com', '2025-06-01', false, NULL, '2025-06-01 08:30:00', '2025-06-01 17:40:00', 480, 10, 0, '早出+少し残業', NOW(), NOW()),
('2025-06', 'suzuki@example.com', '2025-06-02', false, NULL, '2025-06-02 08:30:00', '2025-06-02 17:40:00', 480, 10, 0, '早出+少し残業', NOW(), NOW()),

-- 山田さんの2025年6月詳細
('2025-06', 'yamada@example.com', '2025-06-01', false, NULL, '2025-06-01 09:00:00', '2025-06-01 17:30:00', 480, 0, 0, '定時勤務', NOW(), NOW()),
('2025-06', 'yamada@example.com', '2025-06-02', false, NULL, '2025-06-02 09:00:00', '2025-06-02 17:30:00', 480, 0, 0, '定時勤務', NOW(), NOW()),

-- 渡辺さんの2025年6月詳細（フレックス勤務）
('2025-06', 'watanabe@example.com', '2025-06-01', false, NULL, '2025-06-01 10:00:00', '2025-06-01 19:15:00', 540, 15, 0, 'フレックス勤務', NOW(), NOW()),
('2025-06', 'watanabe@example.com', '2025-06-02', false, NULL, '2025-06-02 10:00:00', '2025-06-02 19:15:00', 540, 15, 0, 'フレックス勤務', NOW(), NOW()),

-- 田中さんの2025年5月の一部（サンプル）
('2025-05', 'tanaka@example.com', '2025-05-01', false, NULL, '2025-05-01 09:00:00', '2025-05-01 18:30:00', 480, 30, 0, 'GW前の仕上げ', NOW(), NOW()),
('2025-05', 'tanaka@example.com', '2025-05-02', true, NULL, NULL, NULL, 0, 0, 0, 'GW休暇', NOW(), NOW()),
('2025-05', 'tanaka@example.com', '2025-05-07', false, NULL, '2025-05-07 09:00:00', '2025-05-07 18:00:00', 480, 0, 0, 'GW明け', NOW(), NOW()),
('2025-05', 'tanaka@example.com', '2025-05-15', false, 'PAID_LEAVE', NULL, NULL, 0, 0, 0, '有給取得', NOW(), NOW()),

-- 佐藤さんの2025年5月の一部（サンプル）
('2025-05', 'sato@example.com', '2025-05-01', false, NULL, '2025-05-01 09:30:00', '2025-05-01 18:30:00', 480, 0, 0, '通常勤務', NOW(), NOW()),
('2025-05', 'sato@example.com', '2025-05-10', false, 'PAID_LEAVE', NULL, NULL, 0, 0, 0, '有給休暇', NOW(), NOW()),
('2025-05', 'sato@example.com', '2025-05-20', false, 'PAID_LEAVE', NULL, NULL, 0, 0, 0, '有給休暇', NOW(), NOW()),

-- 鈴木さんの2025年5月の一部（サンプル）
('2025-05', 'suzuki@example.com', '2025-05-01', false, NULL, '2025-05-01 08:30:00', '2025-05-01 18:30:00', 480, 60, 0, '繁忙期対応', NOW(), NOW()),
('2025-05', 'suzuki@example.com', '2025-05-15', false, 'COMPENSATORY_LEAVE_AM', '2025-05-15 13:00:00', '2025-05-15 17:30:00', 240, 0, 0, '午前代休', NOW(), NOW());

-- コメント用データ
COMMENT ON TABLE locations IS 'テストデータには東京の主要エリア（東京駅、新宿、渋谷）のGPS座標を使用';
COMMENT ON TABLE reports IS 'テストデータには2025年5月（確定済み）と2025年6月（進行中）の勤怠表を含む';
COMMENT ON TABLE report_details IS 'テストデータには通常勤務、残業、有給休暇、代休などの様々なパターンを含む';