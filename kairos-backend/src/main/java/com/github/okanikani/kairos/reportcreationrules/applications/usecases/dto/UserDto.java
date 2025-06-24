package com.github.okanikani.kairos.reportcreationrules.applications.usecases.dto;

/**
 * ユーザーDTO（勤怠作成ルールドメイン用）
 * DDD原則により各ドメインコンテキスト毎に独自のDTOを定義
 */
public record UserDto(
        String userId
) {
}