# STEP 4 계획 — 공통 응답·예외·에러 코드

## 1. 단계 목표

- **공통 성공/실패 응답 envelope** 와 **에러 코드 체계**를 문서로 고정해, 이후 모든 API가 동일한 계약을 따르도록 한다.
- **글로벌 예외 처리** 전략(Spring 기준)과 **HTTP 상태 코드·비즈니스 코드** 매핑 원칙을 정의한다.
- **Swagger(OpenAPI)** 에서 에러 응답을 **재사용 스키마**로 노출하는 규칙을 정한다.
- `docs/backend-api-spec.md` **초안**과 `docs/backend-conventions.md`의 **응답/예외** 절을 STEP 4 기준으로 보강한다.

## 2. 이번 단계에서 해결할 문제

- 컨트롤러·도메인마다 응답 JSON 형태가 달라지는 문제.
- 클라이언트가 **인증 실패(401)** 와 **인가 실패(403)** · **리소스 없음(404)** 를 동일하게 처리하게 되는 문제.
- DB 유니크 위반·검증 실패·외부 API 실패를 **동일한 `error.code`** 규칙으로 노출하지 않는 문제.
- Swagger에 200만 있고 **4xx/5xx 스키마**가 빠지는 문제.

## 3. 설계 대상

| 대상 | 설명 |
|------|------|
| 성공 envelope | `success`, `requestId`, `data`, 선택 `meta`(페이징 등) |
| 실패 envelope | `success`, `requestId`, `error{ code, message, details }` |
| 에러 코드 | `도메인_이유` 형태 UPPER_SNAKE, 카탈로그 초안 |
| 예외 계층 | 도메인 예외 ↔ `ErrorCode` ↔ HTTP 상태 |
| 프레임워크 | Spring `RestControllerAdvice`, Security 예외 진입점 |
| Swagger | 공통 `ApiErrorResponse` 컴포넌트, `@ApiResponse` 패턴 |

## 4. 주요 결정 사항

- **항상 JSON body**에 공통 envelope 사용(성공/실패 동형 필드 `success`, `requestId`).
- **클라이언트 분기 1순위**: HTTP 상태 코드, **2순위**: `error.code`(세부 분기·i18n 키).
- **메시지**: `error.message`는 **사용자/클라이언트 표시용** 한국어(또는 다국어) 허용; **로그·모니터링**은 `error.code` + `requestId` 중심.
- **검증 실패(Bean Validation)**: HTTP **400**, `code`는 `COMMON_VALIDATION_FAILED`, `details`에 필드 단위 정보.
- **DB 유니크 위반** 등 인프라 예외는 **409** 또는 **400**으로 매핑하되 **도메인 코드**로 변환(예: `USER_EMAIL_DUPLICATE`).
- 상세 **엔드포인트별 요청/응답 DTO**는 API 명세에 **초안 수준**만 두고, CRUD 확정은 STEP 9에서 보강한다.

## 5. 생성/수정 예정 파일

| 파일 | 용도 |
|------|------|
| `docs/backend-step4-plan.md` | 본 문서 |
| `docs/backend-api-spec.md` | 공통 응답·에러·글로벌 예외·Swagger 규칙·API 목록 초안 |
| `docs/backend-conventions.md` | §3 예외/응답 규칙 보강, §4 Swagger 에러 절 보강 |
| `docs/backend-step4-result.md` | STEP 4 산출 요약 |

## 6. 구현 범위

- 위 문서 작성·conventions 일부 개정.
- **Java 소스(GlobalExceptionHandler 등)는 STEP 4 범위에서 작성하지 않음**(문서만 고정).

## 7. 제외 범위

- JWT 필터·토큰 페이로드 상세(`backend-auth-spec.md` STEP 5).
- 로깅 MDC·마스킹 상세(`backend-logging-spec.md` STEP 7).
- 각 API의 완전한 요청/응답 필드 목록(후속 단계).

## 8. 다음 단계 연결 포인트

- **STEP 5**: 인증 실패/인가 실패 응답이 본 envelope와 **동일한 `error` 구조**를 쓰도록 `backend-auth-spec.md`에 맞춤.
- **STEP 6**: `backend-swagger-spec.md`에 본 STEP의 `ApiErrorResponse`·`@ApiResponse` 패턴을 상속·세분화.
- **구현 단계**: `global.response`, `global.error` 패키지에 DTO·`ErrorCode`·`RestControllerAdvice` 구현.
