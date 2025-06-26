-- Kairos 勤怠管理システム データベーススキーマ定義
-- 作成日: 2025-06-26
-- 説明: 全ドメインのテーブル構造を定義

-- データベース設定
SET client_encoding = 'UTF8';
SET timezone = 'Asia/Tokyo';

-- 拡張機能の有効化（必要に応じて）
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==================================================
-- 1. 勤怠表関連テーブル (Reports Domain)
-- ==================================================

-- 勤怠表テーブル（メインテーブル）
CREATE TABLE reports (
    year_month VARCHAR(7) NOT NULL,           -- 年月 (YYYY-MM形式)
    user_id VARCHAR(255) NOT NULL,            -- ユーザーID
    status VARCHAR(20) NOT NULL,              -- ステータス (NOT_SUBMITTED, SUBMITTED, APPROVED)
    
    -- 集計情報（埋め込み）
    work_days NUMERIC(5,2) NOT NULL DEFAULT 0.0,                    -- 就業日数
    paid_leave_days NUMERIC(5,2) NOT NULL DEFAULT 0.0,              -- 有給日数
    compensatory_leave_days NUMERIC(5,2) NOT NULL DEFAULT 0.0,      -- 代休日数
    special_leave_days NUMERIC(5,2) NOT NULL DEFAULT 0.0,           -- 特休日数
    total_work_time_minutes BIGINT NOT NULL DEFAULT 0,              -- 総就業時間（分）
    total_overtime_minutes BIGINT NOT NULL DEFAULT 0,               -- 総残業時間（分）
    total_holiday_work_minutes BIGINT NOT NULL DEFAULT 0,           -- 総休出時間（分）
    
    -- メタデータ
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    PRIMARY KEY (year_month, user_id),
    CONSTRAINT chk_reports_status CHECK (status IN ('NOT_SUBMITTED', 'SUBMITTED', 'APPROVED')),
    CONSTRAINT chk_reports_year_month CHECK (year_month ~ '^\d{4}-\d{2}$')
);

-- 勤務日詳細テーブル
CREATE TABLE report_details (
    id BIGSERIAL PRIMARY KEY,
    report_year_month VARCHAR(7) NOT NULL,     -- 勤怠表への参照（年月）
    report_user_id VARCHAR(255) NOT NULL,      -- 勤怠表への参照（ユーザーID）
    work_date DATE NOT NULL,                   -- 勤務日
    is_holiday BOOLEAN NOT NULL DEFAULT FALSE, -- 休日フラグ
    leave_type VARCHAR(30),                    -- 休暇区分
    start_date_time TIMESTAMP,                 -- 開始日時
    end_date_time TIMESTAMP,                   -- 終了日時
    working_hours_minutes BIGINT NOT NULL DEFAULT 0,      -- 就業時間（分）
    overtime_hours_minutes BIGINT NOT NULL DEFAULT 0,     -- 残業時間（分）
    holiday_work_hours_minutes BIGINT NOT NULL DEFAULT 0, -- 休出時間（分）
    note VARCHAR(500),                         -- 特記事項
    
    -- メタデータ
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    FOREIGN KEY (report_year_month, report_user_id) 
        REFERENCES reports(year_month, user_id) ON DELETE CASCADE,
    CONSTRAINT chk_report_details_leave_type CHECK (
        leave_type IN ('PAID_LEAVE', 'PAID_LEAVE_AM', 'PAID_LEAVE_PM', 
                      'COMPENSATORY_LEAVE', 'COMPENSATORY_LEAVE_AM', 'COMPENSATORY_LEAVE_PM',
                      'SPECIAL_LEAVE')
    ),
    CONSTRAINT chk_report_details_time_range CHECK (
        (start_date_time IS NULL AND end_date_time IS NULL) OR
        (start_date_time IS NOT NULL AND end_date_time IS NOT NULL AND start_date_time <= end_date_time)
    )
);

-- ==================================================
-- 2. 位置情報テーブル (Locations Domain)
-- ==================================================

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    latitude NUMERIC(10,8) NOT NULL,          -- 緯度（-90.0～90.0）
    longitude NUMERIC(11,8) NOT NULL,         -- 経度（-180.0～180.0）
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL, -- 記録日時
    user_id VARCHAR(255) NOT NULL,            -- ユーザーID
    
    -- メタデータ
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    CONSTRAINT chk_locations_latitude CHECK (latitude >= -90.0 AND latitude <= 90.0),
    CONSTRAINT chk_locations_longitude CHECK (longitude >= -180.0 AND longitude <= 180.0)
);

-- ==================================================
-- 3. 勤怠ルールテーブル (Rules Domain)
-- ==================================================

-- 勤怠ルールテーブル（所属期間あり）
CREATE TABLE work_rules (
    id BIGSERIAL PRIMARY KEY,
    work_place_id BIGINT NOT NULL,            -- 勤怠先ID
    latitude NUMERIC(10,8) NOT NULL,          -- 勤怠先緯度
    longitude NUMERIC(11,8) NOT NULL,         -- 勤怠先経度
    user_id VARCHAR(255) NOT NULL,            -- ユーザーID
    standard_start_time TIME NOT NULL,        -- 規定勤怠開始時刻
    standard_end_time TIME NOT NULL,          -- 規定勤怠終了時刻
    break_start_time TIME,                    -- 規定休憩開始時刻
    break_end_time TIME,                      -- 規定休憩終了時刻
    membership_start_date DATE NOT NULL,      -- 所属開始日
    membership_end_date DATE NOT NULL,        -- 所属終了日
    
    -- メタデータ
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    CONSTRAINT chk_work_rules_latitude CHECK (latitude >= -90.0 AND latitude <= 90.0),
    CONSTRAINT chk_work_rules_longitude CHECK (longitude >= -180.0 AND longitude <= 180.0),
    CONSTRAINT chk_work_rules_standard_time CHECK (standard_start_time < standard_end_time),
    CONSTRAINT chk_work_rules_break_time CHECK (
        (break_start_time IS NULL AND break_end_time IS NULL) OR
        (break_start_time IS NOT NULL AND break_end_time IS NOT NULL AND break_start_time < break_end_time)
    ),
    CONSTRAINT chk_work_rules_membership_period CHECK (membership_start_date <= membership_end_date)
);

