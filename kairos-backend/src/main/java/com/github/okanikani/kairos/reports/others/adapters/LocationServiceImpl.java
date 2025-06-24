package com.github.okanikani.kairos.reports.others.adapters;

import com.github.okanikani.kairos.reports.domains.service.LocationService;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 位置情報サービス実装クラス
 * 勤怠表ドメインから位置情報ドメインへのアダプター
 * ※これは開発・テスト用の一時的な実装です。
 * TODO: 実際の位置情報ドメインとの連携実装
 */
@Service
public class LocationServiceImpl implements LocationService {
    
    @Override
    public List<LocalDateTime> getLocationRecordTimes(YearMonth yearMonth, User user) {
        // TODO: 実際の位置情報ドメインから記録日時を取得する実装
        // 開発・テスト用の空実装
        return List.of();
    }
}