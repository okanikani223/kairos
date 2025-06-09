# API設計統一ガイドライン

## 1. 概要

本ドキュメントは、kairosプロジェクトにおけるAPI設計の統一基準を定義し、機能間の一貫性を確保するためのガイドラインです。

### 1.1. 目的
- APIエンドポイントの命名規則統一
- HTTPステータスコードの使用基準統一
- レスポンス形式の構造統一
- エラーハンドリングの標準化

### 1.2. 対象範囲
- REST API設計全般
- HTTPメソッド・ステータスコード
- リクエスト・レスポンス形式
- エラーレスポンス形式

## 2. APIエンドポイント設計規則

### 2.1. 基本パターン

#### 2.1.1. RESTful API標準パターン
```
GET    /api/v1/{resource}                    # リソース一覧取得
GET    /api/v1/{resource}/{id}               # リソース詳細取得
POST   /api/v1/{resource}                    # リソース新規作成
PUT    /api/v1/{resource}/{id}               # リソース完全更新
PATCH  /api/v1/{resource}/{id}               # リソース部分更新
DELETE /api/v1/{resource}/{id}               # リソース削除
```

#### 2.1.2. ネストしたリソースパターン
```
GET    /api/v1/{parent}/{parent-id}/{child}           # 子リソース一覧取得
GET    /api/v1/{parent}/{parent-id}/{child}/{child-id} # 子リソース詳細取得
POST   /api/v1/{parent}/{parent-id}/{child}           # 子リソース新規作成
PUT    /api/v1/{parent}/{parent-id}/{child}/{child-id} # 子リソース完全更新
PATCH  /api/v1/{parent}/{parent-id}/{child}/{child-id} # 子リソース部分更新
DELETE /api/v1/{parent}/{parent-id}/{child}/{child-id} # 子リソース削除
```

### 2.2. 命名規則

#### 2.2.1. リソース名
- **複数形を使用**: `attendances`、`companies`、`locations`
- **ケバブケース**: 単語区切りは半角ハイフン（-）を使用
- **階層は最大3レベル**: `/api/v1/attendances/{id}/details/{detail-id}`

#### 2.2.2. パラメータ名
- **スネークケース**: `user_id`、`start_date`、`end_date`
- **意味が明確**: 略語を避け、完全な単語を使用
- **統一性**: 同じ概念には同じ名前を使用

#### 2.2.3. 特殊エンドポイント
業務固有の操作が必要な場合のみ、動詞を含むエンドポイントを許可：
```
PATCH /api/v1/attendances/{id}/disable    # 無効化
POST  /api/v1/attendances/{id}/regenerate # 再生成
GET   /api/v1/attendances/search          # 高度な検索
```

### 2.3. エンドポイント統一基準

#### 2.3.1. 勤怠情報関連エンドポイント（統一後）
```
# 基本CRUD
GET    /api/v1/attendances                     # 一覧取得
GET    /api/v1/attendances/{attendance-id}     # 詳細取得
POST   /api/v1/attendances                     # 新規登録
PUT    /api/v1/attendances/{attendance-id}     # 完全更新
PATCH  /api/v1/attendances/{attendance-id}     # 部分更新
DELETE /api/v1/attendances/{attendance-id}     # 削除

# 子リソース（詳細情報）
GET    /api/v1/attendances/{attendance-id}/details
POST   /api/v1/attendances/{attendance-id}/details
PATCH  /api/v1/attendances/{attendance-id}/details/{detail-id}
DELETE /api/v1/attendances/{attendance-id}/details/{detail-id}

# 特殊操作
PATCH  /api/v1/attendances/{attendance-id}/disable    # 論理削除
POST   /api/v1/attendances/{attendance-id}/regenerate # 自動再生成
```

#### 2.3.2. 勤怠情報提出先関連エンドポイント
```
GET    /api/v1/companies                 # 一覧取得
GET    /api/v1/companies/{company-id}    # 詳細取得
POST   /api/v1/companies                 # 新規登録
PUT    /api/v1/companies/{company-id}    # 完全更新
DELETE /api/v1/companies/{company-id}    # 削除
```

#### 2.3.3. 位置情報関連エンドポイント
```
POST   /api/v1/locations                 # 位置情報登録
GET    /api/v1/locations                 # 位置履歴取得
DELETE /api/v1/locations/{location-id}   # 位置情報削除
```

