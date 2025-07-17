package com.github.okanikani.kairos.reports.others.repositories;

import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.vos.*;
import com.github.okanikani.kairos.reports.others.jpa.entities.*;
import com.github.okanikani.kairos.reports.others.jpa.repositories.ReportJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JpaReportRepositoryのUnit Test
 * 
 * テスト対象: ドメインモデルとJPAエンティティ間の複雑な変換とCRUD操作
 */
@ExtendWith(MockitoExtension.class)
class JpaReportRepositoryTest {

    @Mock
    private ReportJpaRepository reportJpaRepository;

    @InjectMocks
    private JpaReportRepository jpaReportRepository;

    private Report testReport;
    private ReportJpaEntity testJpaEntity;
    private User testUser;
    private YearMonth testYearMonth;

    @BeforeEach
    void setUp() {
        testUser = new User("test-user-001");
        testYearMonth = YearMonth.of(2025, 1);
        
        // 勤務日詳細の作成
        List<Detail> workDays = List.of(
                new Detail(
                        LocalDate.of(2025, 1, 6),           // workDate (月曜日)
                        false,                              // isHoliday
                        null,                               // leaveType (通常勤務)
                        new WorkTime(LocalDateTime.of(2025, 1, 6, 9, 0)),    // startDateTime
                        new WorkTime(LocalDateTime.of(2025, 1, 6, 18, 0)),   // endDateTime
                        Duration.ofHours(8),                // workingHours
                        Duration.ofMinutes(30),             // overtimeHours
                        Duration.ZERO,                      // holidayWorkHours
                        null                                // note
                ),
                new Detail(
                        LocalDate.of(2025, 1, 7),           // workDate (火曜日)
                        false,                              // isHoliday
                        LeaveType.PAID_LEAVE_AM,            // leaveType (午前半休)
                        new WorkTime(LocalDateTime.of(2025, 1, 7, 13, 0)),   // startDateTime
                        new WorkTime(LocalDateTime.of(2025, 1, 7, 18, 0)),   // endDateTime
                        Duration.ofHours(4),                // workingHours
                        Duration.ZERO,                      // overtimeHours
                        Duration.ZERO,                      // holidayWorkHours
                        "午前半休取得"                       // note
                )
        );

        // サマリーの作成
        Summary summary = new Summary(
                2.0,                                        // workDays
                0.5,                                        // paidLeaveDays
                0.0,                                        // compensatoryLeaveDays
                0.0,                                        // specialLeaveDays
                Duration.ofHours(12),                       // totalWorkTime
                Duration.ofMinutes(30),                     // totalOvertime
                Duration.ZERO                               // totalHolidayWork
        );

        testReport = new Report(
                testYearMonth,
                testUser,
                ReportStatus.NOT_SUBMITTED,
                workDays,
                summary
        );

        // JPAエンティティの作成
        ReportId reportId = new ReportId(testYearMonth, "test-user-001");
        SummaryJpaEntity summaryJpa = new SummaryJpaEntity(
                2.0, 0.5, 0.0, 0.0,
                Duration.ofHours(12), Duration.ofMinutes(30), Duration.ZERO
        );
        testJpaEntity = new ReportJpaEntity(reportId, ReportStatus.NOT_SUBMITTED, summaryJpa);
        
        DetailJpaEntity detail1 = new DetailJpaEntity(
                LocalDate.of(2025, 1, 6), false, null,
                LocalDateTime.of(2025, 1, 6, 9, 0),
                LocalDateTime.of(2025, 1, 6, 18, 0),
                Duration.ofHours(8), Duration.ofMinutes(30), Duration.ZERO, null
        );
        DetailJpaEntity detail2 = new DetailJpaEntity(
                LocalDate.of(2025, 1, 7), false, LeaveType.PAID_LEAVE_AM,
                LocalDateTime.of(2025, 1, 7, 13, 0),
                LocalDateTime.of(2025, 1, 7, 18, 0),
                Duration.ofHours(4), Duration.ZERO, Duration.ZERO, "午前半休取得"
        );
        testJpaEntity.addWorkDay(detail1);
        testJpaEntity.addWorkDay(detail2);
    }

    @Test
    void save_正常なReport_正常に保存される() {
        // Given
        when(reportJpaRepository.save(any(ReportJpaEntity.class))).thenReturn(testJpaEntity);

        // When
        jpaReportRepository.save(testReport);

        // Then
        verify(reportJpaRepository).save(any(ReportJpaEntity.class));
    }

