# STEP 6 계획 — Swagger/OpenAPI 문서화 구조 고정

## 1. 단계 목표

- **OpenAPI 3** 기준으로 Swagger UI 문서화 **구조·규칙**을 문서로 고정한다.
- **태그·Controller·DTO·enum·인증·에러·예시값**에 대한 **일관된 작성 기준**을 정의해, 이후 모든 API를 “처음부터 문서화 가능한” 상태로 만든다.
- STEP 4(`backend-api-spec.md`)·STEP 5(`backend-auth-spec.md`)와 **모순 없이** 맞춘다.

## 2. 이번 단계에서 해결할 문제

- 태그·요약 문구가 컨트롤러마다 제각각인 문제.
- 성공 응답만 있고 **401/403/404** 등이 빠지는 문제.
- DTO 필드 **`@Schema` 누락**으로 생성 클라이언트 품질이 떨어지는 문제.
- enum·에러 코드가 문서에 값 목록으로 남지 않는 문제.
- 예시값에 **민감·실데이터**가 들어가는 문제.

## 3. 설계 대상

| 대상 | 설명 |
|------|------|
| 도구 | Spring Boot 3 + **springdoc-openapi** 가정(OpenAPI 3.0 YAML/JSON) |
| 태그 | 도메인·경계 기준 분류 및 표기 규칙 |
| Controller | `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`, `@SecurityRequirement` |
| DTO | Request/Response 필드 단위 `@Schema`, required, example, nullable |
| enum | 스키마 노출·설명·예시 |
| 인증 | `bearerAuth`, 공개/보호 API 문서 차이 |
| 에러 | `ApiErrorResponse` 재사용, 코드별 예시 |
| 예시 | 데이터·다국어·PII 금지 |

## 4. 주요 결정 사항

- OpenAPI 문서의 **단일 진실**은 코드의 어노테이션 + (선택) `OpenApiCustomizer`이며, **규칙의 단일 진실**은 **`docs/backend-swagger-spec.md`**.
- **보호 API**는 **401·403** 을 필수로 문서화; **단건 리소스**는 **404**, **생성/변경**은 **400·409**를 유스케이스에 맞게 문서화.
- **성공 envelope**(`success`, `requestId`, `data`)는 **전역 스키마 또는 커스텀 SpringDoc 래퍼**로 표현하고, 구체 `data` 타입은 **도메인 DTO**로 연결(구현 단계에서 `GroupedOpenApi`·`Schema` 등록 병행 가능).
- **Entity**는 OpenAPI에 **등록하지 않음**; **Request/Response DTO만** 스키마화.

## 5. 생성/수정 예정 파일

| 파일 | 용도 |
|------|------|
| `docs/backend-step6-plan.md` | 본 문서 |
| `docs/backend-swagger-spec.md` | Swagger/OpenAPI 상세 규칙 |
| `docs/backend-step6-result.md` | STEP 6 산출 요약 |
| `docs/backend-conventions.md` | §4와 `backend-swagger-spec.md` 연결 문구 보강(선택) |

## 6. 구현 범위

- 위 문서 작성. **의존성 추가·Java Config 코드는 작성하지 않음**(규칙만 고정).

## 7. 제외 범위

- 실제 `pom.xml`/`build.gradle` springdoc 버전 고정.
- 각 엔드포인트별 완전한 request/response 예시 JSON 전수(STEP 9·구현 단계에서 보강).

## 8. 다음 단계 연결 포인트

- **STEP 7** 로깅: Swagger에 노출하지 않는 내부 필드와 로그 마스킹 정합.
- **구현**: `OpenApiConfig` 빈, `ApiErrorResponse` 스키마 클래스, 컨트롤러 어노테이션 일괄 적용.
