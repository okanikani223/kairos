package com.github.okanikani.kairos.reports.domains.models.entities;

import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Reportエンティティのテスト
 */
@DisplayName("Reportエンティティのテスト")
class ReportTest {

    @Nested
    @DisplayName("コンストラクタのテスト")
    class ConstructorTest {

        @Test
        @DisplayName("正常系_全ての必須パラメータが設定されている場合_正常にインスタンスが生成される")
        void 正常系_全ての必須パラメータが設定されている場合_正常にインスタンスが生成される() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            User owner = new User("testuser");
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                    new Report(yearMonth, owner, status, workDays, summary));
        }

        @Test
        @DisplayName("異常系_yearMonthがnullの場合_NullPointerExceptionが発生する")
        void 異常系_yearMonthがnullの場合_NullPointerExceptionが発生する() {
            // Arrange
            User owner = new User("testuser");
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            // Act & Assert
            assertThatThrownBy(() ->
                    new Report(null, owner, status, workDays, summary))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("yearMonthは必須です");
        }

        @Test
        @DisplayName("異常系_ownerがnullの場合_NullPointerExceptionが発生する")
        void 異常系_ownerがnullの場合_NullPointerExceptionが発生する() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            // Act & Assert
            assertThatThrownBy(() ->
                    new Report(yearMonth, null, status, workDays, summary))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("ownerは必須です");
        }

        @Test
        @DisplayName("異常系_statusがnullの場合_NullPointerExceptionが発生する")
        void 異常系_statusがnullの場合_NullPointerExceptionが発生する() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            User owner = new User("testuser");
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            // Act & Assert
            assertThatThrownBy(() ->
                    new Report(yearMonth, owner, null, workDays, summary))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("statusは必須です");
        }

        @Test
        @DisplayName("異常系_workDaysがnullの場合_NullPointerExceptionが発生する")
        void 異常系_workDaysがnullの場合_NullPointerExceptionが発生する() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            User owner = new User("testuser");
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            Summary summary = Summary.EMPTY;

            // Act & Assert
            assertThatThrownBy(() ->
                    new Report(yearMonth, owner, status, null, summary))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("workDaysは必須です");
        }

        @Test
        @DisplayName("異常系_summaryがnullの場合_NullPointerExceptionが発生する")
        void 異常系_summaryがnullの場合_NullPointerExceptionが発生する() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            User owner = new User("testuser");
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();

            // Act & Assert
            assertThatThrownBy(() ->
                    new Report(yearMonth, owner, status, workDays, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("summaryは必須です");
        }
    }

    @Nested
    @DisplayName("フィールドアクセスのテスト")
    class FieldAccessTest {

        @Test
        @DisplayName("yearMonth取得_正常に取得できる")
        void yearMonth取得_正常に取得できる() {
            // Arrange
            YearMonth expectedYearMonth = YearMonth.of(2025, 1);
            Report report = createDefaultReport(expectedYearMonth, ReportStatus.NOT_SUBMITTED);

            // Act
            YearMonth actualYearMonth = report.yearMonth();

            // Assert
            assertThat(actualYearMonth).isEqualTo(expectedYearMonth);
        }

        @Test
        @DisplayName("owner取得_正常に取得できる")
        void owner取得_正常に取得できる() {
            // Arrange
            User expectedOwner = new User("testuser");
            Report report = new Report(
                    YearMonth.of(2025, 1),
                    expectedOwner,
                    ReportStatus.NOT_SUBMITTED,
                    new ArrayList<>(),
                    Summary.EMPTY
            );

            // Act
            User actualOwner = report.owner();

            // Assert
            assertThat(actualOwner).isEqualTo(expectedOwner);
        }

        @ParameterizedTest
        @EnumSource(ReportStatus.class)
        @DisplayName("status取得_全てのステータスで正常に取得できる")
        void status取得_全てのステータスで正常に取得できる(ReportStatus expectedStatus) {
            // Arrange
            Report report = createDefaultReport(YearMonth.of(2025, 1), expectedStatus);

            // Act
            ReportStatus actualStatus = report.status();

            // Assert
            assertThat(actualStatus).isEqualTo(expectedStatus);
        }

        @Test
        @DisplayName("workDays取得_正常に取得できる")
        void workDays取得_正常に取得できる() {
            // Arrange
            List<Detail> expectedWorkDays = new ArrayList<>();
            Report report = new Report(
                    YearMonth.of(2025, 1),
                    new User("testuser"),
                    ReportStatus.NOT_SUBMITTED,
                    expectedWorkDays,
                    Summary.EMPTY
            );

            // Act
            List<Detail> actualWorkDays = report.workDays();

            // Assert
            assertThat(actualWorkDays).isSameAs(expectedWorkDays);
        }

        @Test
        @DisplayName("summary取得_正常に取得できる")
        void summary取得_正常に取得できる() {
            // Arrange
            Summary expectedSummary = Summary.EMPTY;
            Report report = new Report(
                    YearMonth.of(2025, 1),
                    new User("testuser"),
                    ReportStatus.NOT_SUBMITTED,
                    new ArrayList<>(),
                    expectedSummary
            );

            // Act
            Summary actualSummary = report.summary();

            // Assert
            assertThat(actualSummary).isEqualTo(expectedSummary);
        }
    }

    @Nested
    @DisplayName("境界値テスト")
    class BoundaryTest {

        @Test
        @DisplayName("年月の境界値_最小値2000年1月_正常にインスタンスが生成される")
        void 年月の境界値_最小値2000年1月_正常にインスタンスが生成される() {
            // Arrange
            YearMonth minYearMonth = YearMonth.of(2000, 1);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                    createDefaultReport(minYearMonth, ReportStatus.NOT_SUBMITTED));
        }

        @Test
        @DisplayName("年月の境界値_最大値2099年12月_正常にインスタンスが生成される")
        void 年月の境界値_最大値2099年12月_正常にインスタンスが生成される() {
            // Arrange
            YearMonth maxYearMonth = YearMonth.of(2099, 12);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                    createDefaultReport(maxYearMonth, ReportStatus.NOT_SUBMITTED));
        }

        @Test
        @DisplayName("workDaysの境界値_空のリスト_正常にインスタンスが生成される")
        void workDaysの境界値_空のリスト_正常にインスタンスが生成される() {
            // Arrange
            List<Detail> emptyWorkDays = new ArrayList<>();

            // Act
            Report report = new Report(
                    YearMonth.of(2025, 1),
                    new User("testuser"),
                    ReportStatus.NOT_SUBMITTED,
                    emptyWorkDays,
                    Summary.EMPTY
            );

            // Assert
            assertThat(report.workDays()).isEmpty();
        }
    }

    @Nested
    @DisplayName("不変性のテスト")
    class ImmutabilityTest {

        @Test
        @DisplayName("workDaysリストの変更_元のリストを変更するとReportのworkDaysも変更される")
        void workDaysリストの変更_元のリストを変更するとReportのworkDaysも変更される() {
            // Arrange
            List<Detail> originalWorkDays = new ArrayList<>();
            Report report = new Report(
                    YearMonth.of(2025, 1),
                    new User("testuser"),
                    ReportStatus.NOT_SUBMITTED,
                    originalWorkDays,
                    Summary.EMPTY
            );

            // Act
            originalWorkDays.add(null); // 元のリストに要素を追加

            // Assert
            assertThat(report.workDays()).hasSize(1); // Reportのリストも変更される（Recordの特性）
        }
    }

    @Nested
    @DisplayName("equalsとhashCodeのテスト")
    class EqualsAndHashCodeTest {

        @Test
        @DisplayName("equals_同じ値を持つ2つのインスタンス_trueを返す")
        void equals_同じ値を持つ2つのインスタンス_trueを返す() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            User owner = new User("testuser");
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            Report report1 = new Report(yearMonth, owner, status, workDays, summary);
            Report report2 = new Report(yearMonth, owner, status, workDays, summary);

            // Act & Assert
            assertThat(report1).isEqualTo(report2);
            assertThat(report1.hashCode()).isEqualTo(report2.hashCode());
        }

        @Test
        @DisplayName("equals_異なるyearMonthを持つ2つのインスタンス_falseを返す")
        void equals_異なるyearMonthを持つ2つのインスタンス_falseを返す() {
            // Arrange
            User owner = new User("testuser");
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            Report report1 = new Report(YearMonth.of(2025, 1), owner, status, workDays, summary);
            Report report2 = new Report(YearMonth.of(2025, 2), owner, status, workDays, summary);

            // Act & Assert
            assertThat(report1).isNotEqualTo(report2);
        }

        @Test
        @DisplayName("equals_異なるownerを持つ2つのインスタンス_falseを返す")
        void equals_異なるownerを持つ2つのインスタンス_falseを返す() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            Report report1 = new Report(yearMonth, new User("user1"), status, workDays, summary);
            Report report2 = new Report(yearMonth, new User("user2"), status, workDays, summary);

            // Act & Assert
            assertThat(report1).isNotEqualTo(report2);
        }
    }

    @Nested
    @DisplayName("toStringのテスト")
    class ToStringTest {

        @Test
        @DisplayName("toString_全てのフィールドが含まれる")
        void toString_全てのフィールドが含まれる() {
            // Arrange
            YearMonth yearMonth = YearMonth.of(2025, 1);
            User owner = new User("testuser");
            ReportStatus status = ReportStatus.NOT_SUBMITTED;
            List<Detail> workDays = new ArrayList<>();
            Summary summary = Summary.EMPTY;

            Report report = new Report(yearMonth, owner, status, workDays, summary);

            // Act
            String result = report.toString();

            // Assert
            assertThat(result)
                    .contains("yearMonth=2025-01")
                    .contains("owner=User[userId=testuser]")
                    .contains("status=NOT_SUBMITTED")
                    .contains("workDays=[]")
                    .contains("summary=");
        }
    }

    // ヘルパーメソッド
    private Report createDefaultReport(YearMonth yearMonth, ReportStatus status) {
        return new Report(
                yearMonth,
                new User("testuser"),
                status,
                new ArrayList<>(),
                Summary.EMPTY
        );
    }
}