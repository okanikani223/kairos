-- ユーザーテーブル(users)
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(50) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    display_language VARCHAR(10) NOT NULL DEFAULT 'ja',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT users_username_unique UNIQUE (username)
);

-- ユーザー勤務設定テーブル(user_work_settings)
CREATE TABLE user_work_settings (
    setting_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    default_start_time TIME NOT NULL DEFAULT '09:00:00',
    default_end_time TIME NOT NULL DEFAULT '18:00:00',
    break_start_time TIME NOT NULL DEFAULT '12:00:00',
    break_end_time TIME NOT NULL DEFAULT '13:00:00',
    break_duration_minutes INTEGER NOT NULL DEFAULT 60,
    standard_work_minutes INTEGER NOT NULL DEFAULT 480,
    time_rounding_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_work_settings_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_user_work_settings_user_id ON user_work_settings (user_id);

-- 勤務場所テーブル(work_locations)
CREATE TABLE work_locations (
    location_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    latitude DECIMAL(9,7) NOT NULL, -- 緯度は-90～90なので整数部分は2桁(符号1桁含む)で十分
    longitude DECIMAL(10,7) NOT NULL, -- 経度は-180～180なので整数部分は3桁(符号1桁含む)で十分
    radius_meters INTEGER NOT NULL DEFAULT 100,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_work_locations_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_work_locations_user_id ON work_locations (user_id);

-- 休日設定テーブル(holidays)
CREATE TABLE holidays (
    holiday_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_type VARCHAR(20) NOT NULL DEFAULT 'NATIONAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_holidays_date ON holidays (holiday_date);

-- 休暇残数テーブル(leave_balances)
CREATE TABLE leave_balances (
    leave_balance_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    fiscal_year INTEGER NOT NULL,
    paid_leave_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    compensatory_leave_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    special_leave_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_balances_user_id FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT leave_balances_user_year_unique UNIQUE (user_id, fiscal_year)
);

-- 位置情報ログテーブル(location_logs)
CREATE TABLE location_logs (
    log_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    latitude DECIMAL(9,7) NOT NULL, -- 緯度は-90～90なので整数部分は2桁(符号1桁含む)で十分
    longitude DECIMAL(10,7) NOT NULL, -- 経度は-180～180なので整数部分は3桁(符号1桁含む)で十分
    accuracy DECIMAL(10,2),
    device_info VARCHAR(255),
    CONSTRAINT fk_location_logs_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_location_logs_user_recorded ON location_logs (user_id, recorded_at);

-- 勤怠記録テーブル(attendance_records)
CREATE TABLE attendance_records (
    record_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    record_date DATE NOT NULL,
    start_time TIMESTAMP, -- TIMEからTIMESTAMPに変更（日をまたぐ勤務に対応）
    end_time TIMESTAMP, -- TIMEからTIMESTAMPに変更（日をまたぐ勤務に対応）
    break_minutes INTEGER NOT NULL DEFAULT 0,
    is_holiday BOOLEAN NOT NULL DEFAULT FALSE,
    leave_type VARCHAR(20),
    overtime_minutes INTEGER NOT NULL DEFAULT 0,
    holiday_work_minutes INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_records_user_id FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT attendance_records_user_date_unique UNIQUE (user_id, record_date)
);

-- 勤怠報告書テーブル(attendance_reports)
CREATE TABLE attendance_reports (
    report_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    target_year INTEGER NOT NULL,
    target_month INTEGER NOT NULL,
    working_days INTEGER NOT NULL DEFAULT 0,
    paid_leave_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    compensatory_leave_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    special_leave_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    total_working_minutes INTEGER NOT NULL DEFAULT 0,
    total_overtime_minutes INTEGER NOT NULL DEFAULT 0,
    total_holiday_work_minutes INTEGER NOT NULL DEFAULT 0,
    report_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_reports_user_id FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT attendance_reports_user_year_month_unique UNIQUE (user_id, target_year, target_month)
);

-- 休暇予約テーブル(leave_reservations)
CREATE TABLE leave_reservations (
    reservation_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    leave_date DATE NOT NULL,
    leave_type VARCHAR(20) NOT NULL DEFAULT 'PAID',
    is_half_day BOOLEAN NOT NULL DEFAULT FALSE,
    day_part VARCHAR(10),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_applied BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_leave_reservations_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_leave_reservations_user_date ON leave_reservations (user_id, leave_date);

-- 更新時にタイムスタンプを自動更新するトリガー関数
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 各テーブルに更新時タイムスタンプトリガーを追加
CREATE TRIGGER update_users_timestamp
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_user_work_settings_timestamp
BEFORE UPDATE ON user_work_settings
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_work_locations_timestamp
BEFORE UPDATE ON work_locations
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_holidays_timestamp
BEFORE UPDATE ON holidays
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_leave_balances_timestamp
BEFORE UPDATE ON leave_balances
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_attendance_records_timestamp
BEFORE UPDATE ON attendance_records
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_attendance_reports_timestamp
BEFORE UPDATE ON attendance_reports
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER update_leave_reservations_timestamp
BEFORE UPDATE ON leave_reservations
FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- コメント追加
COMMENT ON TABLE users IS 'ユーザー情報を管理するテーブル';
COMMENT ON TABLE user_work_settings IS 'ユーザーごとの勤務設定を管理するテーブル';
COMMENT ON TABLE work_locations IS 'ユーザーごとの勤務場所情報を管理するテーブル';
COMMENT ON TABLE holidays IS 'システム共通の休日情報を管理するテーブル';
COMMENT ON TABLE leave_balances IS 'ユーザーごとの休暇残数を管理するテーブル';
COMMENT ON TABLE location_logs IS 'ユーザーの位置情報ログを管理するテーブル';
COMMENT ON TABLE attendance_records IS '日ごとの勤怠情報を管理するテーブル';
COMMENT ON TABLE attendance_reports IS '月次の勤怠報告書情報を管理するテーブル';
COMMENT ON TABLE leave_reservations IS '休暇予約情報を管理するテーブル';