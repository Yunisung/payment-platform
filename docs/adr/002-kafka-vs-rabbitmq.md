# ADR 002 - 메시징 브로커: Kafka 선택

## 상태
채택 (Accepted)

## 배경

결제 승인 후 정산 서비스에 이벤트를 전달하기 위한 메시징 브로커가 필요했습니다.
주요 후보는 **Apache Kafka**와 **RabbitMQ**였습니다.

## 결정

**Apache Kafka** 선택

## 비교

| 항목 | Kafka | RabbitMQ |
|------|-------|----------|
| 메시지 보관 | 디스크에 보관 (기본 7일) | 소비 후 즉시 삭제 |
| 재처리 | offset으로 과거 메시지 재소비 가능 | 불가 |
| 처리량 | 초당 수십만 건 | 초당 수만 건 |
| 순서 보장 | 파티션 내 보장 | 큐 단위 보장 |
| 소비 방식 | Consumer Pull | Broker Push |
| 운영 복잡도 | 높음 (Zookeeper 필요) | 낮음 |
| 적합한 use case | 대용량 이벤트 스트리밍, 감사 로그 | 작업 큐, RPC |

## 이유

### 결제 도메인에서 Kafka가 더 적합한 이유

**1. 메시지 재처리 가능**

정산 서비스 장애 시 RabbitMQ는 메시지가 유실될 수 있습니다.
Kafka는 offset을 되돌려 장애 구간의 메시지를 재처리할 수 있습니다.

```
정산 서비스 장애 발생 (offset 100 ~ 200 처리 실패)
  → 복구 후 offset 100으로 되돌려 재처리 가능
```

**2. 이벤트 소싱 확장성**

현재는 settlement-service 하나만 구독하지만,
향후 알림 서비스, 정산 통계 서비스 등이 추가될 때
**같은 토픽을 여러 Consumer Group이 독립적으로 소비** 가능합니다.

```
payment.approved 토픽
  ├── settlement-group   (정산 처리)
  ├── notification-group (결제 알림) ← 나중에 추가 가능
  └── analytics-group    (통계 집계) ← 나중에 추가 가능
```

**3. at-least-once 보장과 중복 방어**

Kafka는 at-least-once 전달을 보장하므로 중복 소비가 발생할 수 있습니다.
settlement-service에서 `paymentId` 중복 체크로 이를 방어합니다.

## 트레이드오프

| 항목 | 내용 |
|------|------|
| 장점 | 메시지 재처리, 다중 컨슈머 독립 소비, 높은 처리량 |
| 단점 | Zookeeper 의존, 운영 복잡도 높음, 소규모에는 오버스펙 |
| 보완 | 중복 소비 방어 로직(idempotency check) 필수 구현 |
