package com.github.okanikani.kairos.reports.others.repositories;

import com.github.okanikani.kairos.commons.exceptions.ResourceNotFoundException;
import com.github.okanikani.kairos.reports.domains.models.entities.Report;
import com.github.okanikani.kairos.reports.domains.models.repositories.ReportRepository;
import com.github.okanikani.kairos.reports.domains.models.vos.User;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * インメモリ勤怠表リポジトリ実装
 * 
 * ※これは開発・テスト用の一時的な実装です。
 * 本番環境ではデータベースを使用した実装に置き換える必要があります。
 * TODO: PostgreSQL等を使用した永続化実装への置き換え
 */
@Repository
public class InMemoryReportRepository implements ReportRepository {
    
    private final Map<String, Report> storage = new HashMap<>();
    
    @Override
    public void save(Report report) {
        Objects.requireNonNull(report, "reportは必須です");
        String key = generateKey(report.yearMonth(), report.owner());
        storage.put(key, report);
    }
    
    @Override
    public Report find(YearMonth yearMonth, User user) {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(user, "userは必須です");
        String key = generateKey(yearMonth, user);
        return storage.get(key);
    }
    
    @Override
    public List<Report> findAll() {
        return storage.values().stream().toList();
    }
    
    @Override
    public void update(Report report) {
        Objects.requireNonNull(report, "reportは必須です");
        String key = generateKey(report.yearMonth(), report.owner());
        if (!storage.containsKey(key)) {
            throw new ResourceNotFoundException("更新対象の勤怠表が存在しません: " + key);
        }
        storage.put(key, report);
    }
    
    @Override
    public void delete(YearMonth yearMonth, User user) {
        Objects.requireNonNull(yearMonth, "yearMonthは必須です");
        Objects.requireNonNull(user, "userは必須です");
        String key = generateKey(yearMonth, user);
        storage.remove(key);
    }
    
    private String generateKey(YearMonth yearMonth, User user) {
        return yearMonth.toString() + ":" + user.userId();
    }
}