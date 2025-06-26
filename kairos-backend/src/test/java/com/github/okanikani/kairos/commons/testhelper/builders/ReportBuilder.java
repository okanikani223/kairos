package com.github.okanikani.kairos.commons.testhelper.builders;

import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.User;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Reportエンティティのテストデータビルダー
 * テストコードの可読性と保守性向上のためのBuilder パターン実装
 */
public class ReportBuilder {
    
    private YearMonth yearMonth = YearMonth.of(2025, 1);
    private User owner = new User("testuser");
    private ReportStatus status = ReportStatus.NOT_SUBMITTED;
    private List<Detail> workDays = new ArrayList<>();
    private Summary summary = Summary.EMPTY;
    
    /**
     * ReportBuilderの新しいインスタンスを作成
     * 
     * @return ReportBuilder
     */
    public static ReportBuilder create() {
        return new ReportBuilder();
    }
    
    /**
     * デフォルト値で設定されたReportBuilderを作成
     * 
     * @return ReportBuilder
     */
    public static ReportBuilder defaultReport() {
        return new ReportBuilder();
    }
    
    /**
     * 年月を設定
     * 
     * @param yearMonth 年月
     * @return ReportBuilder
     */
    public ReportBuilder withYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
        return this;
    }
    
    /**
     * 年月を設定（年・月指定）
     * 
     * @param year 年
     * @param month 月
     * @return ReportBuilder
     */
    public ReportBuilder withYearMonth(int year, int month) {
        this.yearMonth = YearMonth.of(year, month);
        return this;
    }
    
    /**
     * 所有者を設定
     * 
     * @param owner 所有者
     * @return ReportBuilder
     */
    public ReportBuilder withOwner(User owner) {
        this.owner = owner;
        return this;
    }
    
    /**
     * 所有者を設定（ユーザーID指定）
     * 
     * @param userId ユーザーID
     * @return ReportBuilder
     */
    public ReportBuilder withOwner(String userId) {
        this.owner = new User(userId);
        return this;
    }
    
    /**
     * ステータスを設定
     * 
     * @param status ステータス
     * @return ReportBuilder
     */
    public ReportBuilder withStatus(ReportStatus status) {
        this.status = status;
        return this;
    }
    
    /**
     * 勤務日情報一覧を設定
     * 
     * @param workDays 勤務日情報一覧
     * @return ReportBuilder
     */
    public ReportBuilder withWorkDays(List<Detail> workDays) {
        this.workDays = new ArrayList<>(workDays);
        return this;
    }
    
    /**
     * 勤務日情報を追加
     * 
     * @param detail 勤務日情報
     * @return ReportBuilder
     */
    public ReportBuilder addWorkDay(Detail detail) {
        this.workDays.add(detail);
        return this;
    }
    
    /**
     * サマリ情報を設定
     * 
     * @param summary サマリ情報
     * @return ReportBuilder
     */
    public ReportBuilder withSummary(Summary summary) {
        this.summary = summary;
        return this;
    }
    
    /**
     * 空のサマリを設定
     * 
     * @return ReportBuilder
     */
    public ReportBuilder withEmptySummary() {
        this.summary = Summary.EMPTY;
        return this;
    }
    
    /**
     * ドラフト状態のレポートを作成
     * 
     * @return ReportBuilder
     */
    public ReportBuilder asDraft() {
        this.status = ReportStatus.NOT_SUBMITTED;
        return this;
    }
    
    /**
     * 提出済み状態のレポートを作成
     * 
     * @return ReportBuilder
     */
    public ReportBuilder asSubmitted() {
        this.status = ReportStatus.SUBMITTED;
        return this;
    }
    
    /**
     * 承認済み状態のレポートを作成
     * 
     * @return ReportBuilder
     */
    public ReportBuilder asApproved() {
        this.status = ReportStatus.APPROVED;
        return this;
    }
    
    /**
     * 特定の年月のレポートを作成
     * 
     * @param year 年
     * @param month 月
     * @return ReportBuilder
     */
    public ReportBuilder forYearMonth(int year, int month) {
        return withYearMonth(year, month);
    }
    
    /**
     * 特定のユーザーのレポートを作成
     * 
     * @param userId ユーザーID
     * @return ReportBuilder
     */
    public ReportBuilder forUser(String userId) {
        return withOwner(userId);
    }
    
    /**
     * Reportエンティティを構築
     * 
     * @return Report
     */
    public Report build() {
        return new Report(yearMonth, owner, status, workDays, summary);
    }
    
    /**
     * 複数のReportエンティティを構築
     * 
     * @param count 作成数
     * @return List<Report>
     */
    public List<Report> buildList(int count) {
        List<Report> reports = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            reports.add(build());
        }
        return reports;
    }
    
    /**
     * 異なる年月で複数のReportエンティティを構築
     * 
     * @param startYear 開始年
     * @param startMonth 開始月
     * @param count 作成数（月数）
     * @return List<Report>
     */
    public List<Report> buildMonthlyList(int startYear, int startMonth, int count) {
        List<Report> reports = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.of(startYear, startMonth);
        
        for (int i = 0; i < count; i++) {
            reports.add(withYearMonth(currentYearMonth).build());
            currentYearMonth = currentYearMonth.plusMonths(1);
        }
        return reports;
    }
}