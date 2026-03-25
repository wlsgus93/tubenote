# 백엔드 로깅 명세

> STEP 7 산출물. API 응답의 `requestId`는 `backend-api-spec.md`와 **동일 값**을 권장한다.  
> 구현 스택 가정: **SLF4J + Logback**, Spring Boot 3.

---

## 1. 로그 목적

| 목적 | 설명 |
|------|------|
| 장애 대응 | 요청 단위로 원인(외부 API, DB, 권한) 추적 |
| 보안 감사 | 인증 실패·의심 패턴(반복 401) 탐지 |
| 성능 | 처리 시간(duration), 외부 호출 지연 |
| 비즈니스 가시성 | 동기화 작업·배치 성공/실패(PII 없이) |
| 규정·분쟁 대비 | 민감정보는 마스킹·최소 수집 |

---

## 2. 로그 레벨 기준

| 레벨 | 사용 시점 | 예시 |
|------|-----------|------|
| **ERROR** | 요청 처리 실패로 **5xx** 반환, 복구 불가 예외, 외부 API 연속 실패 | `UnhandledException`, DB 커넥션 고갈 |
| **WARN** | **4xx** 중 주의 필요, 재시도 가능 실패, 할당량 임박 | YouTube 429, 낙관적 락 충돌, 비정상 입력 패턴 |
| **INFO** | 정상 유스케이스 **마일스톤**, 요청 완료 한 줄, 작업 시작/완료 | `request completed`, `sync job started` |
| **DEBUG** | 개발·특정 프로파일만; 쿼리 파라미터 일부, 매퍼 입출력(민감 제외) | SQL 파라미터(마스킹), 매핑 결과 요약 |
| **TRACE** | 기본 **비활성**; 필요 시 프레임워크 디버깅 전용 | — |

### 2.1 운영 기본

- 루트 로거 **INFO**; `com.myapp.learningtube` 패키지는 INFO 기본.
- 서드파티( Hibernate SQL 등)는 **WARN** 이상으로 제한 권장.

### 2.2 금지

- ERROR로 “정상 흐름” 기록(알람 피로).
- INFO에 **대용량 본문**·**토큰**·**자막 전문**.

---

## 3. Controller 로그 정책

### 3.1 요청 경계(필터 또는 Interceptor)

| 시점 | 레벨 | 포함 필드(권장) |
|------|------|----------------|
| 요청 시작 | INFO 또는 DEBUG | `requestId`, `method`, `path`(쿼리스트링 제외 또는 마스킹), `clientIp`(선택) |
| 요청 종료 | INFO | `requestId`, `method`, `path`, `status`, `durationMs` |

- **경로 파라미터**(`{id}`)는 로그 가능; **쿼리**에 `token`, `password` 키가 있으면 **마스킹 또는 제외**.
- **요청/응답 Body**는 **로깅하지 않음**(기본). 디버깅이 필요하면 **DEBUG** + **길이 상한(예: 512자)** + 민감 필드 제거 유틸.

### 3.2 Controller 클래스

- **비즈니스 로직 로그는 지양**; 위임은 Service로.
- 허용: 진입 확인 수준 **DEBUG** (과다 출력 주의).

---

## 4. Service 로그 정책

| 상황 | 레벨 | 내용 |
|------|------|------|
| 유스케이스 성공 | INFO(선택) | 도메인 이벤트 1줄 — `userId`, `resourceType`, `resourceId`(내부 id) |
| 비즈니스 규칙 위반으로 4xx | INFO 또는 WARN | `error.code`, `requestId`, 원인 키워드(PII 없음) |
| 예상치 못한 예외 전파 전 | — | Service에서 잡지 않고 글로벌 핸들러로 넘김; **중복 스택 금지** |

### 4.1 원칙

- **한 요청당 INFO 로그 과다** 방지: 핵심 트랜잭션만.
- `userId` 로깅은 **인증된 주체** 기준; **클라이언트가 보낸 userId 문자열**은 신뢰·로그 가치 낮음.

---

## 5. 외부 API(YouTube 등) 호출 로그 정책

### 5.1 클라이언트 인터셉터 / Aspect

| 시점 | 레벨 | 필드 |
|------|------|------|
| 요청 전 | INFO | `requestId`, `provider`(YOUTUBE), `operation`(videos.list 등), `resourceKey`(videoId/channelId 마스킹 불필요한 공개 식별자) |
| 성공 후 | INFO | 위 + `durationMs`, `httpStatus` |
| 실패 | WARN 또는 ERROR | 위 + `httpStatus`, **응답 body 일부 상한**(에러 메시지), **API 키·토큰 금지** |

### 5.2 금지

- OAuth 액세스 토큰, API 키, `Authorization` 헤더 값.
- 전체 응답 JSON(대용량).

### 5.3 할당량·429

- **WARN**: `YOUTUBE_QUOTA_EXCEEDED` 또는 429, 재시도 정책은 STEP 8과 연계.

---

## 6. 예외 로그 정책

