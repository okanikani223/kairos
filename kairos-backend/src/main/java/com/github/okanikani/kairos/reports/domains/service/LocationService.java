package com.github.okanikani.kairos.reports.domains.service;

import com.github.okanikani.kairos.reports.domains.models.vos.User;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 位置情報を取得するドメインサービス
 * 勤怠表ドメインから位置情報ドメインへのアクセスを抽象化
 */
public interface LocationService {
    
    /**
     * 指定年月とユーザーの位置情報の記録日時を取得する
     * @param yearMonth 対象年月
     * @param user ユーザー
     * @return 記録日時のリスト（昇順）
     */
    List<LocalDateTime> getLocationRecordTimes(YearMonth yearMonth, User user);
}