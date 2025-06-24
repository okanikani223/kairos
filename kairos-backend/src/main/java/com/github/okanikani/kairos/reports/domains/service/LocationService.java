package com.github.okanikani.kairos.reports.domains.service;

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
}