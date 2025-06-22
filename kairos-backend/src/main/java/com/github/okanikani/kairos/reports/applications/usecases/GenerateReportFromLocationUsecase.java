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
import com.github.okanikani.kairos.reports.domains.service.LocationService;
import com.github.okanikani.kairos.reports.domains.service.SummaryFactory;
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
    
    public GenerateReportFromLocationUsecase(LocationService locationService, ReportRepository reportRepository) {
        this.locationService = Objects.requireNonNull(locationService, "locationServiceは必須です");
        this.reportRepository = Objects.requireNonNull(reportRepository, "reportRepositoryは必須です");
    }
    
    public ReportResponse execute(GenerateReportFromLocationRequest request) {
        Objects.requireNonNull(request, "requestは必須です");
        
        User user = ReportMapper.toUser(request.user());
        
        // 位置情報の記録日時を取得
        List<LocalDateTime> locationTimes = locationService.getLocationRecordTimes(request.yearMonth(), user);
        
        // 位置情報を1時間以内の間隔でグルーピングして勤務日詳細を生成
        List<DetailDto> workDays = groupLocationTimesAndCreateDetails(locationTimes);
        
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
     * @return 勤務日詳細のリスト
     */
    private List<DetailDto> groupLocationTimesAndCreateDetails(List<LocalDateTime> locationTimes) {
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
                DetailDto workDay = createDetailFromGroup(currentGroup);
                workDays.add(workDay);
                
                // 新しいグループ開始
                currentGroup = new ArrayList<>();
                currentGroup.add(current);
            }
        }
        
        // 最後のグループを処理
        if (!currentGroup.isEmpty()) {
            DetailDto workDay = createDetailFromGroup(currentGroup);
            workDays.add(workDay);
        }
        
        return workDays;
    }
    
    /**
     * 位置情報記録日時のグループから勤務日詳細を作成する
     * @param group 位置情報記録日時のグループ
     * @return 勤務日詳細
     */
    private DetailDto createDetailFromGroup(List<LocalDateTime> group) {
        LocalDateTime startTime = group.get(0);
        LocalDateTime endTime = group.get(group.size() - 1);
        
        boolean isHoliday = isHolidayDate(startTime);
        Duration totalWorkTime = Duration.between(startTime, endTime);
        
        // 勤務時間の種別計算
        var workTimeCalculation = calculateWorkTimeBreakdown(totalWorkTime, isHoliday);
        
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
     * @return 勤務時間の内訳
     */
    private WorkTimeCalculation calculateWorkTimeBreakdown(Duration totalWorkTime, boolean isHoliday) {
        Duration standardWorkTime = Duration.ofMinutes(450); // 定時7.5時間
        
        if (isHoliday) {
            // 休日勤務: 全時間を休出時間として扱う
            return new WorkTimeCalculation(Duration.ZERO, totalWorkTime);
        } else {
            // 平日勤務: 定時を超えた分を残業時間として扱う
            Duration overtimeHours = totalWorkTime.compareTo(standardWorkTime) > 0 
                ? totalWorkTime.minus(standardWorkTime) 
                : Duration.ZERO;
            return new WorkTimeCalculation(overtimeHours, Duration.ZERO);
        }
    }
}