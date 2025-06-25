package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.reports.applications.usecases.dto.GenerateReportFromLocationRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.dto.UserDto;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.service.LocationService;
import com.github.okanikani.kairos.reports.domains.service.ReportPeriodCalculator;
import com.github.okanikani.kairos.reports.domains.service.WorkRuleResolverService;
import com.github.okanikani.kairos.reports.domains.roundings.MinuteBasedRoundingSetting;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

class GenerateReportFromLocationUsecaseTest {

    private GenerateReportFromLocationUsecase generateReportFromLocationUsecase;

    @Mock
    private LocationService locationService;

    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private WorkRuleResolverService workRuleResolverService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generateReportFromLocationUsecase = new GenerateReportFromLocationUsecase(
            locationService, 
            reportRepository,
            workRuleResolverService
        );
    }
    
    private void setupDefaultWorkRuleMocks(User user) {
        when(workRuleResolverService.getClosingDay(eq(user))).thenReturn(1);
        when(workRuleResolverService.createRoundingSetting(eq(user))).thenReturn(new MinuteBasedRoundingSetting(15));
        when(workRuleResolverService.resolveWorkRule(eq(user), any())).thenReturn(WorkRuleResolverService.WorkRuleInfo.createDefault());
    }

    @Test
    void execute_正常ケース_位置情報から勤怠表が生成される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 1日の勤務: 9:00から1時間以内の間隔で記録される位置情報
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 1, 9, 0),   // 出勤
            LocalDateTime.of(2024, 1, 1, 9, 30),  // 30分後（同じグループ）
            LocalDateTime.of(2024, 1, 1, 10, 0)   // 1時間後（同じグループ）
        );

        // モックの設定
        when(workRuleResolverService.getClosingDay(eq(user))).thenReturn(1);
        when(workRuleResolverService.createRoundingSetting(eq(user))).thenReturn(new MinuteBasedRoundingSetting(15));
        when(workRuleResolverService.resolveWorkRule(eq(user), any())).thenReturn(WorkRuleResolverService.WorkRuleInfo.createDefault());
        
        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(yearMonth, response.yearMonth());
        assertEquals("testuser", response.owner().userId());
        assertEquals("NOT_SUBMITTED", response.status());
        
        // 1つのグループとして勤務データが生成されること
        assertEquals(1, response.workDays().size());
        
        // 勤務時間が正しく設定されること（9:00-10:00）
        var workDay = response.workDays().get(0);
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), workDay.startDateTime().value());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), workDay.endDateTime().value());

        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_正常ケース_1時間以内の位置情報がグルーピングされる() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 2つの勤務グループ: 午前（9:00-10:00）と午後（13:00-14:00）
        // 10:00と13:00の間が3時間空いているため別グループ
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 1, 9, 0),   // グループ1開始
            LocalDateTime.of(2024, 1, 1, 10, 0),  // グループ1終了
            LocalDateTime.of(2024, 1, 1, 13, 0),  // グループ2開始（3時間後）
            LocalDateTime.of(2024, 1, 1, 14, 0)   // グループ2終了
        );

        setupDefaultWorkRuleMocks(user);
        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        
        // 2つのグループに分かれるため、2つの勤務データが生成される
        assertEquals(2, response.workDays().size());
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_異常ケース_nullリクエストで例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> generateReportFromLocationUsecase.execute(null)
        );
        assertEquals("requestは必須です", exception.getMessage());
        
        verify(locationService, never()).getLocationRecordTimes(any(), any());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void execute_異常ケース_位置情報が存在しない場合() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(List.of()); // 空のリスト

        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.workDays().size()); // 勤務日なし
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void constructor_nullLocationService_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new GenerateReportFromLocationUsecase(null, reportRepository, workRuleResolverService)
        );
        assertEquals("locationServiceは必須です", exception.getMessage());
    }

    @Test
    void constructor_nullReportRepository_例外が発生する() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new GenerateReportFromLocationUsecase(locationService, null, workRuleResolverService)
        );
        assertEquals("reportRepositoryは必須です", exception.getMessage());
    }

    @Test
    void execute_休日勤務_休日フラグと休出時間が正しく設定される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 土曜日の勤務（2024/1/6は土曜日）- 1時間以内の間隔で記録
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 6, 9, 0),   // 土曜日の勤務開始
            LocalDateTime.of(2024, 1, 6, 9, 30),  // 30分後
            LocalDateTime.of(2024, 1, 6, 10, 0)   // 1時間後（合計1時間勤務）
        );

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.workDays().size());
        
        var workDay = response.workDays().get(0);
        assertTrue(workDay.isHoliday()); // 休日フラグが立っていること
        assertEquals(Duration.ofHours(1), workDay.holidayWorkHours()); // 1時間全てが休出時間
        assertEquals(Duration.ZERO, workDay.overtimeHours()); // 残業時間は0
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_平日定時内勤務_残業時間が0になる() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 平日の定時内勤務（2024/1/1は月曜日、1時間勤務）- 1時間以内の間隔で記録
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 1, 9, 0),   // 平日勤務開始
            LocalDateTime.of(2024, 1, 1, 9, 30),  // 30分後
            LocalDateTime.of(2024, 1, 1, 10, 0)   // 1時間後（合計1時間勤務）
        );

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.workDays().size());
        
        var workDay = response.workDays().get(0);
        assertFalse(workDay.isHoliday()); // 平日フラグ
        assertEquals(Duration.ZERO, workDay.overtimeHours()); // 1時間なので残業なし（7.5h未満）
        assertEquals(Duration.ZERO, workDay.holidayWorkHours()); // 休出時間は0
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_平日残業_残業時間が正しく計算される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 平日の残業（2024/1/1は月曜日、9時間勤務）- 9時から18時まで連続勤務
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 1, 9, 0),   // 平日勤務開始
            LocalDateTime.of(2024, 1, 1, 9, 30),  // 30分後
            LocalDateTime.of(2024, 1, 1, 10, 0),  // 1時間後
            LocalDateTime.of(2024, 1, 1, 10, 30), // 1.5時間後
            LocalDateTime.of(2024, 1, 1, 11, 0),  // 2時間後
            LocalDateTime.of(2024, 1, 1, 11, 30), // 2.5時間後
            LocalDateTime.of(2024, 1, 1, 12, 0),  // 3時間後
            LocalDateTime.of(2024, 1, 1, 12, 30), // 3.5時間後
            LocalDateTime.of(2024, 1, 1, 13, 0),  // 4時間後
            LocalDateTime.of(2024, 1, 1, 13, 30), // 4.5時間後
            LocalDateTime.of(2024, 1, 1, 14, 0),  // 5時間後
            LocalDateTime.of(2024, 1, 1, 14, 30), // 5.5時間後
            LocalDateTime.of(2024, 1, 1, 15, 0),  // 6時間後
            LocalDateTime.of(2024, 1, 1, 15, 30), // 6.5時間後
            LocalDateTime.of(2024, 1, 1, 16, 0),  // 7時間後
            LocalDateTime.of(2024, 1, 1, 16, 30), // 7.5時間後
            LocalDateTime.of(2024, 1, 1, 17, 0),  // 8時間後
            LocalDateTime.of(2024, 1, 1, 17, 30), // 8.5時間後
            LocalDateTime.of(2024, 1, 1, 18, 0)   // 9時間後（合計9時間勤務）
        );

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.workDays().size());
        
        var workDay = response.workDays().get(0);
        assertFalse(workDay.isHoliday()); // 平日フラグ
        assertEquals(Duration.ofMinutes(30), workDay.overtimeHours()); // 0.5時間の残業（9h - 1h休憩 - 7.5h標準 = 0.5h）
        assertEquals(Duration.ZERO, workDay.holidayWorkHours()); // 休出時間は0
        assertEquals(Duration.ofHours(9), workDay.workingHours()); // 総勤務時間9時間
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_単一位置情報_1つのグループとして処理される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 単一の位置情報（ループに入らないケース）
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 1, 9, 0)   // 1つの記録のみ
        );

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.workDays().size());
        
        var workDay = response.workDays().get(0);
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), workDay.startDateTime().value());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), workDay.endDateTime().value());
        assertEquals(Duration.ZERO, workDay.workingHours()); // 開始時刻と終了時刻が同じなので0時間
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_日曜日勤務_休日フラグが正しく設定される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 日曜日の勤務（2024/1/7は日曜日）
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 7, 9, 0),   // 日曜日の勤務開始
            LocalDateTime.of(2024, 1, 7, 10, 0)   // 日曜日の勤務終了
        );

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.workDays().size());
        
        var workDay = response.workDays().get(0);
        assertTrue(workDay.isHoliday()); // 日曜日なので休日フラグが立っていること
        assertEquals(Duration.ofHours(1), workDay.holidayWorkHours()); // 1時間全てが休出時間
        assertEquals(Duration.ZERO, workDay.overtimeHours()); // 残業時間は0
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_平日定時ちょうど_残業時間が0になる() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 平日の定時ちょうど（7.5時間）勤務 - 30分間隔で記録
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 1, 9, 0),   // 開始
            LocalDateTime.of(2024, 1, 1, 9, 30),  // 30分後
            LocalDateTime.of(2024, 1, 1, 10, 0),  // 1時間後
            LocalDateTime.of(2024, 1, 1, 10, 30), // 1.5時間後
            LocalDateTime.of(2024, 1, 1, 11, 0),  // 2時間後
            LocalDateTime.of(2024, 1, 1, 11, 30), // 2.5時間後
            LocalDateTime.of(2024, 1, 1, 12, 0),  // 3時間後
            LocalDateTime.of(2024, 1, 1, 12, 30), // 3.5時間後
            LocalDateTime.of(2024, 1, 1, 13, 0),  // 4時間後
            LocalDateTime.of(2024, 1, 1, 13, 30), // 4.5時間後
            LocalDateTime.of(2024, 1, 1, 14, 0),  // 5時間後
            LocalDateTime.of(2024, 1, 1, 14, 30), // 5.5時間後
            LocalDateTime.of(2024, 1, 1, 15, 0),  // 6時間後
            LocalDateTime.of(2024, 1, 1, 15, 30), // 6.5時間後
            LocalDateTime.of(2024, 1, 1, 16, 0),  // 7時間後
            LocalDateTime.of(2024, 1, 1, 16, 30)  // 7.5時間後（定時ちょうど）
        );

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.workDays().size());
        
        var workDay = response.workDays().get(0);
        assertFalse(workDay.isHoliday()); // 平日フラグ
        assertEquals(Duration.ZERO, workDay.overtimeHours()); // 定時ちょうどなので残業なし
        assertEquals(Duration.ZERO, workDay.holidayWorkHours()); // 休出時間は0
        assertEquals(Duration.ofMinutes(450), workDay.workingHours()); // 7.5時間 = 450分
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void execute_1時間より大きな間隔_複数グループに分離される() {
        // Arrange
        YearMonth yearMonth = YearMonth.of(2024, 1);
        UserDto userDto = new UserDto("testuser");
        User user = new User("testuser");
        GenerateReportFromLocationRequest request = new GenerateReportFromLocationRequest(yearMonth, userDto);

        // 1時間を超える間隔での位置情報（1時間1分の間隔）
        List<LocalDateTime> locationTimes = Arrays.asList(
            LocalDateTime.of(2024, 1, 1, 9, 0),   // グループ1
            LocalDateTime.of(2024, 1, 1, 10, 1),  // グループ2（1時間1分後なので別グループ）
            LocalDateTime.of(2024, 1, 1, 11, 0)   // グループ2
        );

        setupDefaultWorkRuleMocks(user);
        when(locationService.getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user)))
            .thenReturn(locationTimes);
        doNothing().when(reportRepository).save(any(Report.class));

        // Act
        ReportResponse response = generateReportFromLocationUsecase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.workDays().size()); // 2つのグループに分離される
        
        // グループ1: 9:00-9:00（単一記録）
        var workDay1 = response.workDays().get(0);
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), workDay1.startDateTime().value());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), workDay1.endDateTime().value());
        
        // グループ2: 10:01-11:00（15分切り上げ丸めにより10:15-11:00に調整される）
        var workDay2 = response.workDays().get(1);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 15), workDay2.startDateTime().value());
        assertEquals(LocalDateTime.of(2024, 1, 1, 11, 0), workDay2.endDateTime().value());
        
        verify(locationService, times(1)).getLocationRecordTimes(any(ReportPeriodCalculator.ReportPeriod.class), eq(user));
        verify(reportRepository, times(1)).save(any(Report.class));
    }
}