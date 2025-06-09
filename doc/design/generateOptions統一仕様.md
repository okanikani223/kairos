# generateOptions統一仕様

## 1. 概要
generateOptionsは、勤怠情報の自動生成処理における各種オプションの設定を司るパラメータです。
この仕様書では、勤怠情報登録機能および勤怠情報更新機能で共通して使用されるgenerateOptionsの統一的な定義と動作仕様を定めます。

## 2. generateOptionsオブジェクト仕様

### 2.1 パラメータ定義

| フィールド | 型 | 説明 | デフォルト値 | 必須 |
|---------|------|------|------------|-----|
| useLocationData | Boolean | 位置情報を勤務判定に使用するかどうか | true | いいえ |
| fallbackToRegulation | Boolean | 位置情報がない場合に規定時間を使用するかどうか | true | いいえ |
| autoLeaveDetection | Boolean | 自動的な休暇判定を行うかどうか | true | いいえ |

### 2.2 省略時の動作
- generateOptionsが省略（未指定またはnull）された場合、システムは以下のデフォルト値を適用します：
  ```json
  {
    "useLocationData": true,
    "fallbackToRegulation": true,
    "autoLeaveDetection": true
  }
  ```
- これにより、位置情報を最大限活用する標準的な自動生成処理が実行されます。

### 2.3 バリデーション仕様
- 各フィールドの型チェック（Boolean以外はエラー）
- 未知のフィールドは無視される
- オブジェクト以外の値（文字列、数値、配列等）が指定された場合はエラー

## 3. オプション別動作仕様

### 3.1 useLocationDataオプション

#### 3.1.1 true（位置情報使用）
- 指定期間の位置情報データを取得し、勤務地近辺の判定を行う
- 勤務開始・終了時刻を位置情報から自動判定
- 位置情報データが不足している場合の動作はfallbackToRegulationオプションに依存

#### 3.1.2 false（位置情報不使用）
- 位置情報データの取得・処理をスキップ
- 勤務判定の動作はfallbackToRegulationオプションに依存
  - `fallbackToRegulation: true`：規定勤怠時間を使用
  - `fallbackToRegulation: false`：欠勤として処理

### 3.2 fallbackToRegulationオプション

#### 3.2.1 true（規定時間フォールバック有効）
- 位置情報が不足またはuseLocationDataがfalseの場合、UserAttendanceSettingから規定時間を取得
- 規定勤怠開始時刻・終了時刻・休憩時間を使用して勤怠詳細を生成
- 休日の場合は休日フラグを設定し、勤務時間は0とする

#### 3.2.2 false（規定時間フォールバック無効）
- 位置情報が不足またはuseLocationDataがfalseの場合、欠勤として処理
- 勤務時間、残業時間をすべて0に設定
- 休日の場合は通常通り休日として処理

### 3.3 autoLeaveDetectionオプション

