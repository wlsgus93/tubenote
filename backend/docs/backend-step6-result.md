# STEP 6 결과 — Swagger/OpenAPI 문서화 기준 고정

## 1. 구현 완료 항목

- OpenAPI **적용 범위·도구 가정**(springdoc, OAS 3.0)을 정의함.
- **태그 분류** 표(Health, Auth, User, Collections, …) 및 태그 작성 규칙을 `backend-swagger-spec.md`에 정리함.
- **Controller**(`@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`, `@SecurityRequirement`) 규칙을 정의함.
- **Request/Response DTO** `@Schema` 필수 항목·envelope 문서화 방식(권장 A: 래퍼 스키마)을 정의함.
- **enum**·**에러 코드** 문서화 방식을 정의함.
- **인증 API**·**bearerAuth**·공개/보호 문서 차이를 `backend-auth-spec.md`와 맞춤.
- **에러 응답** `ApiErrorBody` / `ApiErrorResponse` 및 Operation별 최소 HTTP 코드 세트를 §7로 고정함.
- **예시값** PII·토큰·일관성 규칙을 §8로 정의함.
- **Java/Gradle 코드 미작성**(문서만).

## 2. 생성/수정한 파일 목록

| 파일 | 비고 |
|------|------|
| `docs/backend-step6-plan.md` | 신규 |
| `docs/backend-swagger-spec.md` | 신규 |
| `docs/backend-step6-result.md` | 신규 |
| `docs/backend-conventions.md` | §4.4 상세 명세 포인터 보강, 개정 0.3 |

## 3. 핵심 클래스/구조 설명

- 구현 시: `OpenApiConfig`, `ApiErrorResponse`·`ApiErrorBody` DTO, Controller 어노테이션 일괄 적용.
- 규칙 단일 기준: **`docs/backend-swagger-spec.md`**.

## 4. 반영된 설계 원칙

- Entity 비노출, DTO 중심 스키마.
- 인증·에러·envelope를 **API 문서에서도** STEP 4·5와 동형으로 설명.

## 5. Swagger 반영 내용

- 본 STEP이 Swagger 규칙의 **본문**이 됨; `backend-api-spec.md` §7은 요약 역할.

## 6. 로깅 반영 내용

- 예시값에 토큰·실데이터 금지 — §8·§6.4와 `backend-logging-spec.md`(STEP 7)에서 재강조 가능.

## 7. 아쉬운 점 / 개선 포인트

- 성공 envelope의 **제네릭** 표현은 SpringDoc 한계로 **도메인별 래퍼** 또는 Customizer 선택이 필요 — 구현 시 팀에서 A안 확정 권장.
- `Collection Videos` 태그를 `Collections`에 흡수할지는 UI 선호에 따라 조정.

## 8. 다음 단계 TODO

| 순서 | 작업 |
|------|------|
| STEP 7 | `backend-logging-spec.md` — 요청 추적·마스킹·Swagger 비노출 필드 정합 |
| 구현 | springdoc 의존성, `OpenApiConfig`, 공통 스키마 클래스, 컨트롤러 적용 |

---

## 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 6 완료 기준 초안 |
