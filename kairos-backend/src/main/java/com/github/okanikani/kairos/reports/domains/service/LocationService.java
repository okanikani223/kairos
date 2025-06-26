package com.github.okanikani.kairos.reports.domains.service;

import com.github.okanikani.kairos.commons.service.LocationFilteringService.WorkplaceLocation;
import com.github.okanikani.kairos.reports.domains.models.vos.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 位置情報を取得するドメインサービス
 * 勤怠表ドメインから位置情報ドメインへのアクセスを抽象化
 */
public interface LocationService {
    
    /**
     * 期間を指定して位置情報記録日時を取得
     * @param period 勤怠計算期間
     * @param user ユーザー
     * @return 位置情報記録日時リスト（昇順）
     */
    List<LocalDateTime> getLocationRecordTimes(ReportPeriodCalculator.ReportPeriod period, User user);
    
    /**
     * 期間と作業場所を指定して、作業場所近辺の位置情報記録日時を取得
     * 
     * 作業場所からの距離に基づいて位置情報をフィルタリングし、
     * 勤務時間として妥当な位置情報の記録日時のみを返却する
     * 
     * @param period 勤怠計算期間
     * @param user ユーザー
     * @param workplace 作業場所の位置情報
     * @return フィルタリング後の位置情報記録日時リスト（昇順）
     */
    List<LocalDateTime> getLocationRecordTimesNearWorkplace(
        ReportPeriodCalculator.ReportPeriod period, 
        User user, 
        WorkplaceLocation workplace
    );
}