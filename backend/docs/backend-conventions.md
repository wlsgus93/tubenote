# 백엔드 공통 규칙 (Conventions)

> `backend-architecture.md`와 함께 사용한다.  
> 상세 스펙은 `backend-auth-spec.md`, `backend-swagger-spec.md`, `backend-logging-spec.md`에서 단계적으로 보강한다.

---

## 1. 네이밍 규칙

### 1.1 패키지·디렉터리

- 소문자만 사용, 단어 구분은 생략 또는 한 단어 우선(`user`, `video`, `collection`).
- 루트: `com.myapp.learningtube`.

### 1.2 클래스

| 종류 | 패턴 | 예 |
|------|------|-----|
| 엔티티 | 명사, 단수 | `User`, `Video` |
| Repository | `{Entity}Repository` | `UserRepository` |
| Service | `{Domain}Service` 또는 `{UseCase}Service` | `VideoService` |
| Controller | `{Domain}Controller` | `CollectionController` |
| 요청 DTO | `{동작}{Domain}Request` | `CreateVideoRequest`, `UpdateNoteRequest` |
| 응답 DTO | `{Domain}Response`, `{Domain}SummaryResponse` | `VideoResponse` |
| 예외 | `{상황}{Domain}Exception` | `VideoNotFoundException` |
| 설정 | `{기능}Config` | `SecurityConfig`, `SwaggerConfig` |
| Mapper(수동) | `{A}To{B}Mapper` | `YoutubeVideoDtoMapper` |

### 1.3 메서드·필드

- 메서드: 동사로 시작, `camelCase` (`findById`, `registerChannel`).
- boolean: `is`, `has`, `can` 접두 (`isDeleted`).
- 상수: `UPPER_SNAKE_CASE`.
- DB 컬럼 매핑: 엔티티는 `camelCase`, `@Column(name = "snake_case")`로 DB는 snake_case 권장.

### 1.4 API 경로

- 소문자, 복수 리소스 (`/api/v1/videos`, `/api/v1/collections/{id}`).
- 버전 접두: `/api/v1/...` (프로젝트 합의 후 고정).

---

## 2. DTO / Entity 분리 규칙

### 2.1 원칙

- **Entity는 HTTP 요청/응답에 직접 노출하지 않는다.**
- API 계약은 **전용 DTO**로 유지하여 직렬화 형태·필드 노출을 제어한다.

### 2.2 종류 구분

| 종류 | 용도 | 위치(권장) |
|------|------|------------|
| Request DTO | 입력 검증, OpenAPI 스키마 | `domain.{pkg}.dto.request` |
| Response DTO | 출력 전용 | `domain.{pkg}.dto.response` |
| Command / Query | 애플리케이션 내부 유스케이스 입력(선택) | `domain.{pkg}.command` 등 |
| Infra DTO | YouTube API 등 외부 스펙 | `infra.youtube.dto` |

### 2.3 매핑

- 단순 구조: 서비스 내 팩토리/매퍼 메서드.
- 복잡·다수 필드: 전용 Mapper 클래스(MapStruct 등 도입 시 규칙 통일).
- **양방향 혼동 방지**: Entity → Response, Request → Entity(또는 Command) 방향을 명확히 한다.

### 2.4 검증

- Request DTO에 Bean Validation(`@NotNull`, `@Size` 등) 적용.
- **도메인 불변식**(예: “소유자만 수정”)은 서비스에서 검증한다.

---

## 3. 예외 처리·공통 응답 규칙

> 필드 정의·HTTP 매핑표·에러 코드 카탈로그는 **`docs/backend-api-spec.md`** 가 단일 기준이다.

### 3.1 계층

