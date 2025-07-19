# Multi-stage build for Spring Boot application
# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# 作業ディレクトリを設定
WORKDIR /app

# pom.xmlをコピー（依存関係のキャッシュ活用）
COPY pom.xml .

# 依存関係をダウンロード（レイヤーキャッシュ最適化）
RUN mvn dependency:go-offline -B

# ソースコードをコピー
COPY src ./src

# アプリケーションをビルド
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# セキュリティベストプラクティス: non-rootユーザーを作成
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

# 必要なパッケージのインストール（curl: ヘルスチェック用）
RUN apk add --no-cache curl

# 作業ディレクトリを設定
WORKDIR /app

# ビルドステージからJARファイルをコピー
COPY --from=builder /app/target/kairos-*.jar app.jar

# 所有権を設定
RUN chown spring:spring app.jar

# non-rootユーザーに切り替え
USER spring:spring

# アプリケーションが使用するポートを公開
EXPOSE 8080

# JVMオプションを環境変数として設定可能にする
ENV JAVA_OPTS=""

# ヘルスチェック設定
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# アプリケーション起動
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]