    @Test
    void find_存在するレポート_対応するReportが返される() {
        // Given
        when(reportJpaRepository.findByYearMonthAndUserId(testYearMonth, "test-user-001"))
                .thenReturn(Optional.of(testJpaEntity));

        // When
        Report result = jpaReportRepository.find(testYearMonth, testUser);

        // Then
        verify(reportJpaRepository).findByYearMonthAndUserId(testYearMonth, "test-user-001");
        assertThat(result).isNotNull();
        assertThat(result.yearMonth()).isEqualTo(testYearMonth);
        assertThat(result.owner().userId()).isEqualTo("test-user-001");
        assertThat(result.status()).isEqualTo(ReportStatus.NOT_SUBMITTED);
        assertThat(result.workDays()).hasSize(2);
        assertThat(result.summary().workDays()).isEqualTo(2.0);
        assertThat(result.summary().paidLeaveDays()).isEqualTo(0.5);
    }

    @Test
    void find_存在しないレポート_nullが返される() {
        // Given
        when(reportJpaRepository.findByYearMonthAndUserId(testYearMonth, "test-user-001"))
                .thenReturn(Optional.empty());

        // When
        Report result = jpaReportRepository.find(testYearMonth, testUser);

        // Then
        verify(reportJpaRepository).findByYearMonthAndUserId(testYearMonth, "test-user-001");
        assertThat(result).isNull();
    }