- **비즈니스 위반**: `*Exception`을 상속한 도메인 예외(런타임) + **`ErrorCode`** (또는 동등한 상수) 보유.
- **인프라/기술 오류**: 래핑하거나 공통 `InfraException` 등으로 구분(팀 합의); 최종 HTTP·envelope는 **글로벌 핸들러**에서 결정.
- **HTTP 매핑**: `@RestControllerAdvice`에서 상태 코드·`error.code` 통일.

### 3.2 공통 성공 응답

- 모든 성공 응답은 JSON **envelope** 를 따른다: `success: true`, `requestId`, `data`, 선택 `meta`(페이징 등).
- HTTP **2xx**; 삭제·갱신도 원칙적으로 **본문 + 200** 으로 통일해 클라이언트 파싱을 단순화한다(`data`가 `null`일 수 있음). 예외 API는 `backend-api-spec.md`에 명시.

### 3.3 공통 실패 응답

- `success: false`, `requestId`, `error: { code, message, details }`.
- 클라이언트는 **HTTP 상태**로 1차 분기, **`error.code`** 로 세부 처리(재시도, 필드 하이라이트, 토큰 갱신 등).
- `error.message`는 표시용; **로그·알림**은 `requestId` + `error.code` 중심.

### 3.4 에러 코드

- 형식: **`{도메인}_{이유}`**, `UPPER_SNAKE_CASE` (`COMMON_VALIDATION_FAILED`, `AUTH_TOKEN_EXPIRED` 등).
- 새 API 추가 시 **새 코드가 필요하면** `backend-api-spec.md` 카탈로그에 먼저 반영 후 구현.

### 3.5 검증·인증·인가

- Bean Validation 실패 → **400**, `COMMON_VALIDATION_FAILED`, `details`에 필드 목록.
- 인증 실패 → **401**, `AUTH_*`; 소유권·역할 → **403**, `ACCESS_*` 또는 `*_ACCESS_DENIED`.
- 리소스 없음(soft delete 포함) → **404**, `*_NOT_FOUND`.

### 3.6 금지

- Controller에서 `try/catch`로 비즈니스 예외를 삼키고 envelope 없이 응답하는 것(스트리밍·웹훅 등 예외는 문서화).
- `Exception`만 던지고 `error.code` 없이 메시지만 노출하는 것.
- Entity를 성공 `data`에 직접 매핑하는 것.

---

## 4. Swagger / OpenAPI 문서화 규칙

> **후순위 금지**: API 추가와 동시에 문서화한다.

### 4.1 Controller

- 클래스에 `@Tag(name, description)`.
- 각 핸들러에 `@Operation(summary, description)`.
- 인증 필요 API: `@SecurityRequirement`로 Bearer JWT 명시.

### 4.2 DTO

- 필드에 `@Schema(description, example, requiredMode)`.
- enum: `@Schema(allowableValues)` 또는 enum 상수에 설명.

### 4.3 에러 응답 (Swagger)

- OpenAPI `components.schemas`에 **`ApiErrorResponse`**(실패 envelope)를 등록하고, 보호 API는 **401·403**, 리소스 API는 **404·409**, 본문 있는 POST/PATCH는 **400** 을 `@ApiResponse`로 명시한다.
- 응답 스키마는 `ApiErrorResponse` **implementation** 으로 재사용; 예시(`examples`)에 `error.code`, `error.message` 샘플을 포함한다.
- **500** 은 매 엔드포인트 중복 대신, 태그/글로벌 설명에 “서버 오류 시 `COMMON_INTERNAL_ERROR`” 를 한 줄 이상 명시 가능.
- 성공 응답은 도메인별 `*Response` DTO를 `data`에 넣는 형태로 문서화(래퍼 타입은 SpringDoc 설정에 따라 커스터마이징).

### 4.4 운영

- **상세 규칙(태그, DTO, enum, 인증·에러 예시, 예시값)** 은 `docs/backend-swagger-spec.md`가 단일 기준이다.
- 본 절과 충돌 시: **공통 envelope·HTTP 코드**는 `backend-api-spec.md`, **JWT·bearerAuth**는 `backend-auth-spec.md`, **문서화 형식**은 `backend-swagger-spec.md`를 우선한다.

