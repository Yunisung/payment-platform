# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 빌드 및 실행 명령어

```bash
# 전체 모듈 빌드
./gradlew build

# 특정 모듈 실행
./gradlew :api-server:bootRun
./gradlew :settlement-service:bootRun
./gradlew :batch:bootRun

# 전체 테스트 실행
./gradlew test

# 단일 모듈 테스트 실행
./gradlew :api-server:test
./gradlew :settlement-service:test
./gradlew :batch:test
```

## 인프라

로컬에서 모듈을 실행하기 전에 필요한 모든 서비스(MySQL, Redis, Zookeeper, Kafka)를 먼저 시작해야 합니다:

```bash
cd infra && docker-compose up -d
```

| 서비스     | 포트 | 접속 정보                          |
|------------|------|-------------------------------------|
| MySQL 8.0  | 3306 | payment/payment1234, db: payment_db |
| Redis 7.2  | 6379 | —                                   |
| Kafka      | 9092 | PLAINTEXT://localhost:9092          |

## 아키텍처

멀티모듈 Gradle 프로젝트(`settings.gradle`에 `api-server`, `settlement-service`, `batch` 포함). 세 모듈 모두 독립적인 Spring Boot 3.x 애플리케이션으로, 런타임 코드를 공유하지 않고 루트 `build.gradle`의 공통 Gradle 설정만 공유합니다.

**api-server** — REST API 레이어. 결제 요청 수신, JWT 토큰 발급(JJWT 0.12.3), Redis 분산락을 활용한 멱등성 보장, Spring Data JPA를 통한 MySQL 데이터 저장. 진입점: `com.payment.api.ApiServerApplication`

**settlement-service** — 이벤트 기반 컨슈머. Kafka 토픽에서 결제 이벤트를 수신하고 정산 레코드를 MySQL에 저장. 진입점: `com.payment.settlement.SettlementApplication`

**batch** — 스케줄 처리. Spring Batch 잡으로 정산 데이터를 주기적으로 집계 및 확정. 진입점: `com.payment.batch.BatchApplication`

## 테스트

JUnit 5와 Testcontainers 사용(api-server·batch는 MySQL, settlement-service는 Kafka). 인프라 모킹 없이 실제 컨테이너를 직접 실행합니다. 테스트 실행 전 Docker가 구동 중인지 확인해야 합니다.
