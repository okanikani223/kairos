# Kairos - 勤怠管理システム

Kairosは、Spring Bootで構築された勤怠管理システムです。勤怠表の管理、位置情報の追跡、勤務ルールの設定などの機能を提供します。

## 技術スタック

- Java 21
- Spring Boot 3.5.3
- PostgreSQL 42.7.3
- Spring Security (JWT認証)
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

## ビルドとテスト

```bash
# プロジェクトのビルド
mvn clean package

# テストの実行（開発プロファイルで実行）
mvn test

# 特定のプロファイルでテスト
mvn test -Dspring.profiles.active=dev
```

## 注意事項

- 開発環境（InMemory実装）ではアプリケーション再起動時にデータが消失します
- 本番環境ではPostgreSQLが起動している必要があります
- 環境変数`SPRING_PROFILES_ACTIVE`はコマンドライン引数より優先度が低いです