## 3. HTTPステータスコード統一基準

### 3.1. 成功レスポンス

| ステータスコード | 用途 | 使用場面 |
|---------------|------|---------|
| 200 OK | 取得・更新成功 | GET、PUT、PATCH、DELETE |
| 201 Created | 作成成功 | POST |
| 204 No Content | 成功（レスポンスボディなし） | DELETE（物理削除） |

### 3.2. クライアントエラー

| ステータスコード | 用途 | 使用場面 | 必須フィールド |
|---------------|------|---------|--------------|
| 400 Bad Request | リクエスト形式エラー | パラメータ不正、JSON形式エラー | code, message, details |
| 401 Unauthorized | 認証エラー | JWT無効、認証失敗 | code, message |
| 403 Forbidden | 認可エラー | アクセス権限不足 | code, message |
| 404 Not Found | リソース未存在 | 指定IDのデータなし | code, message |
| 409 Conflict | 競合エラー | 重複データ、楽観的ロック | code, message, details |
| 422 Unprocessable Entity | ビジネスロジックエラー | 業務ルール違反 | code, message, details |

### 3.3. サーバーエラー

| ステータスコード | 用途 | 使用場面 |
|---------------|------|---------|
| 500 Internal Server Error | サーバー内部エラー | システム例外、DB接続エラー |
| 503 Service Unavailable | サービス利用不可 | メンテナンス、過負荷 |

### 3.4. エラーシナリオ別ステータスコード

#### 3.4.1. データ取得時
- **データが存在しない**: 404 Not Found
- **権限がない**: 403 Forbidden
- **パラメータ不正**: 400 Bad Request

#### 3.4.2. データ作成時
- **作成成功**: 201 Created
- **重複データ**: 409 Conflict
- **バリデーションエラー**: 400 Bad Request
- **ビジネスルール違反**: 422 Unprocessable Entity

#### 3.4.3. データ更新時
- **更新成功**: 200 OK
- **データが存在しない**: 404 Not Found
- **楽観的ロックエラー**: 409 Conflict
- **バリデーションエラー**: 400 Bad Request

#### 3.4.4. データ削除時
- **削除成功（レスポンスあり）**: 200 OK
- **削除成功（レスポンスなし）**: 204 No Content
- **データが存在しない**: 404 Not Found
- **関連データ存在**: 409 Conflict

## 4. レスポンス形式統一基準

### 4.1. 共通レスポンス構造

#### 4.1.1. 成功レスポンス基本形式
```json
{
  "success": true,
  "data": {
    // 実際のデータ
  },
  "meta": {
    // メタデータ（ページネーション等）
  }
}
```

#### 4.1.2. エラーレスポンス基本形式
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "エラーメッセージ",
    "details": [
      // エラー詳細（配列形式で統一）
    ]
  }
}
```

### 4.2. データ包含パターン

#### 4.2.1. 単一リソース
```json
{
  "success": true,
  "data": {
    "attendance": {
      "id": "att_001",
      "userId": "usr_001",
      "timestamp": "2025-06-09T09:00:00Z",
      // その他のフィールド
    }
  }
}
```

#### 4.2.2. 複数リソース（一覧）
```json
{
  "success": true,
  "data": {
    "attendances": [
      {
        "id": "att_001",
        // リソース情報
      },
      {
        "id": "att_002",
        // リソース情報
      }
    ]
  },
  "meta": {
    "total": 150,
    "page": 1,
    "limit": 20,
    "hasNext": true
  }
}
```

#### 4.2.3. ページネーション
```json
{
  "success": true,
  "data": {
    "attendances": []
  },
  "meta": {
    "pagination": {
      "total": 150,
      "page": 1,
      "limit": 20,
      "totalPages": 8,
      "hasNext": true,
      "hasPrev": false
    }
  }
}
```

### 4.3. リソース別レスポンス形式

#### 4.3.1. 勤怠情報
```json
// 単一取得・作成・更新
{
  "success": true,
  "data": {
    "attendance": {
      "id": "att_001",
      "userId": "usr_001",
      "timestamp": "2025-06-09T09:00:00Z",
      "attendanceType": "attendance",
      "location": {
        "latitude": 35.6762,
        "longitude": 139.6503
      },
      "workLocation": {
        "id": "wl_001",
        "name": "本社オフィス"
      },
      "isGenerated": true,
      "createdAt": "2025-06-09T09:00:00Z",
      "updatedAt": "2025-06-09T09:00:00Z"
    }
  }
}

