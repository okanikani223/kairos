package com.github.okanikani.kairos.reports.domains.service;

import com.github.okanikani.kairos.reports.domains.models.constants.LeaveType;
import com.github.okanikani.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.WorkTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * SummaryFactoryのテスト
 */
@DisplayName("SummaryFactoryのテスト")
class SummaryFactoryTest {

    @Nested
    @DisplayName("from メソッドのテスト")
    class FromMethodTest {

        @Test
        @DisplayName("正常系_空のDetailリストの場合_EMPTYサマリが返される")
        void 正常系_空のDetailリストの場合_EMPTYサマリが返される() {
            // Arrange
            List<Detail> emptyDetails = new ArrayList<>();

            // Act
            Summary result = SummaryFactory.from(emptyDetails);

            // Assert
            assertThat(result).isEqualTo(Summary.EMPTY);
        }

        @Test
        @DisplayName("正常系_nullのDetailリストの場合_EMPTYサマリが返される")
        void 正常系_nullのDetailリストの場合_EMPTYサマリが返される() {
            // Act
            Summary result = SummaryFactory.from(null);

            // Assert
            assertThat(result).isEqualTo(Summary.EMPTY);
        }

        @Test
        @DisplayName("正常系_通常勤務のみの場合_正しくサマリが計算される")
        void 正常系_通常勤務のみの場合_正しくサマリが計算される() {
            // Arrange
            List<Detail> details = Arrays.asList(
                    createNormalWorkDetail(LocalDate.of(2025, 1, 1), Duration.ofHours(8)),
                    createNormalWorkDetail(LocalDate.of(2025, 1, 2), Duration.ofHours(8)),
                    createNormalWorkDetail(LocalDate.of(2025, 1, 3), Duration.ofHours(8))
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.workDays()).isEqualTo(3.0);
            assertThat(result.paidLeaveDays()).isEqualTo(0.0);
            assertThat(result.compensatoryLeaveDays()).isEqualTo(0.0);
            assertThat(result.specialLeaveDays()).isEqualTo(0.0);
            assertThat(result.totalWorkTime()).isEqualTo(Duration.ofHours(24));
            assertThat(result.totalOvertime()).isEqualTo(Duration.ZERO);
            assertThat(result.totalHolidayWork()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("正常系_残業ありの勤務の場合_正しくサマリが計算される")
        void 正常系_残業ありの勤務の場合_正しくサマリが計算される() {
            // Arrange
            List<Detail> details = Arrays.asList(
                    createWorkDetailWithOvertime(
                            LocalDate.of(2025, 1, 1),
                            Duration.ofHours(8),
                            Duration.ofHours(2)
                    ),
                    createWorkDetailWithOvertime(
                            LocalDate.of(2025, 1, 2),
                            Duration.ofHours(8),
                            Duration.ofHours(1)
                    )
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.workDays()).isEqualTo(2.0);
            assertThat(result.totalWorkTime()).isEqualTo(Duration.ofHours(16));
            assertThat(result.totalOvertime()).isEqualTo(Duration.ofHours(3));
            assertThat(result.totalHolidayWork()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("正常系_休日出勤ありの場合_正しくサマリが計算される")
        void 正常系_休日出勤ありの場合_正しくサマリが計算される() {
            // Arrange
            List<Detail> details = Arrays.asList(
                    createNormalWorkDetail(LocalDate.of(2025, 1, 1), Duration.ofHours(8)),
                    createHolidayWorkDetail(
                            LocalDate.of(2025, 1, 4),
                            Duration.ofHours(4)
                    )
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.workDays()).isEqualTo(2.0);
            assertThat(result.totalWorkTime()).isEqualTo(Duration.ofHours(8));
            assertThat(result.totalOvertime()).isEqualTo(Duration.ZERO);
            assertThat(result.totalHolidayWork()).isEqualTo(Duration.ofHours(4));
        }

        @ParameterizedTest
        @MethodSource("有給休暇テストケース")
        @DisplayName("正常系_有給休暇の場合_正しく日数が計算される")
        void 正常系_有給休暇の場合_正しく日数が計算される(LeaveType leaveType, double expectedDays) {
            // Arrange
            List<Detail> details = Collections.singletonList(
                    createLeaveDetail(LocalDate.of(2025, 1, 1), leaveType)
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.paidLeaveDays()).isEqualTo(expectedDays);
            assertThat(result.workDays()).isEqualTo(1.0 - expectedDays);
        }

        static Stream<Arguments> 有給休暇テストケース() {
            return Stream.of(
                    Arguments.of(LeaveType.PAID_LEAVE, 1.0),
                    Arguments.of(LeaveType.PAID_LEAVE_AM, 0.5),
                    Arguments.of(LeaveType.PAID_LEAVE_PM, 0.5)
            );
        }

        @ParameterizedTest
        @MethodSource("代休テストケース")
        @DisplayName("正常系_代休の場合_正しく日数が計算される")
        void 正常系_代休の場合_正しく日数が計算される(LeaveType leaveType, double expectedDays) {
            // Arrange
            List<Detail> details = Collections.singletonList(
                    createLeaveDetail(LocalDate.of(2025, 1, 1), leaveType)
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.compensatoryLeaveDays()).isEqualTo(expectedDays);
            assertThat(result.workDays()).isEqualTo(1.0 - expectedDays);
        }

        static Stream<Arguments> 代休テストケース() {
            return Stream.of(
                    Arguments.of(LeaveType.COMPENSATORY_LEAVE, 1.0),
                    Arguments.of(LeaveType.COMPENSATORY_LEAVE_AM, 0.5),
                    Arguments.of(LeaveType.COMPENSATORY_LEAVE_PM, 0.5)
            );
        }

        @Test
        @DisplayName("正常系_特別休暇の場合_正しく日数が計算される")
        void 正常系_特別休暇の場合_正しく日数が計算される() {
            // Arrange
            List<Detail> details = Arrays.asList(
                    createLeaveDetail(LocalDate.of(2025, 1, 1), LeaveType.SPECIAL_LEAVE),
                    createLeaveDetail(LocalDate.of(2025, 1, 2), LeaveType.SPECIAL_LEAVE)
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.specialLeaveDays()).isEqualTo(2.0);
            assertThat(result.workDays()).isEqualTo(0.0); // 2日分の特別休暇を減算
        }

        @Test
        @DisplayName("正常系_複合的なパターンの場合_正しくサマリが計算される")
        void 正常系_複合的なパターンの場合_正しくサマリが計算される() {
            // Arrange
            List<Detail> details = Arrays.asList(
                    // 通常勤務
                    createNormalWorkDetail(LocalDate.of(2025, 1, 1), Duration.ofHours(8)),
                    createNormalWorkDetail(LocalDate.of(2025, 1, 2), Duration.ofHours(8)),
                    
                    // 残業あり勤務
                    createWorkDetailWithOvertime(
                            LocalDate.of(2025, 1, 3),
                            Duration.ofHours(8),
                            Duration.ofHours(2)
                    ),
                    
                    // 有給休暇（全日）
                    createLeaveDetail(LocalDate.of(2025, 1, 6), LeaveType.PAID_LEAVE),
                    
                    // 有給休暇（午前半休）
                    createLeaveDetail(LocalDate.of(2025, 1, 7), LeaveType.PAID_LEAVE_AM),
                    
                    // 代休（全日）
                    createLeaveDetail(LocalDate.of(2025, 1, 8), LeaveType.COMPENSATORY_LEAVE),
                    
                    // 特別休暇
                    createLeaveDetail(LocalDate.of(2025, 1, 9), LeaveType.SPECIAL_LEAVE),
                    
                    // 休日出勤
                    createHolidayWorkDetail(LocalDate.of(2025, 1, 11), Duration.ofHours(4))
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.workDays()).isEqualTo(4.5); // 8日 - 1日(有給) - 0.5日(有給AM) - 1日(代休) - 1日(特休) = 4.5日
            assertThat(result.paidLeaveDays()).isEqualTo(1.5); // 1日 + 0.5日
            assertThat(result.compensatoryLeaveDays()).isEqualTo(1.0);
            assertThat(result.specialLeaveDays()).isEqualTo(1.0);
            assertThat(result.totalWorkTime()).isEqualTo(Duration.ofHours(24)); // 8 + 8 + 8 = 24時間
            assertThat(result.totalOvertime()).isEqualTo(Duration.ofHours(2));
            assertThat(result.totalHolidayWork()).isEqualTo(Duration.ofHours(4));
        }

        @Test
        @DisplayName("正常系_休日かつ休暇申請なしの場合_出勤扱いとなる")
        void 正常系_休日かつ休暇申請なしの場合_出勤扱いとなる() {
            // Arrange
            List<Detail> details = Collections.singletonList(
                    createHolidayDetailWithoutLeave(LocalDate.of(2025, 1, 11))
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.workDays()).isEqualTo(1.0); // 休日だが休暇申請がないため出勤扱い
        }

        @Test
        @DisplayName("境界値テスト_nullの時間要素が含まれる場合_適切に処理される")
        void 境界値テスト_nullの時間要素が含まれる場合_適切に処理される() {
            // Arrange
            List<Detail> details = Collections.singletonList(
                    createDetailWithNullTimes(LocalDate.of(2025, 1, 1))
            );

            // Act
            Summary result = SummaryFactory.from(details);

            // Assert
            assertThat(result.workDays()).isEqualTo(1.0);
            assertThat(result.totalWorkTime()).isEqualTo(Duration.ZERO);
            assertThat(result.totalOvertime()).isEqualTo(Duration.ZERO);
            assertThat(result.totalHolidayWork()).isEqualTo(Duration.ZERO);
        }
    }

    // ヘルパーメソッド
    private Detail createNormalWorkDetail(LocalDate workDate, Duration workingHours) {
        return new Detail(
                workDate,
                false, // 休日ではない
                null,  // 休暇種別なし
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(9, 0))),
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(17, 0))),
                workingHours,
                Duration.ZERO,
                Duration.ZERO,
                null
        );
    }

