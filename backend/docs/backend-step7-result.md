# STEP 7 결과 — 로깅 전략 고정

## 1. 구현 완료 항목

- 로그 **목적·레벨(ERROR/WARN/INFO/DEBUG)** 기준을 `backend-logging-spec.md`에 정의함.
- **Controller(요청 경계)**, **Service**, **외부 API**, **예외**, **인증/인가** 별 로그 정책을 표로 정리함.
- **민감정보 금지·마스킹** 규칙(토큰, Authorization, 자막 전문, 이메일 등)을 명시함.
- **`requestId` / `traceId`** 생성·전파·MDC·응답 연계를 정의함.
- **비동기**(`@Async`, `sync_jobs`) MDC 전파·`jobId`/`syncJobId`·TaskDecorator 규칙을 정의함.
- **구조화 로그(JSON)** 권장 필드와 구현 체크리스트를 포함함.
- **Logback XML/필터 Java 코드 미작성**(문서만).

## 2. 생성/수정한 파일 목록

| 파일 | 비고 |
|------|------|
| `docs/backend-step7-plan.md` | 신규 |
| `docs/backend-logging-spec.md` | 신규 |
| `docs/backend-step7-result.md` | 신규 |
| `docs/backend-conventions.md` | §5 상세 기준을 `backend-logging-spec.md`로 연결, 개정 0.4 |

## 3. 핵심 클래스/구조 설명

- 구현 시 권장: `RequestLoggingFilter`(또는 Spring `CommonsRequestLoggingFilter` 커스터마이징), `MdcTaskDecorator`, YouTube `ClientHttpRequestInterceptor`, 글로벌 예외 핸들러와 중복 없는 ERROR 스택.

## 4. 반영된 설계 원칙

- API `requestId`와 로그 **동일 상관 ID**.
- 본문·토큰 **기본 비로깅**; DEBUG도 길이·마스킹 제한.

## 5. Swagger 반영 내용

- 해당 없음(로그는 API 스키마 비노출). 예시값에 실토큰 금지는 `backend-swagger-spec.md`와 정합.

## 6. 로깅 반영 내용

- 본 STEP 문서가 로깅 **단일 상세 기준**이 됨.

## 7. 아쉬운 점 / 개선 포인트

- OpenTelemetry 통합 시 `traceId`·span id 필드 추가 및 샘플링 정책 별도 문서 권장.
- 로그 저장소·보존 기간은 인프라/컴플라이언스 문서로 분리.

## 8. 다음 단계 TODO

| 순서 | 작업 |
|------|------|
| STEP 8 | `backend-async-spec.md` — 재시도·멱등성·로그 키 정합 |
| 구현 | 필터, MDC, 인터셉터, JSON appender, 프로파일별 레벨 |

---

## 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 7 완료 기준 초안 |
