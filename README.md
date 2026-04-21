# 💳 Payment Platform

결제/정산 도메인의 백엔드 시스템을 직접 설계하고 구현한 사이드 프로젝트입니다.
실무에서 경험한 결제 시스템의 복잡한 비즈니스 로직을 바탕으로,
안정성·확장성·운영 가능성을 고려한 아키텍처를 목표로 합니다.

![CI](https://github.com/Yunisung/payment-platform/actions/workflows/ci.yml/badge.svg)

## 🛠 Tech Stack

| 분류 | 기술 |
|---|---|
| Backend | Java 21, Spring Boot 3.x, Spring Data JPA, Spring Batch |
| Database | MySQL 8.0, Redis 7.2 |
| Messaging | Apache Kafka |
| Infra | Docker, Kubernetes, AWS (EC2/RDS) |
| CI/CD | GitHub Actions |
| Test | JUnit5, Mockito, H2, Testcontainers |

## 🏗 Architecture

```
[Client]
   │
   ▼
[api-server]         # 결제 요청/승인/취소 API, JWT 인증
   │
   ├──── MySQL       # 결제/가맹점 데이터 저장
   ├──── Redis       # 멱등성 키 관리, 분산락
   │
   ▼ Kafka Event
[settlement-service] # 결제 이벤트 수신 → 정산 처리
   │
   ▼
[batch]              # 정산 배치 스케줄러 (Spring Batch)
```

## 📦 Module Structure

```
payment-platform/
├── api-server/          # 결제 API 서버
├── settlement-service/  # 정산 서비스 (Kafka Consumer)
├── batch/               # 배치 정산 처리
└── infra/               # Docker Compose 설정
```

## 🔑 Key Features

- **결제 처리**: 결제 요청/승인/취소 API 구현
- **중복 결제 방지**: Redis 분산락을 활용한 멱등성 보장
- **이벤트 기반 정산**: Kafka를 통한 결제 이벤트 발행/구독
- **배치 정산**: Spring Batch를 활용한 일별 정산 처리 (매일 새벽 2시)
- **인증**: JWT 기반 가맹점/어드민 권한 분리

## 📌 API 명세

### 인증

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/api/auth/signup` | 가맹점 회원가입 | 불필요 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) | 불필요 |

**회원가입 요청**
```json
POST /api/auth/signup
{
  "email": "merchant@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

**로그인 응답**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "memberId": 1,
  "email": "merchant@example.com",
  "role": "MERCHANT"
}
```

### 결제

> 모든 결제 API는 `Authorization: Bearer {token}` 헤더 필요

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/payments` | 결제 요청 |
| GET | `/api/payments/{paymentId}` | 결제 조회 |
| DELETE | `/api/payments/{paymentId}` | 결제 취소 |

**결제 요청**
```json
POST /api/payments
{
  "idempotencyKey": "order-20240101-001",
  "amount": 10000,
  "currency": "KRW",
  "orderName": "상품명"
}
```

**결제 응답**
```json
{
  "paymentId": 1,
  "idempotencyKey": "order-20240101-001",
  "amount": 10000,
  "currency": "KRW",
  "orderName": "상품명",
  "status": "APPROVED",
  "createdAt": "2024-01-01T10:00:00"
}
```

**결제 상태 흐름**
```
PENDING → APPROVED → CANCELLED
        ↘ FAILED
```

## 🚀 Getting Started

### 사전 준비
- Java 21+
- Docker Desktop

### 로컬 환경 실행

```bash
# 1. 인프라 실행 (MySQL, Redis, Kafka)
cd infra && docker-compose up -d

# 2. api-server 실행
./gradlew :api-server:bootRun

# 3. settlement-service 실행 (선택)
./gradlew :settlement-service:bootRun

# 4. batch 실행 (선택)
./gradlew :batch:bootRun
```

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 모듈별 테스트
./gradlew :api-server:test
./gradlew :settlement-service:test
./gradlew :batch:test
```

## 📝 설계 기록 (ADR)

프로젝트를 진행하며 내린 기술적 의사결정을 기록합니다.

- [Redis 분산락을 선택한 이유]() - 작성 예정
- [Kafka vs RabbitMQ 비교 및 선택 근거]() - 작성 예정
- [멀티모듈 구조 설계 이유]() - 작성 예정

## 🙋 Author

박윤성 | Backend Engineer
- GitHub: [@Yunisung](https://github.com/Yunisung)
- Email: pys0102@gmail.com
