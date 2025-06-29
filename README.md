# Kairos - 勤怠管理システム

Kairosは、Java 21とSpring Boot 3.5.3で構築された勤怠管理システムです。クリーンアーキテクチャとドメイン駆動設計（DDD）の原則に基づいて実装されており、勤怠表の管理、位置情報の追跡、勤務ルールの設定などの機能を提供します。

## 技術スタック

- Java 21
- Spring Boot 3.5.3 (Web, Security, Data JPA, Validation)
- PostgreSQL 42.7.3
- Spring Security + JWT認証 (jjwt 0.12.3)
- Spring Data JPA
- Maven

## プロファイル設定

本システムは、Spring Profilesを使用して開発環境と本番環境で異なるリポジトリ実装を切り替えることができます。

### 開発環境（dev profile）

開発環境では、データベース接続不要のInMemory実装を使用します。高速な開発とテストが可能です。

```bash
# 開発環境での起動（デフォルト）
mvn spring-boot:run

# または明示的にdevプロファイルを指定
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 環境変数で指定
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### 本番環境（prod profile）

本番環境では、PostgreSQLを使用したJPA実装で永続化を行います。

```bash
# 本番環境での起動
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 環境変数で指定
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run

# JARファイルでの実行
java -jar target/kairos-0.1.0.jar --spring.profiles.active=prod
```

### プロファイル別の特徴

| 設定項目 | 開発環境（dev） | 本番環境（prod） |
|---------|----------------|-----------------|
| リポジトリ実装 | InMemory | JPA (PostgreSQL) |
| データ永続化 | なし（メモリ内のみ） | あり |
| データベース接続 | 不要 | 必要 |
| 起動速度 | 高速 | 通常 |
| 用途 | 開発・テスト | 本番運用 |

### デフォルトプロファイル

プロファイルが指定されていない場合、`dev`プロファイルがデフォルトで使用されます。これは`application.yml`で設定されています。

## プロファイル別設定ファイル

- `src/main/resources/application.yml` - 共通設定とデフォルトプロファイル設定
- `src/main/resources/application-dev.yml` - 開発環境専用設定
- `src/main/resources/application-prod.yml` - 本番環境専用設定
- `src/test/resources/application-test.yml` - テスト環境専用設定

## Docker環境

本番環境でPostgreSQLを使用する場合は、同梱のdocker-compose.ymlを使用してデータベースを起動できます：

```bash
cd kairos-backend
docker-compose up -d
```

## アーキテクチャ

本プロジェクトはクリーンアーキテクチャとドメイン駆動設計（DDD）の原則に従って構築されています。

### ドメイン構成

- `reports` - 勤怠表管理ドメイン
- `locations` - 位置情報追跡ドメイン（勤怠表生成用途のみ）
- `rules` - 勤務ルール管理ドメイン
- `reportcreationrules` - 勤怠表作成ルール管理ドメイン
- `security` - セキュリティ・認証コンポーネント

### パッケージ構造

各ドメインは以下の構造に従います：
- `domains/models/` - ドメインモデル（エンティティ、値オブジェクト、リポジトリ）
- `domains/service/` - ドメインサービス
- `applications/usecases/` - アプリケーション層ユースケース
- `others/` - インターフェースアダプター層（コントローラー、リポジトリ実装）

## ビルドとテスト

```bash
# プロジェクト作業ディレクトリに移動
cd kairos-backend

# プロジェクトのコンパイル
mvn clean compile

# プロジェクトのビルド
mvn clean package

# テストの実行（開発プロファイルで実行）
mvn test

# アプリケーションの起動
mvn spring-boot:run
```

## API エンドポイント

### 認証
- **POST** `/api/auth/login` - JWT トークン取得

### 勤怠表管理
- **POST** `/api/reports` - 新規勤怠表登録
- **GET** `/api/reports/{year}/{month}` - 勤怠表取得
- **PUT** `/api/reports/{year}/{month}` - 勤怠表更新
- **DELETE** `/api/reports/{year}/{month}` - 勤怠表削除
- **POST** `/api/reports/generate` - 位置情報から勤怠表生成

### その他の主要エンドポイント
- **POST** `/api/locations` - 位置情報登録
- **POST** `/api/work-rules` - 勤務ルール登録
- **POST** `/api/default-work-rules` - デフォルト勤務ルール登録
- **POST** `/api/report-creation-rules` - 勤怠表作成ルール登録

## セキュリティ

- JWT認証を使用したステートレスセッション管理
- `/api/auth/**` 以外の全エンドポイントで認証が必要
- Authorization ヘッダーでのBearer Token認証: `Bearer {token}`

## 開発ガイドライン

詳細な開発ガイドラインについては以下のドキュメントを参照してください：
- [`CLAUDE.md`](./CLAUDE.md) - プロジェクト全体のガイドライン
- [`docs/DEVELOPMENT.md`](./docs/DEVELOPMENT.md) - 開発ベストプラクティス
- [`docs/API.md`](./docs/API.md) - API仕様詳細

## 注意事項

- 開発環境（InMemory実装）ではアプリケーション再起動時にデータが消失します
- 本番環境ではPostgreSQLが起動している必要があります
- 環境変数`SPRING_PROFILES_ACTIVE`はコマンドライン引数より優先度が低いです
- 位置情報は勤怠表生成用途のみに使用されます（高度な地理的機能は対象外）