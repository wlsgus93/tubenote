# STEP 7 계획 — 로깅 전략 고정

## 1. 단계 목표

- **구조화된 로깅 전략**을 문서로 고정해 장애 분석·보안·감사 추적이 가능하도록 한다.
- **요청/응답 경계**, **Controller/Service**, **외부 API(YouTube 등)**, **예외**, **인증/인가**, **비동기 작업** 각각에 대한 **로그 레벨·필드·금지 사항**을 정의한다.
- **`requestId` / `traceId`** 와 MDC 전파 규칙을 `backend-api-spec.md`의 `requestId`와 맞춘다.

## 2. 이번 단계에서 해결할 문제

- 로그가 콘솔에만 흩어져 **검색·알람**이 불가능한 문제.
- 토큰·비밀번호·자막 전문 등 **민감정보 유출** 위험.
- 비동기 스레드에서 **상관 ID 단절**로 원인 추적 실패.
- 동일 이벤트에 **DEBUG와 INFO가 중복**되어 비용만 증가하는 문제.

## 3. 설계 대상

| 대상 | 설명 |
|------|------|
| 스택 | SLF4J + Logback(가정), JSON appender(운영 권장) |
| MDC 키 | `requestId`, `traceId`, `userId`(선택) |
| 필터 | 요청 시작/종료, duration, status |
| 계층별 정책 | Controller, Service, Infra client |
| 마스킹 | 토큰, 비밀번호, 이메일, Authorization 헤더 등 |
| 비동기 | `@Async`, 메시지 컨슈머, `sync_jobs` 연계 |

## 4. 주요 결정 사항

- **상관 ID**: HTTP 요청마다 **`requestId`** (UUID) 생성 또는 `X-Request-Id` 수신; 응답 body의 `requestId`와 **동일 값**을 MDC에 넣는다. 분산 추적 확장 시 **`traceId`** 를 별도 헤더(`traceparent` 또는 `X-Trace-Id`)로 수용 가능.
- **운영 기본 레벨**: **INFO**; **DEBUG**는 로컬/특정 프로파일만.
- **본문 로깅**: 요청/응답 **JSON 전문**은 **기본 금지**; 필요 시 특정 API만 **샘플링·길이 제한** 후 DEBUG.
- **인증**: 실패 시 **이유 코드**만; **Authorization** 값은 **절대** 로그에 남기지 않음.
- **비동기**: 작업 시작 시 MDC에 `jobId` / `syncJobId` 설정, 종료 시 정리(`finally`).

## 5. 생성/수정 예정 파일

| 파일 | 용도 |
|------|------|
| `docs/backend-step7-plan.md` | 본 문서 |
| `docs/backend-logging-spec.md` | 로깅 상세 명세 |
| `docs/backend-step7-result.md` | STEP 7 산출 요약 |
| `docs/backend-conventions.md` | §5와 `backend-logging-spec.md` 연결 보강 |

## 6. 구현 범위

- 문서 작성 및 conventions 소규모 개정. **Logback XML/Java 필터 코드는 작성하지 않음**.

## 7. 제외 범위

- ELK/Datadog 등 **수집 파이프라인** 상세.
- 로그 보관 기간·법적 보관 정책(운영 정책 문서로 분리 가능).

## 8. 다음 단계 연결 포인트

- **STEP 8** `backend-async-spec.md`: 이벤트·재시도·멱등성과 **로그 키** 정합.
- **구현**: `RequestLoggingFilter`, MDC clear, `ClientHttpRequestInterceptor`(YouTube), `@Async` MDC TaskDecorator.