// 一覧取得
{
  "success": true,
  "data": {
    "attendances": []
  },
  "meta": {
    "total": 150,
    "page": 1,
    "limit": 20
  }
}
```

#### 4.3.2. 勤怠情報提出先
```json
// 単一取得・作成・更新
{
  "success": true,
  "data": {
    "company": {
      "id": "cmp_001",
      "name": "株式会社サンプル",
      "address": "東京都渋谷区...",
      "contactInfo": {
        "email": "contact@example.com",
        "phone": "03-1234-5678"
      },
      "createdAt": "2025-06-09T09:00:00Z",
      "updatedAt": "2025-06-09T09:00:00Z"
    }
  }
}

// 一覧取得
{
  "success": true,
  "data": {
    "companies": []
  },
  "meta": {
    "total": 10,
    "page": 1,
    "limit": 20
  }
}
```

#### 4.3.3. 位置情報
```json
// 登録
{
  "success": true,
  "data": {
    "location": {
      "id": "loc_001",
      "userId": "usr_001",
      "latitude": 35.6762,
      "longitude": 139.6503,
      "accuracy": 10.5,
      "timestamp": "2025-06-09T09:00:00Z"
    }
  }
}
```

## 5. エラーレスポンス統一基準

### 5.1. エラーコード体系

#### 5.1.1. エラーコード命名規則
```
{CATEGORY}_{SPECIFIC_ERROR}

例:
VALIDATION_ERROR      # バリデーションエラー
AUTHENTICATION_ERROR  # 認証エラー
AUTHORIZATION_ERROR   # 認可エラー
RESOURCE_NOT_FOUND    # リソース未存在
CONFLICT_ERROR        # 競合エラー
BUSINESS_RULE_ERROR   # ビジネスルール違反
```

#### 5.1.2. 共通エラーコード
| エラーコード | HTTPステータス | 説明 |
|------------|--------------|------|
| VALIDATION_ERROR | 400 | リクエストパラメータのバリデーションエラー |
| AUTHENTICATION_ERROR | 401 | 認証エラー（JWT無効等） |
| AUTHORIZATION_ERROR | 403 | 認可エラー（権限不足） |
| RESOURCE_NOT_FOUND | 404 | 指定されたリソースが存在しない |
| CONFLICT_ERROR | 409 | データ競合エラー（重複、楽観的ロック） |
| BUSINESS_RULE_ERROR | 422 | ビジネスルール違反 |
| INTERNAL_SERVER_ERROR | 500 | サーバー内部エラー |

### 5.2. エラーレスポンス形式

#### 5.2.1. バリデーションエラー（400）
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "リクエストパラメータに不正な値が含まれています",
    "details": [
      {
        "field": "latitude",
        "message": "緯度は-90から90の範囲で入力してください",
        "value": 95.0
      },
      {
        "field": "timestamp",
        "message": "日時は有効な形式で入力してください",
        "value": "invalid-date"
      }
    ]
  }
}
```

#### 5.2.2. 認証エラー（401）
```json
{
  "success": false,
  "error": {
    "code": "AUTHENTICATION_ERROR",
    "message": "認証が必要です。有効なJWTトークンを提供してください"
  }
}
```

#### 5.2.3. 権限エラー（403）
```json
{
  "success": false,
  "error": {
    "code": "AUTHORIZATION_ERROR",
    "message": "このリソースにアクセスする権限がありません"
  }
}
```

#### 5.2.4. リソース未存在エラー（404）
```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "指定された勤怠情報が見つかりません",
    "details": [
      {
        "field": "attendanceId",
        "message": "ID 'att_999' に対応する勤怠情報は存在しません",
        "value": "att_999"
      }
    ]
  }
}
```

