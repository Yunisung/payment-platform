# 💳 Payment Platform

결제/정산 도메인의 백엔드 시스템을 직접 설계하고 구현한 사이드 프로젝트입니다.
실무에서 경험한 결제 시스템의 복잡한 비즈니스 로직을 바탕으로,
안정성·확장성·운영 가능성을 고려한 아키텍처를 목표로 합니다.

## 🛠 Tech Stack

| 분류 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot 3.x, Spring Batch |
| Database | MySQL, Redis |
| Messaging | Apache Kafka |
| Infra | Docker, Kubernetes, AWS (EC2/RDS) |
| CI/CD | GitHub Actions |
| Test | JUnit5, Testcontainers |

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
└── infra/               # Docker Compose, K8s 설정
```

## 🔑 Key Features

- **결제 처리**: 결제 요청/승인/취소/환불 API 구현
- **중복 결제 방지**: Redis 분산락을 활용한 멱등성 보장
- **이벤트 기반 정산**: Kafka를 통한 결제 이벤트 발행/구독
- **배치 정산**: Spring Batch를 활용한 일별/월별 정산 처리
- **인증**: JWT 기반 가맹점/어드민 권한 분리

## 🚀 Getting Started

### 사전 준비
- Java 17+
- Docker Desktop

### 로컬 환경 실행

```bash
# 1. 인프라 실행 (MySQL, Redis, Kafka)
cd infra
docker-compose up -d

# 2. api-server 실행
./gradlew :api-server:bootRun
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