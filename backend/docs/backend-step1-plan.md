# STEP 1 계획 — 백엔드 아키텍처·공통 규칙 고정

## 1. 단계 목표

- 유튜브 학습 영상 관리 플랫폼 백엔드의 **아키텍처 경계(global / domain / infra)** 와 **공통 개발 원칙**을 문서로 고정한다.
- 이후 단계(DB, JWT, Swagger, 로깅, 비동기)가 동일한 전제 위에서 설계·구현되도록 **기준선(baseline)** 을 만든다.
- **CRUD·엔티티 구현 코드는 작성하지 않는다.** 설계 문서만 산출한다.

## 2. 이번 단계에서 해결할 문제

- 레이어링·패키지 배치·의존성 방향이 팀 합의 없이 흔들리는 문제를 예방한다.
- DTO/Entity 분리, 예외·응답, Swagger·로깅·JWT를 **후순위가 아닌 전제 조건**으로 명문화한다.
- 문서 간 역할을 구분한다: `backend-architecture.md`(구조·흐름), `backend-conventions.md`(일상 코딩 규칙).

## 3. 설계 대상

| 대상 | 설명 |
|------|------|
| 논리 아키텍처 | Controller–Service–Repository, global/domain/infra 경계 |
| 패키지 구조 초안 | 도메인별 패키지, 인프라(YouTube 클라이언트 등) 위치 |
| 공통 규칙 | 네이밍, DTO/Entity, 예외, Swagger, 로깅, 인증/인가 기본 원칙 |
| 문서 체계 | STEP N plan/result, 추후 spec 문서와의 연결 |

## 4. 주요 결정 사항

- **스택 전제**: Java, Spring Boot, Spring Security + JWT, JPA/Hibernate, DB는 MySQL 또는 PostgreSQL(구체 제품은 STEP 2~3에서 확정), OpenAPI(Swagger UI) 필수.
- **루트 패키지**: `com.myapp.learningtube` (필요 시 조직 도메인으로 변경 가능하나, 문서·코드는 한 베이스로 통일).
- **의존성 방향**: `domain` → `infra`의 구체 구현에 직접 의존하지 않도록, **포트(인터페이스)는 domain 또는 global 계약**, **어댑터 구현은 infra** 로 정리하는 방향을 원칙으로 한다(세부는 이후 단계에서 인터페이스로 구체화).
- **문서 우선**: API/엔티티/인증 상세는 각각 `backend-api-spec.md`, `backend-entities.md`, `backend-auth-spec.md` 등에서 다루며, STEP 1에서는 **원칙과 경계만** 고정한다.

## 5. 생성/수정 예정 파일

| 파일 | 용도 |
|------|------|
| `docs/backend-step1-plan.md` | 본 문서(계획) |
| `docs/backend-architecture.md` | 아키텍처·레이어·패키지·횡단 관심사 개요 |
| `docs/backend-conventions.md` | 네이밍, DTO/Entity, 예외, Swagger, 로깅, 인증/인가 기본 규칙 |
| `docs/backend-step1-result.md` | STEP 1 산출 요약 및 다음 단계 연결 |

## 6. 구현 범위

- 위 문서 4종 작성.
- 패키지 트리 **초안**을 `backend-architecture.md`에 반영.
- Swagger·로깅·JWT를 **“나중에”가 아니라 초기 전제**로 conventions에 포함.

## 7. 제외 범위

- Spring Boot 프로젝트 생성, `build.gradle`/`pom.xml`, 실제 소스 코드.
- DB 테이블·인덱스 상세, API 목록, JWT 클레임 상세, Swagger 설정 클래스 구현.
- `backend-db-spec.md`, `backend-auth-spec.md` 등 **별도 spec 문서의 본문 작성**(STEP 2 이후 단계에서 작성·갱신).

## 8. 다음 단계 연결 포인트

- **STEP 2**: 엔티티 후보·ER 수준 관계를 `backend-entities.md` 초안과 연계.
- **STEP 4~8**: 공통 응답/예외, JWT, Swagger, 로깅, 비동기 spec 문서가 본 STEP의 architecture·conventions와 **모순 없이** 이어지도록 검토.
- 패키지 베이스명 변경 시 `backend-architecture.md`·`backend-conventions.md`를 동시에 갱신할 것.