-- デフォルト勤怠ルールテーブル（所属期間なし）
CREATE TABLE default_work_rules (
    id BIGSERIAL PRIMARY KEY,
    work_place_id BIGINT NOT NULL,            -- 勤怠先ID
    latitude NUMERIC(10,8) NOT NULL,          -- 勤怠先緯度
    longitude NUMERIC(11,8) NOT NULL,         -- 勤怠先経度
    user_id VARCHAR(255) NOT NULL,            -- ユーザーID
    standard_start_time TIME NOT NULL,        -- 規定勤怠開始時刻
    standard_end_time TIME NOT NULL,          -- 規定勤怠終了時刻
    break_start_time TIME,                    -- 規定休憩開始時刻
    break_end_time TIME,                      -- 規定休憩終了時刻
    
    -- メタデータ
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    CONSTRAINT chk_default_work_rules_latitude CHECK (latitude >= -90.0 AND latitude <= 90.0),
    CONSTRAINT chk_default_work_rules_longitude CHECK (longitude >= -180.0 AND longitude <= 180.0),
    CONSTRAINT chk_default_work_rules_standard_time CHECK (standard_start_time < standard_end_time),
    CONSTRAINT chk_default_work_rules_break_time CHECK (
        (break_start_time IS NULL AND break_end_time IS NULL) OR
        (break_start_time IS NOT NULL AND break_end_time IS NOT NULL AND break_start_time < break_end_time)
    ),
    -- 業務制約: 同一ユーザー・同一勤怠先のデフォルトルールは1つまで
    UNIQUE (user_id, work_place_id)
);

-- ==================================================
-- 4. 勤怠作成ルールテーブル (ReportCreationRules Domain)
-- ==================================================

CREATE TABLE report_creation_rules (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,     -- ユーザーID（一意制約）
    closing_day INTEGER NOT NULL,             -- 勤怠締め日（1-31）
    time_calculation_unit_minutes INTEGER NOT NULL, -- 勤怠時間計算単位（分）
    
    -- メタデータ
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    CONSTRAINT chk_report_creation_rules_closing_day CHECK (closing_day >= 1 AND closing_day <= 31),
    CONSTRAINT chk_report_creation_rules_time_unit CHECK (
        time_calculation_unit_minutes >= 1 AND time_calculation_unit_minutes <= 60
    )
);

-- ==================================================
-- 5. インデックス定義
-- ==================================================

-- 勤怠表関連
CREATE INDEX idx_reports_user_id ON reports(user_id);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_report_details_work_date ON report_details(work_date);
CREATE INDEX idx_report_details_user_id ON report_details(report_user_id);

-- 位置情報関連
CREATE INDEX idx_locations_user_id ON locations(user_id);
CREATE INDEX idx_locations_recorded_at ON locations(recorded_at);
CREATE INDEX idx_locations_user_date ON locations(user_id, recorded_at);

-- 勤怠ルール関連
CREATE INDEX idx_work_rules_user_id ON work_rules(user_id);
CREATE INDEX idx_work_rules_work_place_id ON work_rules(work_place_id);
CREATE INDEX idx_work_rules_membership_period ON work_rules(membership_start_date, membership_end_date);
CREATE INDEX idx_default_work_rules_user_id ON default_work_rules(user_id);
CREATE INDEX idx_default_work_rules_work_place_id ON default_work_rules(work_place_id);

-- 勤怠作成ルール関連
-- user_idにはUNIQUE制約があるため、追加のインデックスは不要

-- ==================================================
-- 6. コメント追加
-- ==================================================

-- テーブルコメント
COMMENT ON TABLE reports IS '勤怠表: 月次の勤怠データと集計情報を管理';
COMMENT ON TABLE report_details IS '勤務日詳細: 日次の勤務情報を管理';
COMMENT ON TABLE locations IS '位置情報: GPS座標と記録日時を管理';
COMMENT ON TABLE work_rules IS '勤怠ルール: 所属期間付きの勤務規則を管理';
COMMENT ON TABLE default_work_rules IS 'デフォルト勤怠ルール: 所属期間なしの勤務規則を管理';
COMMENT ON TABLE report_creation_rules IS '勤怠作成ルール: ユーザー毎の勤怠表作成設定を管理';

-- カラムコメント（主要なもの）
COMMENT ON COLUMN reports.year_month IS '勤怠年月 (YYYY-MM形式)';
COMMENT ON COLUMN reports.status IS '勤怠表ステータス (NOT_SUBMITTED/SUBMITTED/APPROVED)';
COMMENT ON COLUMN report_details.leave_type IS '休暇区分 (有給、代休、特休等)';
COMMENT ON COLUMN locations.latitude IS '緯度 (-90.0～90.0)';
COMMENT ON COLUMN locations.longitude IS '経度 (-180.0～180.0)';
COMMENT ON COLUMN work_rules.membership_start_date IS '勤怠先への所属開始日';
COMMENT ON COLUMN work_rules.membership_end_date IS '勤怠先への所属終了日';
COMMENT ON COLUMN report_creation_rules.closing_day IS '勤怠締め日 (1-31)';
COMMENT ON COLUMN report_creation_rules.time_calculation_unit_minutes IS '勤怠時間計算単位 (分, 1-60)';