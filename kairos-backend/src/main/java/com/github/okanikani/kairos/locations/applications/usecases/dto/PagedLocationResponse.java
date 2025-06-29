package com.github.okanikani.kairos.locations.applications.usecases.dto;

import java.util.List;

/**
 * ページネーション対応位置情報レスポンス
 * 
 * @param content 位置情報リスト
 * @param page 現在のページ番号（0から開始）
 * @param size 1ページあたりの件数
 * @param totalElements 全件数
 * @param totalPages 全ページ数
 * @param first 最初のページかどうか
 * @param last 最後のページかどうか
 * @param hasNext 次のページが存在するかどうか
 * @param hasPrevious 前のページが存在するかどうか
 */
public record PagedLocationResponse(
    List<LocationResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious
) {
    /**
     * Spring Data JPA の Page オブジェクトから PagedLocationResponse を作成するファクトリメソッド
     */
    public static PagedLocationResponse from(org.springframework.data.domain.Page<LocationResponse> page) {
        return new PagedLocationResponse(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}