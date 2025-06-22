package com.github.okanikani223.kairos.reports.domains.models.repositories;

import com.github.okanikani223.kairos.reports.domains.models.entities.Report;
import com.github.okanikani223.kairos.reports.domains.models.vos.User;

import java.time.YearMonth;
import java.util.List;

/**
 * 勤怠表のCRUDを担当するインターフェース
 */
public interface ReportRepository {
    void save(Report report);

    Report find(YearMonth yearMonth, User user);

    List<Report> findAll();

    void update(Report report);

    void delete(YearMonth yearMonth, User user);
}
