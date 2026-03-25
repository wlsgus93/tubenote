# STEP 4 결과 — 공통 응답·예외·에러 코드 고정

## 1. 구현 완료 항목

- **공통 성공 envelope**(`success`, `requestId`, `data`, `meta`)와 **실패 envelope**(`success`, `requestId`, `error{code,message,details}`)를 `backend-api-spec.md`에 정의함.
- **HTTP 상태 ↔ 용도** 매핑과 **`{도메인}_{이유}`** 에러 코드 분류·**카탈로그 초안**을 문서화함.
- **Spring 글로벌 예외 처리** 전략(`RestControllerAdvice`, 예외 타입별 매핑, 로깅, soft delete와 404 정책)을 `backend-api-spec.md` §6에 정리함.
- **Swagger** 에서 `ApiErrorResponse` 재사용·`@ApiResponse` 최소 세트·예시값 규칙을 `backend-api-spec.md` §7에 정리하고, `backend-conventions.md` §3·§4.3을 동일 기준으로 보강함.
- **API 목록 초안** 표(헬스, 인증, me, 컬렉션 등)를 `backend-api-spec.md` §8에 추가함.
- **Java 코드**는 작성하지 않음(문서만 고정).

## 2. 생성/수정한 파일 목록

| 파일 | 비고 |
|------|------|
| `docs/backend-step4-plan.md` | 신규 |
| `docs/backend-api-spec.md` | 신규 |
| `docs/backend-step4-result.md` | 신규 |
| `docs/backend-conventions.md` | §3 전면 보강, §4.3·§4.4 수정, 개정 이력 0.2 |

## 3. 핵심 클래스/구조 설명

- 코드 미구현. 구현 시 권장 패키지: `global.response`(래퍼 DTO), `global.error`(`ErrorCode`, `BusinessException`, `@RestControllerAdvice`).
- 단일 기준 문서: **`docs/backend-api-spec.md`**.

## 4. 반영된 설계 원칙

- 성공/실패 **동형 추적**(`requestId`).
- 클라이언트 분기: **HTTP 우선**, **`error.code`** 로 세부 처리.
- 인증(401)·인가(403)·검증(400)·충돌(409) 응답을 문서상 분리.

## 5. Swagger 반영 내용

- `ApiErrorResponse` 컴포넌트·엔드포인트별 400/401/403/404/409 문서화 의무를 §7·`conventions` §4.3에 반영.

## 6. 로깅 반영 내용

- 4xx/5xx 로깅 레벨 가이드와 500 시 스택은 로그만(민감 정보 비노출)을 §6.4에 명시.

## 7. 아쉬운 점 / 개선 포인트

- 헬스 체크만 envelope 생략 여부는 운영 모니터링 도구와 맞춰 최종 결정 필요.
- **422** 사용 여부 미확정 — 400으로 통일 시 `backend-api-spec.md` §4 표를 갱신할 것.
- `data` 제네릭 래퍼의 SpringDoc 표현은 구현 단계에서 플러그인/수동 스키마로 보완.

## 8. 다음 단계 TODO

| 순서 | 작업 |
|------|------|
| STEP 5 | `backend-auth-spec.md` — JWT·필터·401/403 응답을 **동일 envelope** 로 명시 |
| STEP 6 | `backend-swagger-spec.md` — 태그·예시·전역 500 문구·`ApiSuccessResponse` 표현 |
| 구현 | `global.error` / `global.response` 코드 생성 및 통합 테스트 |

---

## 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 4 완료 기준 초안 |