| 구분 | 레벨 | 내용 |
|------|------|------|
| 처리된 4xx(비즈니스) | INFO/WARN | `requestId`, 예외 타입, `error.code`, **스택 생략** |
| 5xx(미처리) | ERROR | `requestId`, 예외 타입, 메시지, **스택 1회**(중복 방지) |
| Security 인증 실패 | INFO 또는 WARN | `requestId`, `AUTH_*` 수준, **토큰 미포함** |

### 6.1 글로벌 예외 핸들러

- **한 곳**에서만 스택을 ERROR로 남긴다.
- Controller/Service에서 **catch 후 로그만 남기고 삼키기** 금지(문서화된 경우 제외).

---

## 7. 인증 / 인가 로그 정책

| 이벤트 | 레벨 | 허용 필드 | 금지 |
|--------|------|-----------|------|
| JWT 검증 실패 | INFO/WARN | `requestId`, 실패 사유 코드(expired, invalid_signature) | Bearer 문자열, 클레임 전문 |
| 로그인 실패 | INFO | `requestId`, 이메일 **마스킹** | 비밀번호, 성공 여부 상세 노출 최소화 |
| 로그인 성공 | INFO | `requestId`, `userId`(내부 id) | 토큰 |
| 로그아웃 / Refresh 폐기 | INFO | `requestId`, `userId` | refresh 원문 |
| 인가 실패(403) | INFO | `requestId`, `userId`, 접근 시도 path | 불필요한 리소스 상세 |

---

## 8. 민감정보 마스킹 정책

### 8.1 절대 로그 금지

- Access / Refresh **토큰 원문**, `Authorization` 헤더 전체
- 비밀번호, 비밀번호 해시, API 키, OAuth secret
- `Transcript.content` 전문, 신용카드 등 결제 정보(해당 시)

### 8.2 마스킹 규칙

| 데이터 | 규칙 |
|--------|------|
| 이메일 | `ab***@example.com` 또는 로컬파트만 앞 2자 + `***` |
| IP | 운영 정책에 따라 뒤 옥텟 마스킹(선택) |
| 검증 실패 `rejectedValue` | 비밀번호·토큰 필드는 **null** 또는 `"***"` |

### 8.3 구현 힌트

- `MaskingUtils` 또는 Logback **converter**로 공통화.
- 구조화 로그(JSON) 시 필드 단위 `redacted: true`.

---

## 9. traceId / requestId 전략

### 9.1 용어

| 키 | 설명 |
|-----|------|
| **requestId** | 단일 HTTP 요청·응답 단위 식별자; **API 응답 body**와 동일 값 권장. |
| **traceId** | 분산 추적 ID(옵션). OpenTelemetry 도입 시 `traceId`와 통합 가능. |

### 9.2 생성·전파

1. 클라이언트가 `X-Request-Id`내면 **유효한 UUID/문자열**만 수용(길이 상한 예: 128).
2. 없으면 서버가 UUID 생성.
3. 필터 **최상단**에서 MDC에 `requestId` 설정, 응답 헤더 `X-Request-Id` 에도 동일 값 설정(권장).
4. 요청 종료 시 **반드시 MDC clear** (`finally`).

### 9.3 로그 패턴

- JSON: `"requestId":"..."` 필수.
- 텍스트: `[requestId=...]` 접두.

### 9.4 traceId (선택)

- `X-Trace-Id` 또는 W3C `traceparent` 수신 시 MDC `traceId` 설정.
- 없으면 `requestId`만으로도 단일 서비스 내 추적 가능.

---

## 10. 비동기 작업 로그 연계

### 10.1 대상

- `@Async` 메서드, 메시지 컨슈머(추후), `sync_jobs` 기반 배치, 스케줄러.

### 10.2 규칙

| 단계 | 내용 |
|------|------|
| 작업 시작 | MDC에 `jobId` 또는 `syncJobId`, 가능하면 **원 요청의 `requestId`** 복사 |
| 로그 | 모든 해당 작업 로그에 동일 키 포함 |
| 작업 종료 | `finally`에서 MDC 제거(비동기 스레드 풀 오염 방지) |

### 10.3 TaskDecorator

- Spring `@Async` 사용 시 **`MdcTaskDecorator`** 로 신규 스레드에 MDC 복사 후 종료 시 클리어.

### 10.4 실패

- **ERROR**: 작업 실패 + `syncJobId` + `error.code`(도메인).
- 재시도 큐: **WARN** + 시도 횟수.

---

## 11. 구조화 로그(JSON) 권장 필드

| 필드 | 설명 |
|------|------|
| `@timestamp` | ISO-8601 |
| `level` | ERROR/WARN/INFO/DEBUG |
| `logger` | 클래스명 |
| `thread` | 스레드명 |
| `requestId` | MDC |
| `traceId` | MDC(선택) |
| `userId` | 인증된 경우만(선택) |
| `message` | 사람이 읽을 문장 |
| `error.code` | 있을 때만 |

---

## 12. 구현 체크리스트

- [ ] 요청 필터: MDC 설정/해제, duration, status
- [ ] 응답 `requestId`와 MDC 동기화
- [ ] YouTube 클라이언트 인터셉터 로그
- [ ] 글로벌 예외 핸들러 ERROR 1회
- [ ] `@Async` MDC 데코레이터
- [ ] 운영 JSON appender + 로그 레벨 프로파일

---

## 13. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 7 초안 |