---

## 5. 로깅 규칙

> **사후 추가 금지**: 필터/MDC·레벨 기준을 초기에 맞춘다.  
> **상세(레벨, 계층별, 마스킹, requestId/traceId, 비동기)** 는 `docs/backend-logging-spec.md`가 단일 기준이다.

### 5.1 반드시 남길 이벤트

- 요청 시작/종료(메서드, 경로, 상태 코드, duration — 단, 민감 쿼리스트링 제외 또는 마스킹).
- 인증 실패 / 인가 실패(이유 코드 수준, 토큰 원문 금지).
- 외부 API 호출 시작·성공·실패(상대 URL·correlation id, 응답 시간).
- 비즈니스상 중요한 상태 전이(예: 동기화 작업 시작/완료/실패).
- 처리되지 않은 예외: 스택 또는 상관 ID로 추적 가능하게.

### 5.2 금지·마스킹

- Access/Refresh Token, 비밀번호, API 키, 주민번호 등 **전량 로그 금지** 또는 마스킹.
- 이메일 등 식별자는 필요 시 일부만 마스킹.

### 5.3 Correlation

- **requestId**(또는 traceId)를 MDC에 넣고, 로그 패턴에 포함한다.
- 비동기 작업은 **작업 ID**를 로그에 전파한다.

### 5.4 레벨 가이드

| 레벨 | 용도 |
|------|------|
| ERROR | 처리 실패, 즉시 조치 필요 |
| WARN | 재시도 가능 실패, rate limit 등 |
| INFO | 유스케이스 완료, 배치 단위 |
| DEBUG | 개발·특정 프로파일에서만(운영 기본 비활성 권장) |

표·예시 보강은 `backend-logging-spec.md` §2를 참고한다.

---

## 6. 인증 / 인가 기본 규칙

> JWT·필터 상세는 `backend-auth-spec.md`에서 확정한다. 여기서는 **절대 위반하지 않을 원칙**만 고정한다.

### 6.1 토큰

- **Access Token**: 짧은 만료, API 접근용.
- **Refresh Token**: 재발급 전용, 저장 및 폐기 정책은 spec에서 정의.

### 6.2 책임 분리

- JWT 파싱·검증은 **필터 또는 단일 진입점**에서 수행한다.
- Controller에 토큰 문자열 파싱 로직을 복붙하지 않는다.

### 6.3 API 구분

- **공개 API**: 로그인, 헬스, OAuth 콜백 등 — 화이트리스트로 명시.
- **보호 API**: 기본적으로 인증 필요; 리소스 **소유권**은 Service에서 `userId`와 비교.

### 6.4 실패 응답

- **인증 실패**(토큰 없음/만료/위조)와 **인가 실패**(권한 없음, 타인 리소스)를 구분해 클라이언트가 대응 가능하게 한다.

### 6.5 테스트

- Security 통합 테스트 또는 `@WithMockUser` 등으로 보호 API를 검증한다.

---

## 7. 기타 코드 품질

- 매직 넘버/문자열 최소화 → 상수·enum.
- 서비스 메서드 과대 성장 시 private 메서드 또는 컴포넌트 분리.
- 트랜잭션: 변경 유스케이스에 `@Transactional` 범위 명시, 읽기 전용은 `readOnly = true` 검토.

---

## 8. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 1 초안 작성 |
| 0.2 | 2026-03-25 | STEP 4 — §3 응답/예외·에러 코드, §4.3 Swagger 에러 규칙 보강 |
| 0.3 | 2026-03-25 | STEP 6 — §4.4 `backend-swagger-spec.md` 단일 기준 명시 |
| 0.4 | 2026-03-25 | STEP 7 — §5 `backend-logging-spec.md` 단일 기준 명시 |