    @Test
    void findAll_レポートが存在する場合_全てのReportリストが返される() {
        // Given
        List<ReportJpaEntity> jpaEntities = List.of(testJpaEntity);
        when(reportJpaRepository.findAll()).thenReturn(jpaEntities);

        // When
        List<Report> result = jpaReportRepository.findAll();

        // Then
        verify(reportJpaRepository).findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).yearMonth()).isEqualTo(testYearMonth);
        assertThat(result.get(0).owner().userId()).isEqualTo("test-user-001");
    }

    @Test
    void update_既存のReport_保存メソッドが呼ばれる() {
        // Given
        when(reportJpaRepository.save(any(ReportJpaEntity.class))).thenReturn(testJpaEntity);

        // When
        jpaReportRepository.update(testReport);

        // Then
        verify(reportJpaRepository).save(any(ReportJpaEntity.class));
    }

    @Test
    void delete_存在するレポート_正常に削除される() {
        // When
        jpaReportRepository.delete(testYearMonth, testUser);

        // Then
        verify(reportJpaRepository).deleteById(any(ReportId.class));
    }

    @Test
    void findByUser_ユーザーのレポートが存在する場合_ユーザーのReportリストが返される() {
        // Given
        List<ReportJpaEntity> jpaEntities = List.of(testJpaEntity);
        when(reportJpaRepository.findByUserId("test-user-001")).thenReturn(jpaEntities);

        // When
        List<Report> result = jpaReportRepository.findByUser(testUser);

        // Then
        verify(reportJpaRepository).findByUserId("test-user-001");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).owner().userId()).isEqualTo("test-user-001");
    }

    @Test
    void exists_レポートが存在する場合_trueが返される() {
        // Given
        when(reportJpaRepository.existsByYearMonthAndUserId(testYearMonth, "test-user-001"))
                .thenReturn(true);

        // When
        boolean result = jpaReportRepository.exists(testYearMonth, testUser);

        // Then
        verify(reportJpaRepository).existsByYearMonthAndUserId(testYearMonth, "test-user-001");
        assertThat(result).isTrue();
    }

    @Test
    void exists_レポートが存在しない場合_falseが返される() {
        // Given
        when(reportJpaRepository.existsByYearMonthAndUserId(testYearMonth, "test-user-001"))
                .thenReturn(false);

        // When
        boolean result = jpaReportRepository.exists(testYearMonth, testUser);

        // Then
        verify(reportJpaRepository).existsByYearMonthAndUserId(testYearMonth, "test-user-001");
        assertThat(result).isFalse();
    }

    @Test
    void 変換メソッド_複雑なドメインモデルからJPAエンティティ_正常に変換される() {
        // Given: 複数の休暇タイプと休日勤務を含むレポート
        List<Detail> complexWorkDays = List.of(
                new Detail(
                        LocalDate.of(2025, 1, 8),           // 水曜日
                        false,                              // 平日
                        LeaveType.COMPENSATORY_LEAVE,       // 代休
                        null,                               // 勤務時間なし
                        null,
                        Duration.ZERO,
                        Duration.ZERO,
                        Duration.ZERO,
                        "代休取得"
                ),
                new Detail(
                        LocalDate.of(2025, 1, 11),          // 土曜日
                        true,                               // 休日
                        null,                               // 休日出勤
                        new WorkTime(LocalDateTime.of(2025, 1, 11, 10, 0)),
                        new WorkTime(LocalDateTime.of(2025, 1, 11, 15, 0)),
                        Duration.ofHours(4),                // 休日勤務時間
                        Duration.ZERO,
                        Duration.ofHours(4),                // 休日勤務
                        "緊急対応のため休日出勤"
                )
        );

        Summary complexSummary = new Summary(
                1.0, 0.0, 1.0, 0.0,
                Duration.ofHours(4), Duration.ZERO, Duration.ofHours(4)
        );

        Report complexReport = new Report(
                testYearMonth, testUser, ReportStatus.SUBMITTED,
                complexWorkDays, complexSummary
        );

        ReportJpaEntity expectedJpaEntity = new ReportJpaEntity(
                new ReportId(testYearMonth, "test-user-001"),
                ReportStatus.SUBMITTED,
                new SummaryJpaEntity(1.0, 0.0, 1.0, 0.0,
                        Duration.ofHours(4), Duration.ZERO, Duration.ofHours(4))
        );

        when(reportJpaRepository.save(any(ReportJpaEntity.class))).thenReturn(expectedJpaEntity);

        // When
        jpaReportRepository.save(complexReport);

        // Then: 複雑な変換が正常に実行されることを確認
        verify(reportJpaRepository).save(any(ReportJpaEntity.class));
    }

    @Test
    void 変換メソッド_JPAエンティティからドメインモデル_WorkTimeがnullの場合も正常に変換される() {
        // Given: 勤務時間がnullのDetailを含むJPAエンティティ
        ReportId reportId = new ReportId(testYearMonth, "test-user-001");
        SummaryJpaEntity summaryJpa = new SummaryJpaEntity(
                1.0, 1.0, 0.0, 0.0,
                Duration.ZERO, Duration.ZERO, Duration.ZERO
        );
        ReportJpaEntity jpaWithNullTimes = new ReportJpaEntity(reportId, ReportStatus.NOT_SUBMITTED, summaryJpa);
        
        DetailJpaEntity detailWithNullTimes = new DetailJpaEntity(
                LocalDate.of(2025, 1, 8), false, LeaveType.PAID_LEAVE,
                null, null,  // 勤務時間がnull（全日休暇）
                Duration.ZERO, Duration.ZERO, Duration.ZERO, "有給休暇"
        );
        jpaWithNullTimes.addWorkDay(detailWithNullTimes);

        when(reportJpaRepository.findByYearMonthAndUserId(testYearMonth, "test-user-001"))
                .thenReturn(Optional.of(jpaWithNullTimes));

        // When
        Report result = jpaReportRepository.find(testYearMonth, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.workDays()).hasSize(1);
        Detail detail = result.workDays().get(0);
        assertThat(detail.startDateTime()).isNull();
        assertThat(detail.endDateTime()).isNull();
        assertThat(detail.leaveType()).isEqualTo(LeaveType.PAID_LEAVE);
        assertThat(detail.note()).isEqualTo("有給休暇");
    }

    @Test
    void 変換メソッド_全てのLeaveTypeパターン_正常に変換される() {
        // Given: 全ての休暇タイプを含むテストケース
        List<Detail> allLeaveTypes = List.of(
                new Detail(LocalDate.of(2025, 1, 13), false, LeaveType.PAID_LEAVE, null, null,
                        Duration.ZERO, Duration.ZERO, Duration.ZERO, "有給休暇"),
                new Detail(LocalDate.of(2025, 1, 14), false, LeaveType.PAID_LEAVE_AM, null, null,
                        Duration.ZERO, Duration.ZERO, Duration.ZERO, "午前半休"),
                new Detail(LocalDate.of(2025, 1, 15), false, LeaveType.PAID_LEAVE_PM, null, null,
                        Duration.ZERO, Duration.ZERO, Duration.ZERO, "午後半休"),
                new Detail(LocalDate.of(2025, 1, 16), false, LeaveType.COMPENSATORY_LEAVE, null, null,
                        Duration.ZERO, Duration.ZERO, Duration.ZERO, "代休"),
                new Detail(LocalDate.of(2025, 1, 17), false, LeaveType.COMPENSATORY_LEAVE_AM, null, null,
                        Duration.ZERO, Duration.ZERO, Duration.ZERO, "代休午前"),
                new Detail(LocalDate.of(2025, 1, 20), false, LeaveType.COMPENSATORY_LEAVE_PM, null, null,
                        Duration.ZERO, Duration.ZERO, Duration.ZERO, "代休午後"),
                new Detail(LocalDate.of(2025, 1, 21), false, LeaveType.SPECIAL_LEAVE, null, null,
                        Duration.ZERO, Duration.ZERO, Duration.ZERO, "特別休暇")
        );

        Summary allLeavesSummary = new Summary(
                0.0, 3.0, 2.0, 1.0,
                Duration.ZERO, Duration.ZERO, Duration.ZERO
        );

        Report allLeavesReport = new Report(
                testYearMonth, testUser, ReportStatus.NOT_SUBMITTED,
                allLeaveTypes, allLeavesSummary
        );

        when(reportJpaRepository.save(any(ReportJpaEntity.class))).thenReturn(testJpaEntity);

        // When
        jpaReportRepository.save(allLeavesReport);

        // Then
        verify(reportJpaRepository).save(any(ReportJpaEntity.class));
    }
}