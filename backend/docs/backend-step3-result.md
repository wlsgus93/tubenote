# STEP 3 결과 — DB 명세·제약·인덱스 고정

## 1. 구현 완료 항목

- **PostgreSQL 15+** 기준 **실행 가능한 `CREATE TABLE`** 및 **PK / FK / UNIQUE / CHECK / INDEX** 를 `backend-db-spec.md`에 정의함.
- 테이블 **13개**(`users` ~ `sync_jobs`)에 대해 **ON DELETE** 정책(CASCADE / RESTRICT / SET NULL)을 표로 고정함.
- **조회 패턴 ↔ 인덱스** 매핑(§8) 및 **정규화·의도적 비정규화**(§2, §9) 정리함.
- **공용 vs 사용자별** 테이블 분류와 FK 원칙(사용자가 공용 테이블을 직접 참조하지 않음)을 DB 명세에 명시함.
- **감사 필드**(`created_at`, `updated_at`) 및 **soft delete**(`users`, `collections`, `notes.deleted_at`) 정책을 표로 정리함.
- **MySQL 8.0**과의 차이(특히 **활성 이메일 부분 유니크**)를 §11에 요약함.
- 마이그레이션·JPA 가이드(§10)를 요약 수준으로 포함함.

## 2. 생성/수정한 파일 목록

| 파일 | 비고 |
|------|------|
| `docs/backend-step3-plan.md` | 신규 |
| `docs/backend-db-spec.md` | 신규 |
| `docs/backend-step3-result.md` | 신규 |
| `docs/backend-entities.md` | `UserVideoProgress`에 `created_at` 필드 반영(STEP 3 DB와 정합) |

## 3. 핵심 클래스/구조 설명

- 코드 산출 없음. **단일 기준**: `docs/backend-db-spec.md`.
- **이메일 유일성(PostgreSQL)**: `uk_users_email_active` — `WHERE deleted_at IS NULL` 부분 유니크 인덱스.
- **공용 영상 보존**: `collection_videos`, `notes` 등 → `videos.id` 참조는 **ON DELETE RESTRICT**.

## 4. 반영된 설계 원칙

- STEP 2 엔티티와 컬럼 정합; `user_video_progress`에 **`created_at`** 을 DB 감사 일관성을 위해 추가하고 `backend-entities.md` §3.12에 동기화함.
- `videos.availability`에 `UNKNOWN`, `sync_jobs.status`에 `CANCELLED` 를 CHECK 제약으로 확장해 운영 예외 표현 가능.

## 5. Swagger 반영 내용

- 해당 없음(스키마는 DTO). DB CHECK 값(enum 성격) 변경 시 **API·Swagger enum** 과 불일치하지 않도록 STEP 6·`backend-api-spec.md`에서 맞출 것.

## 6. 로깅 반영 내용

- 해당 없음. `sync_jobs.payload`(JSONB)에 PII·토큰 저장 금지는 STEP 2와 동일 전제.

## 7. 아쉬운 점 / 개선 포인트

- MySQL 채택 시 **이메일 부분 유니크** 대체 패턴을 하나로 확정하고 DDL 예시를 `backend-db-spec.md` §11에 **구체 스크립트**로 보강하는 것이 좋음.
- `notes.deleted_at` 사용 여부가 MVP에서 불명확하면 애플리케이션 기본 필터 정책을 STEP 4와 함께 확정할 것.
- 대용량 `transcripts.content` 검색·페이징은 API·인프라(§9) 후속 과제.

## 8. 다음 단계 TODO

| 순서 | 작업 |
|------|------|
| STEP 4 | 공통 응답·예외·에러 코드; 유니크 위반·FK 위반·soft delete 필터와의 매핑 |
| 구현 | Flyway/Liquibase 스크립트 생성(PostgreSQL 기준), 로컬·CI DB에 적용 검증 |
| 동기화 | JPA 엔티티 구현 시 `backend-db-spec.md` CHECK·ENUM 문자열과 필드 매핑 일치 검증 |

---

## 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 3 완료 기준 초안 |
