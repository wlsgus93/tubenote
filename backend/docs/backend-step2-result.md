# STEP 2 결과 — 엔티티 후보·관계·명세 고정

## 1. 구현 완료 항목

- **엔티티 후보 13개**를 정의하고 `User` / `RefreshToken` / `Channel` / `UserChannel` / `Video` / `Collection` / `CollectionVideo` / `Note` / `Highlight` / `Transcript` / `WatchQueueItem` / `UserVideoProgress` / `SyncJob` 로 목록을 고정함.
- **USER_SCOPED**, **SHARED_REFERENCE**, **SECURITY** 구분으로 사용자별 데이터와 공용 캐시 책임을 문서화함.
- 각 엔티티에 대해 **필드·타입·nullable·기본값·설명·연관관계·인덱스·unique·생성/수정·soft delete·API(Swagger) 가이드·로그 마스킹**을 `backend-entities.md`에 정리함.
- **연관관계 초안**을 Mermaid ER로 `backend-step2-plan.md` 부록에 포함함.
- **조회 패턴 → 인덱스 후보**를 plan 부록 표로 정리함.
- **코드(JPA/DDL) 미작성** — STEP 2 범위 준수.

## 2. 생성/수정한 파일 목록

| 파일 | 비고 |
|------|------|
| `docs/backend-step2-plan.md` | 신규 |
| `docs/backend-entities.md` | 신규 |
| `docs/backend-step2-result.md` | 신규 |

## 3. 핵심 클래스/구조 설명

- 본 단계는 **코드 산출 없음**. 구조의 단일 기준 문서는 **`docs/backend-entities.md`** 이다.
- **공용 캐시**: `Channel`, `Video`, `Transcript` — YouTube ID 유니크 기준 1행(자막은 `(video_id, language_code)` 유니크).
- **사용자 연결**: `UserChannel`, `Collection`, `CollectionVideo`, `Note`, `Highlight`, `WatchQueueItem`, `UserVideoProgress`, `SyncJob` — 소유권은 `user_id` 또는 상위 `Collection.user_id`로 검증.

## 4. 반영된 설계 원칙

- Entity를 API에 직접 노출하지 않음 — 명세에 **DTO 기준 노출/비노출**을 명시.
- 외부 ID(`youtube_channel_id`, `youtube_video_id`)와 내부 PK(`id`) 역할 분리.
- 조회 빈도가 높은 경로에 **복합 인덱스·유니크** 후보를 엔티티별로 기재.

## 5. Swagger 반영 내용

- 구현 코드 없음. **정책**: 모든 스키마는 **Response/Request DTO**에 `@Schema` 적용; Entity는 OpenAPI에 등록하지 않음.
- `backend-entities.md` §4 매트릭스로 엔티티별 DTO 설계 시 주의점(특히 `Transcript.content`, `Note.body`)을 고정.

## 6. 로깅 반영 내용

- 구현 코드 없음. **정책**: `password_hash`, `RefreshToken.token_hash`, `Transcript.content`는 로그 **금지**; `User.email`, `Note`/`Highlight` 텍스트는 **마스킹 또는 레벨 제한**; `SyncJob.payload`는 PII·토큰 저장 금지.

## 7. 아쉬운 점 / 개선 포인트

- `User.email` UNIQUE와 **soft delete** 동시 적용 시 DB별 **partial unique** 문법이 달라 STEP 3에서 구체화 필요.
- YouTube Data API **OAuth 토큰 저장**이 필요해지면 `UserYoutubeCredential` 등 별도 엔티티와 암호화·회전 정책이 필요함(현재 후보만 언급).
- `Video.title` 등 긴 텍스트 **검색**은 DB LIKE 한계가 있어, 이후 전문 검색·별도 인덱스 여부를 STEP 10 전후에 검토.

## 8. 다음 단계 TODO

| 순서 | 작업 |
|------|------|
| STEP 3 | `docs/backend-db-spec.md` — 테이블명, DDL, FK ON DELETE 정책, 인덱스 이름, VARCHAR 길이·JSON 타입·enum 저장 방식 확정 |
| STEP 4 | 공통 응답·에러 코드와 soft delete 필터(예: User/Collection) 정책 정합 |
| 병행 | `backend-api-spec.md` 초안에 리소스별 CRUD·소유권 검증 포인트 반영 |

---

## 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 2 완료 기준 초안 |