    private Detail createWorkDetailWithOvertime(LocalDate workDate, Duration workingHours, Duration overtimeHours) {
        return new Detail(
                workDate,
                false,
                null,
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(9, 0))),
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(19, 0))),
                workingHours,
                overtimeHours,
                Duration.ZERO,
                null
        );
    }

    private Detail createHolidayWorkDetail(LocalDate workDate, Duration holidayWorkHours) {
        return new Detail(
                workDate,
                true, // 休日
                null,
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(10, 0))),
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(14, 0))),
                Duration.ZERO,
                Duration.ZERO,
                holidayWorkHours,
                null
        );
    }

    private Detail createLeaveDetail(LocalDate workDate, LeaveType leaveType) {
        return new Detail(
                workDate,
                false,
                leaveType,
                null, // 休暇のため勤務時間なし
                null,
                Duration.ZERO,
                Duration.ZERO,
                Duration.ZERO,
                null
        );
    }

    private Detail createHolidayDetailWithoutLeave(LocalDate workDate) {
        return new Detail(
                workDate,
                true, // 休日
                null, // 休暇申請なし
                null,
                null,
                Duration.ZERO,
                Duration.ZERO,
                Duration.ZERO,
                null
        );
    }

    private Detail createDetailWithNullTimes(LocalDate workDate) {
        return new Detail(
                workDate,
                false,
                null,
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(9, 0))),
                new WorkTime(LocalDateTime.of(workDate, LocalTime.of(17, 0))),
                null, // null時間
                null, // null時間
                null, // null時間
                null
        );
    }
}