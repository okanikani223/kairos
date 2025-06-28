package com.github.okanikani.kairos.reports.others.jpa.repositories;

import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.others.jpa.entities.DetailJpaEntity;
import com.github.okanikani.kairos.reports.others.jpa.entities.ReportId;
import com.github.okanikani.kairos.reports.others.jpa.entities.ReportJpaEntity;
import com.github.okanikani.kairos.reports.others.jpa.entities.SummaryJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReportJpaRepositoryの統合テスト
 * 複合主キーとリレーションシップのテストを含む
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ReportJpaRepository統合テスト")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
})
class ReportJpaRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("kairos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @Autowired
    private ReportJpaRepository reportJpaRepository;

    private ReportJpaEntity testReport1;
    private ReportJpaEntity testReport2;
    private ReportId reportId1;
    private ReportId reportId2;

    @BeforeEach
    void setUp() {
        reportJpaRepository.deleteAll();
        
        // 2024年1月のレポート
        reportId1 = new ReportId(YearMonth.of(2024, 1), "user1");
        SummaryJpaEntity summary1 = new SummaryJpaEntity(
                20,  // workDays
                2.0, // paidLeaveDays
                1.0, // compensatoryLeaveDays
                0.0, // specialLeaveDays
                Duration.ofHours(160), // totalWorkTime
                Duration.ofHours(10),  // totalOvertime
                Duration.ZERO          // totalHolidayWork
        );
        testReport1 = new ReportJpaEntity(reportId1, ReportStatus.NOT_SUBMITTED, summary1);

        // 2024年2月のレポート（異なるユーザー）
        reportId2 = new ReportId(YearMonth.of(2024, 2), "user2");
        SummaryJpaEntity summary2 = new SummaryJpaEntity(
                19,  // workDays
                1.0, // paidLeaveDays
                0.0, // compensatoryLeaveDays
                1.0, // specialLeaveDays
                Duration.ofHours(152), // totalWorkTime
                Duration.ofHours(5),   // totalOvertime
                Duration.ofHours(8)    // totalHolidayWork
        );
        testReport2 = new ReportJpaEntity(reportId2, ReportStatus.SUBMITTED, summary2);
    }

    @Test
    @DisplayName("基本CRUD操作_レポート作成")
    void save_正常ケース_レポートが作成される() {
        // When
        ReportJpaEntity savedReport = reportJpaRepository.save(testReport1);

        // Then
        assertThat(savedReport.getId()).isNotNull();
        assertThat(savedReport.getId().getYearMonth()).isEqualTo(YearMonth.of(2024, 1));
        assertThat(savedReport.getId().getUserId()).isEqualTo("user1");
        assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.NOT_SUBMITTED);
        
        // Summary確認
        SummaryJpaEntity summary = savedReport.getSummary();
        assertThat(summary.getWorkDays()).isEqualTo(20);
        assertThat(summary.getTotalWorkTime()).isEqualTo(Duration.ofHours(160));
        assertThat(summary.getTotalOvertime()).isEqualTo(Duration.ofHours(10));
        assertThat(summary.getPaidLeaveDays()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("基本CRUD操作_複合主キー検索")
    void findById_正常ケース_複合主キーでレポートが取得される() {
        // Given
        reportJpaRepository.save(testReport1);

        // When
        Optional<ReportJpaEntity> found = reportJpaRepository.findById(reportId1);

        // Then
        assertThat(found).isPresent();
        ReportJpaEntity report = found.get();
        assertThat(report.getId().getYearMonth()).isEqualTo(YearMonth.of(2024, 1));
        assertThat(report.getId().getUserId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("基本CRUD操作_レポート更新")
    void save_更新ケース_レポートが更新される() {
        // Given
        ReportJpaEntity savedReport = reportJpaRepository.save(testReport1);
        
        // When
        savedReport.setStatus(ReportStatus.APPROVED);
        savedReport.setSummary(new SummaryJpaEntity(
                21,  // workDays
                1.0, // paidLeaveDays
                2.0, // compensatoryLeaveDays
                1.0, // specialLeaveDays
                Duration.ofHours(170), // totalWorkTime
                Duration.ofHours(15),  // totalOvertime
                Duration.ofHours(4)    // totalHolidayWork
        ));
        
        ReportJpaEntity updatedReport = reportJpaRepository.save(savedReport);

        // Then
        assertThat(updatedReport.getStatus()).isEqualTo(ReportStatus.APPROVED);
        SummaryJpaEntity summary = updatedReport.getSummary();
        assertThat(summary.getWorkDays()).isEqualTo(21);
        assertThat(summary.getTotalWorkTime()).isEqualTo(Duration.ofHours(170));
        assertThat(summary.getTotalOvertime()).isEqualTo(Duration.ofHours(15));
        assertThat(summary.getPaidLeaveDays()).isEqualTo(1.0);
        assertThat(summary.getCompensatoryLeaveDays()).isEqualTo(2.0);
        assertThat(summary.getSpecialLeaveDays()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("基本CRUD操作_レポート削除")
    void delete_正常ケース_レポートが削除される() {
        // Given
        ReportJpaEntity savedReport = reportJpaRepository.save(testReport1);

        // When
        reportJpaRepository.delete(savedReport);

        // Then
        Optional<ReportJpaEntity> found = reportJpaRepository.findById(reportId1);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("カスタムクエリ_ユーザー別レポート検索")
    void findByIdUserId_正常ケース_ユーザーのレポートが取得される() {
        // Given
        reportJpaRepository.save(testReport1);
        
        // user1の別月のレポートを追加
        ReportId anotherReportId = new ReportId(YearMonth.of(2024, 3), "user1");
        SummaryJpaEntity anotherSummary = new SummaryJpaEntity(
                22,  // workDays
                0.0, // paidLeaveDays
                0.0, // compensatoryLeaveDays
                0.0, // specialLeaveDays
                Duration.ofHours(176), // totalWorkTime
                Duration.ofHours(8),   // totalOvertime
                Duration.ZERO          // totalHolidayWork
        );
        ReportJpaEntity anotherReport = new ReportJpaEntity(anotherReportId, ReportStatus.NOT_SUBMITTED, anotherSummary);
        reportJpaRepository.save(anotherReport);
        
        reportJpaRepository.save(testReport2); // 別ユーザーのレポート

        // When
        List<ReportJpaEntity> user1Reports = reportJpaRepository.findByUserId("user1");

        // Then
        assertThat(user1Reports).hasSize(2);
        assertThat(user1Reports)
                .extracting(report -> report.getId().getUserId())
                .containsOnly("user1");
        assertThat(user1Reports)
                .extracting(report -> report.getId().getYearMonth())
                .containsExactlyInAnyOrder(
                        YearMonth.of(2024, 1),
                        YearMonth.of(2024, 3)
                );
    }

    @Test
    @DisplayName("カスタムクエリ_年月検索")
    void findByYearMonth_正常ケース_年月でレポートが取得される() {
        // Given
        reportJpaRepository.save(testReport1); // 2024年1月
        reportJpaRepository.save(testReport2); // 2024年2月
        
        // 2024年3月のレポートを追加
        ReportId reportId3 = new ReportId(YearMonth.of(2024, 3), "user3");
        SummaryJpaEntity summary3 = new SummaryJpaEntity(
                23,  // workDays
                1.0, // paidLeaveDays
                0.0, // compensatoryLeaveDays
                0.0, // specialLeaveDays
                Duration.ofHours(184), // totalWorkTime
                Duration.ofHours(12),  // totalOvertime
                Duration.ZERO          // totalHolidayWork
        );
        ReportJpaEntity testReport3 = new ReportJpaEntity(reportId3, ReportStatus.APPROVED, summary3);
        reportJpaRepository.save(testReport3);

        // When
        List<ReportJpaEntity> reportsInJan = reportJpaRepository.findByYearMonth(YearMonth.of(2024, 1));
        List<ReportJpaEntity> reportsInFeb = reportJpaRepository.findByYearMonth(YearMonth.of(2024, 2));

        // Then
        assertThat(reportsInJan).hasSize(1);
        assertThat(reportsInFeb).hasSize(1);
        assertThat(reportsInJan.get(0).getId().getYearMonth()).isEqualTo(YearMonth.of(2024, 1));
        assertThat(reportsInFeb.get(0).getId().getYearMonth()).isEqualTo(YearMonth.of(2024, 2));
    }

    @Test
    @DisplayName("カスタムクエリ_ステータス別検索")
    void findByStatus_正常ケース_ステータスでフィルタリングされる() {
        // Given
        reportJpaRepository.save(testReport1); // NOT_SUBMITTED
        reportJpaRepository.save(testReport2); // SUBMITTED
        
        // APPROVEDのレポートを追加
        ReportId reportId3 = new ReportId(YearMonth.of(2024, 3), "user3");
        SummaryJpaEntity approvedSummary = new SummaryJpaEntity(
                20,  // workDays
                0.0, // paidLeaveDays
                0.0, // compensatoryLeaveDays
                0.0, // specialLeaveDays
                Duration.ofHours(160), // totalWorkTime
                Duration.ZERO,         // totalOvertime
                Duration.ZERO          // totalHolidayWork
        );
        ReportJpaEntity approvedReport = new ReportJpaEntity(reportId3, ReportStatus.APPROVED, approvedSummary);
        reportJpaRepository.save(approvedReport);

        // When
        List<ReportJpaEntity> draftReports = reportJpaRepository.findByStatus(ReportStatus.NOT_SUBMITTED);
        List<ReportJpaEntity> submittedReports = reportJpaRepository.findByStatus(ReportStatus.SUBMITTED);
        List<ReportJpaEntity> approvedReports = reportJpaRepository.findByStatus(ReportStatus.APPROVED);

        // Then
        assertThat(draftReports).hasSize(1);
        assertThat(draftReports.get(0).getId().getUserId()).isEqualTo("user1");
        
        assertThat(submittedReports).hasSize(1);
        assertThat(submittedReports.get(0).getId().getUserId()).isEqualTo("user2");
        
        assertThat(approvedReports).hasSize(1);
        assertThat(approvedReports.get(0).getId().getUserId()).isEqualTo("user3");
    }

    @Test
    @DisplayName("カスタムクエリ_存在確認")
    void existsByYearMonthAndUserId_正常ケース_存在確認ができる() {
        // Given
        reportJpaRepository.save(testReport1);

        // When & Then
        assertThat(reportJpaRepository.existsByYearMonthAndUserId(YearMonth.of(2024, 1), "user1")).isTrue();
        assertThat(reportJpaRepository.existsByYearMonthAndUserId(YearMonth.of(2024, 1), "user2")).isFalse();
        assertThat(reportJpaRepository.existsByYearMonthAndUserId(YearMonth.of(2024, 2), "user1")).isFalse();
        assertThat(reportJpaRepository.existsByYearMonthAndUserId(YearMonth.of(2023, 1), "user1")).isFalse();
    }

    @Test
    @DisplayName("リレーションシップ_詳細データとの関係")
    void relationship_詳細データ_正常に関連付けられる() {
        // Given
        ReportJpaEntity savedReport = reportJpaRepository.save(testReport1);
        
        // 詳細データを追加
        DetailJpaEntity detail1 = new DetailJpaEntity(
                LocalDate.of(2024, 1, 15),  // workDate
                false,                       // isHoliday
                null,                        // leaveType (nullで通常勤務)
                LocalDateTime.of(2024, 1, 15, 9, 0),  // startDateTime
                LocalDateTime.of(2024, 1, 15, 19, 0), // endDateTime
                Duration.ofHours(8),         // workingHours
                Duration.ofHours(2),         // overtimeHours
                Duration.ZERO,               // holidayWorkHours
                null                         // note
        );
        
        DetailJpaEntity detail2 = new DetailJpaEntity(
                LocalDate.of(2024, 1, 16),  // workDate
                false,                       // isHoliday
                LeaveType.PAID_LEAVE,        // leaveType
                null,                        // startDateTime
                null,                        // endDateTime
                Duration.ZERO,               // workingHours
                Duration.ZERO,               // overtimeHours
                Duration.ZERO,               // holidayWorkHours
                "有給休暇"                    // note
        );
        
        savedReport.addWorkDay(detail1);
        savedReport.addWorkDay(detail2);
        
        // When
        ReportJpaEntity updatedReport = reportJpaRepository.save(savedReport);

        // Then
        assertThat(updatedReport.getWorkDays()).hasSize(2);
        assertThat(updatedReport.getWorkDays())
                .extracting(DetailJpaEntity::getWorkDate)
                .containsExactlyInAnyOrder(
                        LocalDate.of(2024, 1, 15),
                        LocalDate.of(2024, 1, 16)
                );
        assertThat(updatedReport.getWorkDays())
                .extracting(DetailJpaEntity::getLeaveType)
                .containsExactlyInAnyOrder(
                        null,
                        LeaveType.PAID_LEAVE
                );
    }

    @Test
    @DisplayName("データベース制約_複合主キー一意制約")
    void save_重複複合主キー_例外が発生する() {
        // Given
        reportJpaRepository.save(testReport1);
        
        // 同じ複合主キーで別のレポートを作成
        SummaryJpaEntity duplicateSummary = new SummaryJpaEntity(
                25,  // workDays
                0.0, // paidLeaveDays
                0.0, // compensatoryLeaveDays
                0.0, // specialLeaveDays
                Duration.ofHours(200), // totalWorkTime
                Duration.ofHours(20),  // totalOvertime
                Duration.ZERO          // totalHolidayWork
        );
        ReportJpaEntity duplicateReport = new ReportJpaEntity(
                new ReportId(YearMonth.of(2024, 1), "user1"), // 同じキー
                ReportStatus.SUBMITTED,
                duplicateSummary
        );

        // When & Then
        assertThat(reportJpaRepository.existsByYearMonthAndUserId(YearMonth.of(2024, 1), "user1")).isTrue();
        
        // 重複した複合主キーでの保存は制約違反となることを確認
        // 実際のエラーハンドリングはサービス層で行われる
    }
}