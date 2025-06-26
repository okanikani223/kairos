package com.github.okanikani.kairos.commons.testhelper.builders;

import com.github.okanikani.kairos.rules.domains.models.entities.WorkRule;
import com.github.okanikani.kairos.rules.domains.models.vos.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * WorkRuleエンティティのテストデータビルダー
 * テストコードの可読性と保守性向上のためのBuilder パターン実装
 */
public class WorkRuleBuilder {
    
    private Long id = null;
    private Long workPlaceId = 1L;
    private double latitude = 35.6895; // 東京都庁の緯度（デフォルト）
    private double longitude = 139.6917; // 東京都庁の経度（デフォルト）
    private User user = new User("testuser");
    private LocalTime standardStartTime = LocalTime.of(9, 0);
    private LocalTime standardEndTime = LocalTime.of(17, 30);
    private LocalTime breakStartTime = LocalTime.of(12, 0);
    private LocalTime breakEndTime = LocalTime.of(13, 0);
    private LocalDate membershipStartDate = LocalDate.of(2025, 1, 1);
    private LocalDate membershipEndDate = LocalDate.of(2025, 12, 31);
    
    /**
     * WorkRuleBuilderの新しいインスタンスを作成
     * 
     * @return WorkRuleBuilder
     */
    public static WorkRuleBuilder create() {
        return new WorkRuleBuilder();
    }
    
    /**
     * デフォルト値で設定されたWorkRuleBuilderを作成
     * 
     * @return WorkRuleBuilder
     */
    public static WorkRuleBuilder defaultWorkRule() {
        return new WorkRuleBuilder();
    }
    
