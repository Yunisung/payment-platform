# ADR 004 - MDC 기반 요청 추적 (Trace ID)

## 상태
채택 (Accepted)

## 배경

api-server, settlement-service, batch가 각각 독립적으로 실행되는 분산 환경에서,
특정 결제 요청 하나가 어떤 흐름으로 처리됐는지 로그로 추적하기 어렵다는 문제가 있었습니다.

예를 들어 결제 승인 후 정산이 누락됐을 때, 어떤 요청에서 비롯된 이벤트인지
로그 수천 줄 중에서 찾아내려면 공통 식별자가 없으면 불가능합니다.

## 결정

**MDC(Mapped Diagnostic Context) 기반 Trace ID**를 직접 구현

- 요청 진입 시 UUID 기반 8자리 traceId 생성
- MDC에 등록해 해당 요청의 모든 로그에 자동 포함
- 응답 헤더 `X-Trace-Id`로 클라이언트까지 전파

## 이유

### Zipkin/Jaeger 같은 분산 트레이싱 도구를 선택하지 않은 이유

| 항목 | 외부 트레이싱 도구 | MDC 직접 구현 |
|------|-------------------|--------------|
| 구성 복잡도 | 별도 서버 운영 필요 | 필터 클래스 하나로 완결 |
| 운영 비용 | Zipkin/Jaeger 컨테이너 추가 | 없음 |
| 기능 범위 | 서비스 간 시각적 추적 | 로그 레벨 요청 추적 |
| 적합한 규모 | 마이크로서비스 10개 이상 | 소규모 멀티모듈 |

이 프로젝트의 목적은 운영 환경에서 로그만으로 문제를 추적할 수 있는 최소한의 기반을 갖추는 것이었습니다.
별도 인프라 없이 MDC 한 줄로 동일한 효과를 낼 수 있어 직접 구현을 선택했습니다.

### 구현 방식

```
HTTP 요청 수신
  → MdcLoggingFilter: traceId = UUID (8자리) 생성
  → MDC.put("traceId", traceId)
  → 이후 모든 로그: [traceId=a1b2c3d4] 자동 포함
  → 응답 헤더: X-Trace-Id: a1b2c3d4
  → MDC.clear() (요청 종료 후 반드시 초기화)
```

```
로그 예시
[traceId=a1b2c3d4] 결제 승인 완료. paymentId=42, memberId=1, amount=10000
[traceId=a1b2c3d4] PaymentApprovedEvent 발행. paymentId=42
```

클라이언트가 문제를 신고할 때 `X-Trace-Id` 값만 있으면
해당 요청과 관련된 모든 로그를 즉시 필터링할 수 있습니다.

## 트레이드오프

| 항목 | 내용 |
|------|------|
| 장점 | 추가 인프라 없이 요청 단위 로그 추적 가능 |
| 단점 | 서비스 간 traceId 전파 미구현 (Kafka 이벤트에 포함되지 않음) |
| 보완 | `PaymentApprovedEvent`에 traceId 필드 추가 시 서비스 간 추적도 가능 |