#### 3.3.1 true（自動休暇判定有効）
- 勤務時間情報から午前・午後有給の自動判定を実行
- **参照先**: [勤務判定アルゴリズム統一標準.md - 3.3 休暇自動判定アルゴリズム](./勤務判定アルゴリズム統一標準.md#33-休暇自動判定アルゴリズム)
- 判定結果をleaveフィールドに設定（"午前有給"、"午後有給"、""等）

#### 3.3.2 false（自動休暇判定無効）
- 休暇判定処理をスキップ
- leaveフィールドは空文字列""で固定

## 4. オプション組み合わせ別動作パターン

| useLocationData | fallbackToRegulation | autoLeaveDetection | 動作概要 |
|----------------|---------------------|-------------------|---------|
| true | true | true | 標準的な自動生成（位置情報優先、フォールバック有り、休暇判定有り） |
| true | true | false | 位置情報優先、フォールバック有り、休暇判定無し |
| true | false | true | 位置情報優先、フォールバック無し、休暇判定有り |
| true | false | false | 位置情報優先、フォールバック無し、休暇判定無し |
| false | true | true | 位置情報不使用、規定時間使用、休暇判定有り |
| false | true | false | 位置情報不使用、規定時間使用、休暇判定無し |
| false | false | true | 位置情報不使用、規定時間不使用、休暇判定有り |
| false | false | false | 位置情報不使用、規定時間不使用、休暇判定無し |

### 4.1 推奨使用シーン

#### 4.1.1 標準シーン（true, true, true）
- 通常の勤務月における自動生成
- 位置情報を最大限活用し、データ不足時は安全な規定時間を使用

#### 4.1.2 在宅勤務シーン（false, true, true）
- 在宅勤務期間など位置情報による判定が困難な期間
- 規定時間を使用して安定した勤怠情報を生成

#### 4.1.3 休暇期間シーン（false, false, false）
- 長期休暇期間など勤務がない期間の一括処理
- 全日を欠勤扱いとして処理

## 5. エラーハンドリング

### 5.1 バリデーションエラー
```json
{
  "success": false,
  "error": {
    "code": "INVALID_GENERATE_OPTIONS",
    "message": "自動生成オプションの形式が不正です",
    "details": [
      {
        "field": "generateOptions.useLocationData",
        "message": "Boolean値で指定してください"
      },
      {
        "field": "generateOptions",
        "message": "オブジェクト形式で指定するか、省略してください"
      }
    ]
  }
}
```

### 5.2 エラーケース一覧
| エラーケース | HTTPステータス | エラーコード | 対応方法 |
|------------|---------------|------------|---------|
| generateOptionsが非オブジェクト | 400 | INVALID_GENERATE_OPTIONS | オブジェクト形式で指定するか省略 |
| Boolean以外の値指定 | 400 | INVALID_GENERATE_OPTIONS | 各フィールドはtrue/falseで指定 |
| 位置情報取得エラー（useLocationData: true時） | 422 | LOCATION_DATA_UNAVAILABLE | fallbackToRegulationの有効化を推奨 |

## 6. 機能別適用仕様

### 6.1 勤怠情報登録機能での使用
- **適用タイミング**: `autoGenerate: true`でのリクエスト時
- **パラメータ名**: `generateOptions`
- **必須性**: 任意（省略時はデフォルト値適用）
- **エラーハンドリング**: バリデーションエラー時は400 Bad Requestで応答

### 6.2 勤怠情報更新機能での使用
- **適用タイミング**: `updateType: "regenerate"`でのリクエスト時
- **パラメータ名**: `generateOptions`
- **必須性**: 任意（省略時はデフォルト値適用）
- **処理対象**: `targetDates`で指定された日付のみ
- **エラーハンドリング**: バリデーションエラー時は400 Bad Requestで応答

## 7. 依存関係

### 7.1 関連アルゴリズム
- **勤務判定アルゴリズム**: useLocationDataオプション処理時に使用
- **休暇自動判定アルゴリズム**: autoLeaveDetectionオプション処理時に使用
- **参照先**: [勤務判定アルゴリズム統一標準.md](./勤務判定アルゴリズム統一標準.md)

### 7.2 データ依存
- **位置情報データ**: useLocationDataオプション使用時に必要
- **ユーザー勤怠設定**: fallbackToRegulationオプション使用時に必要
- **休日マスタ**: 休日判定時に必要

## 8. パフォーマンス考慮事項

### 8.1 位置情報処理負荷
- `useLocationData: true`時は位置情報の取得・解析処理が発生
- 大量データ処理時は処理時間の増加を考慮

### 8.2 設定参照負荷
- `fallbackToRegulation: true`時はUserAttendanceSetting参照が発生
- キャッシュ機構により性能影響を最小化

## 9. セキュリティ要件

### 9.1 アクセス制御
- generateOptionsに基づく処理は当該ユーザーのデータのみを対象
- 他ユーザーの位置情報・設定情報へのアクセス不可

### 9.2 データ整合性
- 生成された勤怠情報の論理的整合性チェック
- 不正なオプション組み合わせの検出と適切なエラー応答

## 変更履歴
| 日付 | 変更者 | 変更内容 |
|-----|-------|---------|
| 2025/06/09 | GitHub Copilot | 初版作成 - generateOptions統一仕様の標準化 |

※このドキュメントは開発の進行に合わせて随時更新されます。