    /**
     * IDを設定
     * 
     * @param id ID
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    /**
     * 勤怠先IDを設定
     * 
     * @param workPlaceId 勤怠先ID
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withWorkPlaceId(Long workPlaceId) {
        this.workPlaceId = workPlaceId;
        return this;
    }
    
    /**
     * 緯度を設定
     * 
     * @param latitude 緯度
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }
    
    /**
     * 経度を設定
     * 
     * @param longitude 経度
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
    
    /**
     * 座標を設定
     * 
     * @param latitude 緯度
     * @param longitude 経度
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }
    
    /**
     * ユーザーを設定
     * 
     * @param user ユーザー
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withUser(User user) {
        this.user = user;
        return this;
    }
    
    /**
     * ユーザーを設定（ユーザーID指定）
     * 
     * @param userId ユーザーID
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withUser(String userId) {
        this.user = new User(userId);
        return this;
    }
    
    /**
     * 標準勤務開始時刻を設定
     * 
     * @param standardStartTime 標準勤務開始時刻
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withStandardStartTime(LocalTime standardStartTime) {
        this.standardStartTime = standardStartTime;
        return this;
    }
    
    /**
     * 標準勤務終了時刻を設定
     * 
     * @param standardEndTime 標準勤務終了時刻
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withStandardEndTime(LocalTime standardEndTime) {
        this.standardEndTime = standardEndTime;
        return this;
    }
    
    /**
     * 標準勤務時間を設定
     * 
     * @param startTime 開始時刻
     * @param endTime 終了時刻
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withStandardWorkTime(LocalTime startTime, LocalTime endTime) {
        this.standardStartTime = startTime;
        this.standardEndTime = endTime;
        return this;
    }
    
    /**
     * 休憩開始時刻を設定
     * 
     * @param breakStartTime 休憩開始時刻
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
        return this;
    }
    
    /**
     * 休憩終了時刻を設定
     * 
     * @param breakEndTime 休憩終了時刻
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withBreakEndTime(LocalTime breakEndTime) {
        this.breakEndTime = breakEndTime;
        return this;
    }
    
    /**
     * 休憩時間を設定
     * 
     * @param startTime 休憩開始時刻
     * @param endTime 休憩終了時刻
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withBreakTime(LocalTime startTime, LocalTime endTime) {
        this.breakStartTime = startTime;
        this.breakEndTime = endTime;
        return this;
    }
    
    /**
     * 休憩なしを設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withoutBreak() {
        this.breakStartTime = null;
        this.breakEndTime = null;
        return this;
    }
    
    /**
     * 所属開始日を設定
     * 
     * @param membershipStartDate 所属開始日
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withMembershipStartDate(LocalDate membershipStartDate) {
        this.membershipStartDate = membershipStartDate;
        return this;
    }
    
    /**
     * 所属終了日を設定
     * 
     * @param membershipEndDate 所属終了日
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withMembershipEndDate(LocalDate membershipEndDate) {
        this.membershipEndDate = membershipEndDate;
        return this;
    }
    
    /**
     * 所属期間を設定
     * 
     * @param startDate 開始日
     * @param endDate 終了日
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withMembershipPeriod(LocalDate startDate, LocalDate endDate) {
        this.membershipStartDate = startDate;
        this.membershipEndDate = endDate;
        return this;
    }
    
    /**
     * 標準的な9-17時勤務を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withStandardWorkSchedule() {
        return withStandardWorkTime(LocalTime.of(9, 0), LocalTime.of(17, 0))
                .withBreakTime(LocalTime.of(12, 0), LocalTime.of(13, 0));
    }
    
    /**
     * フレックスタイム制を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withFlexTime() {
        return withStandardWorkTime(LocalTime.of(10, 0), LocalTime.of(19, 0))
                .withBreakTime(LocalTime.of(12, 0), LocalTime.of(13, 0));
    }
    
    /**
     * 早朝勤務を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withEarlyShift() {
        return withStandardWorkTime(LocalTime.of(6, 0), LocalTime.of(15, 0))
                .withBreakTime(LocalTime.of(10, 0), LocalTime.of(11, 0));
    }
    
    /**
     * 夜間勤務を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder withNightShift() {
        return withStandardWorkTime(LocalTime.of(22, 0), LocalTime.of(6, 0))
                .withoutBreak();
    }
    
    /**
     * 新規作成用（IDなし）のWorkRuleを作成
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder asNew() {
        this.id = null;
        return this;
    }
    
    /**
     * 保存済み（IDあり）のWorkRuleを作成
     * 
     * @param id ID
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder asSaved(Long id) {
        this.id = id;
        return this;
    }
    
    /**
     * 東京都庁の座標を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder atTokyoMetropolitanGovernment() {
        return withCoordinates(35.6895, 139.6917);
    }
    
    /**
     * 東京駅の座標を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder atTokyoStation() {
        return withCoordinates(35.6812, 139.7671);
    }
    
    /**
     * 新宿駅の座標を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder atShinjukuStation() {
        return withCoordinates(35.6896, 139.7006);
    }
    
    /**
     * 今年の期間を設定
     * 
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder forThisYear() {
        int currentYear = LocalDate.now().getYear();
        return withMembershipPeriod(
                LocalDate.of(currentYear, 1, 1),
                LocalDate.of(currentYear, 12, 31)
        );
    }
    
    /**
     * 特定の年の期間を設定
     * 
     * @param year 年
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder forYear(int year) {
        return withMembershipPeriod(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );
    }
    
    /**
     * 特定のユーザーのWorkRuleを作成
     * 
     * @param userId ユーザーID
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder forUser(String userId) {
        return withUser(userId);
    }
    
    /**
     * 特定の勤怠先のWorkRuleを作成
     * 
     * @param workPlaceId 勤怠先ID
     * @return WorkRuleBuilder
     */
    public WorkRuleBuilder forWorkPlace(Long workPlaceId) {
        return withWorkPlaceId(workPlaceId);
    }
    
    /**
     * WorkRuleエンティティを構築
     * 
     * @return WorkRule
     */
    public WorkRule build() {
        return new WorkRule(
                id,
                workPlaceId,
                latitude,
                longitude,
                user,
                standardStartTime,
                standardEndTime,
                breakStartTime,
                breakEndTime,
                membershipStartDate,
                membershipEndDate
        );
    }
    
    /**
     * 複数のWorkRuleエンティティを構築
     * 
     * @param count 作成数
     * @return List<WorkRule>
     */
    public List<WorkRule> buildList(int count) {
        List<WorkRule> workRules = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Long currentId = (id != null) ? id + i : null;
            Long currentWorkPlaceId = workPlaceId + i;
            workRules.add(withId(currentId).withWorkPlaceId(currentWorkPlaceId).build());
        }
        return workRules;
    }
}