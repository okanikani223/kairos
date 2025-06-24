package com.github.okanikani.kairos.reports.applications.usecases;

import com.github.okanikani.kairos.reports.applications.usecases.dto.GenerateReportFromLocationRequest;
import com.github.okanikani.kairos.reports.applications.usecases.dto.ReportResponse;
import com.github.okanikani.kairos.reports.applications.usecases.dto.DetailDto;
import com.github.okanikani.kairos.reports.applications.usecases.dto.WorkTimeDto;
import com.github.okanikani.kairos.reports.applications.usecases.mapper.ReportMapper;
import com.github.okanikani.kairos.reports.domains.models.constants.ReportStatus;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.Detail;
import com.github.okanikani.kairos.reports.domains.models.vos.Summary;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import com.github.okanikani.kairos.reports.domains.roundings.RoundingSetting;
import com.github.okanikani.kairos.reports.domains.service.LocationService;
import com.github.okanikani.kairos.reports.domains.service.ReportPeriodCalculator;
import com.github.okanikani.kairos.reports.domains.service.SummaryFactory;
import com.github.okanikani.kairos.reports.domains.service.WorkRuleResolverService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class GenerateReportFromLocationUsecase {
    
    private final LocationService locationService;
    private final ReportRepository reportRepository;
    private final WorkRuleResolverService workRuleResolverService;
    
    public GenerateReportFromLocationUsecase(
        LocationService locationService, 
        ReportRepository reportRepository,
        WorkRuleResolverService workRuleResolverService) {
        
        this.locationService = Objects.requireNonNull(locationService, "locationServiceは必須です");
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepositoryは必須です");
        this.workRuleResolverService = Objects.requireNonNull(workRuleResolverService, "workRuleResolverServiceは必須です");
    }
    
    public ReportResponse execute(GenerateReportFromLocationRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        User user = ReportMapper.toUser(request.user());
        
        // 勤怠計算開始日を取得
        int calculationStartDay = workRuleResolverService.getCalculationStartDay(user);
        
        // 実際の勤怠計算期間を算出
        ReportPeriodCalculator.ReportPeriod period = 
            ReportPeriodCalculator.calculatePeriod(request.yearMonth(), calculationStartDay);
        
        // 期間内の位置情報記録日時を取得
        List<LocalDateTime> locationTimes = locationService.getLocationRecordTimes(period, user);
        
        // 丸め設定を取得
        RoundingSetting roundingSetting = workRuleResolverService.createRoundingSetting(user);
        
        // 位置情報を1時間以内の間隔でグルーピングして勤務日詳細を生成
        List<DetailDto> workDays = groupLocationTimesAndCreateDetails(locationTimes, user, roundingSetting);
        
        // 勤怠表エンティティを作成
        List<Detail> details = workDays.stream()
            .map(ReportMapper::toDetail)
            .toList();
        
        // 勤務日詳細からサマリーを計算
        Summary summary = SummaryFactory.from(details);
        
        Report report = new Report(
            request.yearMonth(),
            user,
            ReportStatus.NOT_SUBMITTED,
            details,
            summary
        );
        
        // 保存
        reportRepository.save(report);
        
        // レスポンス作成
        return ReportMapper.toReportResponse(report);
    }
    
    /**
     * 位置情報記録日時を1時間以内の間隔でグルーピングし、勤務日詳細を作成する
     * @param locationTimes 位置情報記録日時のリスト（昇順）
     * @param user ユーザー
     * @param roundingSetting 丸め設定
     * @return 勤務日詳細のリスト
     */
    private List<DetailDto> groupLocationTimesAndCreateDetails(
        List<LocalDateTime> locationTimes, 
        User user, 
        RoundingSetting roundingSetting) {
        List<DetailDto> workDays = new ArrayList<>();
        
        if (locationTimes.isEmpty()) {
            return workDays;
        }
        
        List<LocalDateTime> currentGroup = new ArrayList<>();
        currentGroup.add(locationTimes.get(0));
        
        for (int i = 1; i < locationTimes.size(); i++) {
            LocalDateTime current = locationTimes.get(i);
            LocalDateTime previous = locationTimes.get(i - 1);
            
            // 前の記録時刻との間隔が1時間以内かチェック
            Duration gap = Duration.between(previous, current);
            if (gap.toMinutes() <= 60) {
                // 同じグループに追加
                currentGroup.add(current);
            } else {
                // 新しいグループ開始：現在のグループから勤務日詳細を作成
                DetailDto workDay = createDetailFromGroup(currentGroup, user, roundingSetting);
                workDays.add(workDay);
                
                // 新しいグループ開始
                currentGroup = new ArrayList<>();
                currentGroup.add(current);
            }
        }
        
        // 最後のグループを処理
        if (!currentGroup.isEmpty()) {
            DetailDto workDay = createDetailFromGroup(currentGroup, user, roundingSetting);
            workDays.add(workDay);
        }
        
        return workDays;
    }
    
    /**
     * 位置情報記録日時のグループから勤務日詳細を作成する
     * @param group 位置情報記録日時のグループ
     * @param user ユーザー
     * @param roundingSetting 丸め設定
     * @return 勤務日詳細
     */
    private DetailDto createDetailFromGroup(
        List<LocalDateTime> group, 
        User user, 
        RoundingSetting roundingSetting) {
        LocalDateTime rawStartTime = group.get(0);
        LocalDateTime rawEndTime = group.get(group.size() - 1);
        
        // WorkTimeファクトリメソッドで丸め処理適用
        com.github.okanikani.kairos.reports.domains.models.vos.WorkTime startWorkTime = 
            com.github.okanikani.kairos.reports.domains.models.vos.WorkTime.of(rawStartTime, roundingSetting);
        com.github.okanikani.kairos.reports.domains.models.vos.WorkTime endWorkTime = 
            com.github.okanikani.kairos.reports.domains.models.vos.WorkTime.of(rawEndTime, roundingSetting);
        
        LocalDateTime startTime = startWorkTime.value();
        LocalDateTime endTime = endWorkTime.value();
        
        // 勤務ルール取得
        WorkRuleResolverService.WorkRuleInfo workRule = 
            workRuleResolverService.resolveWorkRule(user, startTime.toLocalDate());
        
        // 休日判定・勤務時間計算
        boolean isHoliday = isHolidayDate(startTime);
        Duration totalWorkTime = Duration.between(startTime, endTime);
        var workTimeCalculation = calculateWorkTimeBreakdown(totalWorkTime, isHoliday, workRule);
        
        return new DetailDto(
            startTime.toLocalDate(),           // 勤務日付
            isHoliday,                        // 休日フラグ（土日判定）
            null,                             // 休暇区分なし
            new WorkTimeDto(startTime),       // 勤務開始日時
            new WorkTimeDto(endTime),         // 勤務終了日時
            totalWorkTime,                    // 就業時間
            workTimeCalculation.overtimeHours(),      // 残業時間
            workTimeCalculation.holidayWorkHours(),   // 休出時間
            ""                                // 特記事項なし
        );
    }
    
    /**
     * 日付が休日かどうかを判定する
     * @param dateTime 判定対象の日時
     * @return 休日の場合true（土曜日または日曜日）
     */
    private boolean isHolidayDate(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * 勤務時間の内訳計算結果
     */
    private record WorkTimeCalculation(
        Duration overtimeHours,
        Duration holidayWorkHours
    ) {}
    
    /**
     * 勤務時間を残業時間と休出時間に分類して計算する
     * @param totalWorkTime 総勤務時間
     * @param isHoliday 休日フラグ
     * @param workRule 勤務ルール情報
     * @return 勤務時間の内訳
     */
    private WorkTimeCalculation calculateWorkTimeBreakdown(
        Duration totalWorkTime, 
        boolean isHoliday, 
        WorkRuleResolverService.WorkRuleInfo workRule) {
        
        // ルールが無効な場合はシステムデフォルトを使用
        WorkRuleResolverService.WorkRuleInfo effectiveRule = workRule.isValid() ? 
            workRule : WorkRuleResolverService.WorkRuleInfo.createDefault();
        
        // 休憩時間を除いた実労働時間を計算
        Duration effectiveWorkTime = totalWorkTime.minus(effectiveRule.breakTime());
        
        if (isHoliday) {
            // 休日勤務: 実労働時間を休出時間として扱う
            return new WorkTimeCalculation(Duration.ZERO, effectiveWorkTime);
        } else {
            // 平日勤務: 標準勤務時間を超えた分を残業時間として扱う
            Duration overtimeHours = effectiveWorkTime.compareTo(effectiveRule.standardWorkTime()) > 0 
                ? effectiveWorkTime.minus(effectiveRule.standardWorkTime()) 
                : Duration.ZERO;
            return new WorkTimeCalculation(overtimeHours, Duration.ZERO);
        }
    }
}