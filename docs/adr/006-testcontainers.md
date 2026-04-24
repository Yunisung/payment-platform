# ADR 006 - 테스트 전략: Testcontainers 선택 (Mock vs 실제 컨테이너)

## 상태
채택 (Accepted)

## 배경

테스트에서 MySQL, Redis, Kafka 등 인프라 의존성을 어떻게 처리할지 결정이 필요했습니다.
주요 선택지는 두 가지였습니다.

| 방식 | 설명 |
|------|------|
| Mock/인메모리 | Mockito로 Repository 목킹, H2 인메모리 DB, EmbeddedKafka 사용 |
| Testcontainers | 테스트 실행 시 실제 MySQL·Kafka Docker 컨테이너를 직접 구동 |

## 결정

**Testcontainers** — 실제 컨테이너를 띄워 테스트

- api-server, batch: MySQL 컨테이너
- settlement-service: Kafka 컨테이너

## 이유

### Mock/인메모리의 문제

**H2와 MySQL은 다릅니다.**

H2는 MySQL 호환 모드를 지원하지만, 실제로는 동작이 다른 경우가 존재합니다.

```
예시: MySQL의 DATETIME(6) 마이크로초 정밀도
     H2에서는 동작하지만 MySQL에서 컬럼 타입 불일치로 오류 발생 가능

예시: JPA의 네이티브 쿼리, DB 함수 호출
     H2 방언으로 작성한 쿼리가 MySQL에서 문법 오류 발생 가능
```

Mock이나 인메모리 DB로 테스트가 통과해도,
**실제 환경에서 다른 이유로 실패할 수 있다는 신뢰 문제**가 핵심입니다.

### Testcontainers의 장점

```
./gradlew test 실행
  → MySQL 8.0 컨테이너 자동 시작 (운영 환경과 동일 버전)
  → 실제 DDL 적용, 실제 쿼리 실행
  → 테스트 완료 후 컨테이너 자동 종료
```

- 운영 환경과 동일한 DB 버전·설정으로 테스트
- 컨테이너 생명주기를 테스트 프레임워크가 자동 관리
- CI 환경(GitHub Actions)에서도 Docker만 있으면 동일하게 동작

## 트레이드오프

| 항목 | 내용 |
|------|------|
| 장점 | 실제 인프라와 동일한 환경, Mock/실제 환경 불일치 버그 사전 차단 |
| 단점 | 테스트 실행 속도 느림 (컨테이너 시작 시간 추가), Docker 실행 필수 |
| 보완 | `@SpringBootTest` 공유 컨텍스트로 컨테이너 재시작 횟수 최소화 |