#### 5.2.5. 競合エラー（409）
```json
{
  "success": false,
  "error": {
    "code": "CONFLICT_ERROR",
    "message": "データが他のユーザーによって更新されています",
    "details": [
      {
        "field": "version",
        "message": "楽観的ロックエラー。最新データを取得して再度実行してください",
        "expected": 5,
        "actual": 7
      }
    ]
  }
}
```

#### 5.2.6. ビジネスルールエラー（422）
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_RULE_ERROR",
    "message": "ビジネスルールに違反しています",
    "details": [
      {
        "field": "attendanceType",
        "message": "既に出勤記録が存在するため、重複して出勤登録はできません",
        "value": "attendance"
      }
    ]
  }
}
```

### 5.3. detailsフィールド統一仕様

#### 5.3.1. 基本構造
```json
"details": [
  {
    "field": "フィールド名",           // エラー対象フィールド
    "message": "具体的なエラーメッセージ", // 詳細な説明
    "value": "実際の値",              // エラーとなった値（任意）
    "expected": "期待値",             // 期待されていた値（任意）
    "actual": "実際の値"              // 実際の値（競合エラー等で使用）
  }
]
```

#### 5.3.2. 使用ケース別

**単一フィールドエラー**:
```json
"details": [
  {
    "field": "email",
    "message": "有効なメールアドレス形式で入力してください",
    "value": "invalid-email"
  }
]
```

**複数フィールドエラー**:
```json
"details": [
  {
    "field": "latitude",
    "message": "緯度は必須項目です"
  },
  {
    "field": "longitude", 
    "message": "経度は必須項目です"
  }
]
```

**関連性エラー**:
```json
"details": [
  {
    "field": "endTime",
    "message": "終了時刻は開始時刻より後の時刻を指定してください",
    "value": "08:00",
    "expected": "> 09:00"
  }
]
```

## 6. クエリパラメータ統一基準

### 6.1. 共通パラメータ

#### 6.1.1. ページネーション
| パラメータ | 型 | デフォルト値 | 説明 |
|-----------|---|------------|------|
| page | integer | 1 | ページ番号（1から開始） |
| limit | integer | 20 | 1ページあたりのアイテム数 |
| offset | integer | 0 | オフセット（pageとどちらか一方） |

#### 6.1.2. ソート
| パラメータ | 型 | デフォルト値 | 説明 |
|-----------|---|------------|------|
| sort | string | - | ソートフィールド（例: "createdAt"） |
| order | string | "asc" | ソート順序（"asc" または "desc"） |

#### 6.1.3. フィルタリング
| パラメータ | 型 | デフォルト値 | 説明 |
|-----------|---|------------|------|
| start_date | string | - | 開始日（ISO 8601形式） |
| end_date | string | - | 終了日（ISO 8601形式） |
| status | string | - | ステータスフィルタ |

### 6.2. デフォルト値記載形式

#### 6.2.1. APIドキュメント記載例
```markdown
| パラメータ | 型 | 必須 | デフォルト値 | 説明 |
|-----------|---|------|------------|------|
| page | integer | ❌ | 1 | ページ番号 |
| limit | integer | ❌ | 20 | 1ページあたりのアイテム数（最大100） |
| sort | string | ❌ | "createdAt" | ソートフィールド |
| order | string | ❌ | "desc" | ソート順序 |
```

#### 6.2.2. 実装レベルでのデフォルト値処理
```typescript
interface QueryParams {
  page?: number;
  limit?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

function applyDefaults(params: QueryParams): Required<QueryParams> {
  return {
    page: params.page ?? 1,
    limit: Math.min(params.limit ?? 20, 100),
    sort: params.sort ?? 'createdAt',
    order: params.order ?? 'desc'
  };
}
```

## 7. 実装ガイドライン

### 7.1. レスポンス生成ヘルパー

#### 7.1.1. 成功レスポンス
```typescript
function successResponse<T>(data: T, meta?: any) {
  return {
    success: true,
    data,
    ...(meta && { meta })
  };
}

// 使用例
return successResponse({ 
  attendance: attendanceData 
});

return successResponse(
  { attendances: attendanceList },
  { total: 150, page: 1, limit: 20 }
);
```

#### 7.1.2. エラーレスポンス
```typescript
function errorResponse(
  code: string, 
  message: string, 
  details?: ErrorDetail[]
) {
  return {
    success: false,
    error: {
      code,
      message,
      ...(details && { details })
    }
  };
}

// 使用例
return errorResponse(
  'VALIDATION_ERROR',
  'リクエストパラメータに不正な値が含まれています',
  [
    {
      field: 'latitude',
      message: '緯度は-90から90の範囲で入力してください',
      value: invalidLatitude
    }
  ]
);
```

### 7.2. バリデーション統一

#### 7.2.1. 共通バリデーションルール
```typescript
const ValidationRules = {
  latitude: {
    min: -90,
    max: 90,
    required: true,
    message: '緯度は-90から90の範囲で入力してください'
  },
  longitude: {
    min: -180,
    max: 180,
    required: true,
    message: '経度は-180から180の範囲で入力してください'
  },
  timestamp: {
    format: 'ISO8601',
    required: true,
    message: '日時は有効なISO8601形式で入力してください'
  }
};
```

### 7.3. HTTPステータスコード使用指針

#### 7.3.1. コントローラー層での実装例
```typescript
class AttendanceController {
  async create(req: Request, res: Response) {
    try {
      const result = await this.attendanceService.create(req.body);
      return res.status(201).json(successResponse({ attendance: result }));
    } catch (error) {
      if (error instanceof ValidationError) {
        return res.status(400).json(errorResponse(
          'VALIDATION_ERROR',
          error.message,
          error.details
        ));
      }
      if (error instanceof DuplicateError) {
        return res.status(409).json(errorResponse(
          'CONFLICT_ERROR',
          error.message
        ));
      }
      return res.status(500).json(errorResponse(
        'INTERNAL_SERVER_ERROR',
        'サーバー内部エラーが発生しました'
      ));
    }
  }
}
```

## 8. 移行計画

### 8.1. 段階的移行

#### 8.1.1. フェーズ1: 新規API
- 新規作成するAPIはこのガイドラインに準拠
- レビュープロセスでガイドライン遵守を確認

#### 8.1.2. フェーズ2: 既存API更新
- 破壊的変更のないもの（エラーレスポンス、メタデータ等）から順次更新
- API仕様書の更新

#### 8.1.3. フェーズ3: 破壊的変更
- レスポンス構造の大幅変更
- クライアント側の対応も含めた計画的な移行

### 8.2. 互換性管理

#### 8.2.1. バージョニング
- メジャーな変更時はAPIバージョンアップ（v1 → v2）
- 既存バージョンは一定期間サポート継続

#### 8.2.2. 移行期間中の考慮事項
- 新旧両方の形式をサポートする期間を設定
- クライアント側での対応猶予期間を確保

## 9. 適用チェックリスト

### 9.1. API設計チェックリスト

#### 9.1.1. エンドポイント設計
- [ ] RESTful原則に準拠している
- [ ] リソース名が複数形で統一されている
- [ ] 命名規則（ケバブケース）に準拠している
- [ ] パス階層が3レベル以下である

#### 9.1.2. HTTPステータスコード
- [ ] 適切なステータスコードが使用されている
- [ ] 統一基準に従ったエラーコードが設定されている
- [ ] エラーシナリオ別の対応が実装されている

#### 9.1.3. レスポンス形式
- [ ] 成功レスポンスに`success: true`が含まれている
- [ ] データが`data`オブジェクト内に格納されている
- [ ] エラーレスポンスの`details`が配列形式である
- [ ] メタデータが適切に設定されている

#### 9.1.4. エラーハンドリング
- [ ] エラーコードが統一命名規則に準拠している
- [ ] エラーメッセージがユーザーフレンドリーである
- [ ] 詳細情報が`details`配列に含まれている

### 9.2. ドキュメント更新チェックリスト

#### 9.2.1. API仕様書
- [ ] エンドポイント一覧が更新されている
- [ ] HTTPステータスコード表が統一されている
- [ ] レスポンス例が新形式で記載されている
- [ ] エラーレスポンス例が統一されている

#### 9.2.2. 実装ガイド
- [ ] 実装例が最新ガイドラインに準拠している
- [ ] バリデーションルールが明確である
- [ ] エラーハンドリングパターンが統一されている

## 10. 変更履歴

| 日付 | 変更者 | 変更内容 |
|------|--------|----------|
| 2025/06/09 | カーン | 初版作成 |
