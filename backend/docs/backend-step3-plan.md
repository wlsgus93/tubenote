# STEP 3 계획 — DB 테이블·제약·인덱스·정규화 고정

## 1. 단계 목표

- `backend-entities.md`(STEP 2)를 **실제 구현 가능한 수준의 DB 명세**로 옮긴다.
- 테이블별 **PK / FK / UNIQUE / INDEX** 를 이름 규칙과 함께 정의한다.
- **자주 조회되는 조건**을 기준으로 보조 인덱스·부분 인덱스(가능한 벤더)를 확정한다.
- **공용 vs 사용자별** 테이블 경계, **정규화/비정규화**, **감사·soft delete** 정책을 DB 관점에서 문서화한다.

## 2. 이번 단계에서 해결할 문제

- ERD만으로 남는 **FK 방향·ON DELETE·NULL 허용** 모호성을 제거한다.
- `User.email` + `deleted_at`(soft delete)처럼 **논리 유일 제약**을 DB에서 어떻게 강제할지 벤더별로 정한다.
- 목록·정렬·소유권 검증 쿼리에 맞는 **인덱스 누락/중복**을 예방한다.

## 3. 설계 대상

| 대상 | 설명 |
|------|------|
| 물리 명명 | 테이블·컬럼 snake_case, 제약·인덱스 접두사(`pk_`, `fk_`, `uk_`, `idx_`) |
| DDL 기준 | **PostgreSQL 15+** 를 1차 기준 DDL로 두고, **MySQL 8.0+** 차이는 `backend-db-spec.md`에 별도 절로 정리 |
| 13개 테이블 | users, refresh_tokens, channels, user_channels, videos, collections, collection_videos, notes, highlights, transcripts, watch_queue_items, user_video_progress, sync_jobs |
| 성능 | 조회 패턴 ↔ 인덱스 매핑, 선택적 EXPLAIN 검증은 구현 단계 가이드로 언급 |

## 4. 주요 결정 사항

- **기준 DB**: PostgreSQL 15+ (JSON `JSONB`, **부분 유니크 인덱스**로 활성 이메일 유일성 표현).
- **보조 타겟**: MySQL 8.0 — 부분 유니크 미지원 구간은 **대체 패턴**(복합 유니크·애플리케이션 검증)을 명세에 명시.
- **PK**: `BIGSERIAL`/`BIGINT AUTO_INCREMENT` 등 **단조 증가 정수**; 분산 UUID 전략은 현 단계에서 채택하지 않음.
- **타임스탬프**: `TIMESTAMPTZ`(PostgreSQL) / `DATETIME(6)`(MySQL) **UTC 저장**.
- **ON DELETE**: 사용자 **물리 삭제**는 운영상 제한적이나, 명세상 **사용자 소유 테이블은 `user_id` FK에 CASCADE** 로 정의해 데이터 고아 방지. **soft delete**는 애플리케이션 필터가 주력.
- **공용 테이블**(`channels`, `videos`, `transcripts`)은 사용자 테이블과 FK로 직접 연결하지 않고 **연결 테이블**로만 연결(STEP 2와 동일).

## 5. 생성/수정 예정 파일

| 파일 | 용도 |
|------|------|
| `docs/backend-step3-plan.md` | 본 문서 |
| `docs/backend-db-spec.md` | DDL, 제약, 인덱스, 벤더 차이, 정규화·감사·soft delete 정책 |
| `docs/backend-step3-result.md` | STEP 3 산출 요약 및 다음 단계 연결 |

## 6. 구현 범위

- 위 문서 3종 작성.
- `backend-db-spec.md`에 **테이블별 정의서 표** + **PostgreSQL CREATE TABLE** + **인덱스·제약 요약**.

## 7. 제외 범위

- Flyway/Liquibase 마이그레이션 스크립트 파일·JPA `@Entity` 매핑 코드.
- 파티셔닝·리드레플리카·샤딩 등 운영 토폴로지(필요 시 추후 별도 문서).
- `UserYoutubeCredential` 등 STEP 2 후속 후보 테이블(미도입).

## 8. 다음 단계 연결 포인트

- **STEP 4**: 공통 예외·에러 코드와 DB 제약 위반(유니크 충돌 등) 매핑.
- **STEP 5**: `refresh_tokens`, `users`와 JWT 로테이션·로그아웃 정책 정합.
- **구현 시**: JPA에서 `@SQLDelete` / `@Where` 사용 여부와 **부분 유니크**가 있는 `users.email` 검증 쿼리 일치 여부 점검.
