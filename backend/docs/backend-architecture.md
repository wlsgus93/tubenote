# 백엔드 아키텍처 개요

> 프로젝트: 유튜브 학습 영상 관리 플랫폼 백엔드  
> 전제: Spring Boot, Spring Security + JWT, JPA, MySQL 또는 PostgreSQL, OpenAPI(Swagger)

## 1. 목적

- **유지보수·확장**에 유리한 경계를 유지한다.
- **사용자별 데이터**와 **공용/참조 데이터**의 책임을 도메인 패키지와 문서에서 일관되게 다룬다.
- **외부 연동(YouTube API 등)** 은 도메인 비즈니스와 분리한다.

## 2. 논리 레이어

```
┌─────────────────────────────────────────────────────────┐
│  Presentation (Web)                                     │
│  Controller: HTTP 매핑, 검증(형식), DTO 조립, Swagger    │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  Application / Domain Service                           │
│  유스케이스, 트랜잭션 경계, 도메인 규칙, 권한/소유권 검증  │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  Domain Model                                           │
│  Entity, Enum, Domain Event(개념), Repository 인터페이스 │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│  Infrastructure                                         │
│  JPA Repository 구현, YouTube 클라이언트, 메시징, Redis  │
└─────────────────────────────────────────────────────────┘
```

- **Controller**는 비즈니스 규칙을 최소화하고, 인증 주체 식별은 Security·서비스 계층으로 위임한다.
- **Service**가 트랜잭션과 도메인 불변식의 중심이 된다.
- **Repository**: Spring Data JPA 사용 시 인터페이스는 `domain` 또는 `infra.persistence`에 두는 방식 중 하나로 팀에서 통일한다. 권장: **영속성 전용 구현·쿼리는 infra 쪽 집중**, 도메인은 필요한 저장소 **추상화(포트)** 만 바라본다.

## 3. 모듈 경계: global / domain / infra

| 영역 | 책임 | 예시 |
|------|------|------|
| **global** | 앱 전역 설정, 공통 응답/예외, Security/JWT 필터 체인, 로깅·트레이싱, Swagger 설정, 유틸 | `GlobalExceptionHandler`, `JwtAuthenticationFilter`, OpenAPI 그룹 |
| **domain** | 비즈니스 도메인별 엔티티, 서비스, 도메인 예외, API용 DTO(또는 `api` 하위 패키지) | `user`, `video`, `collection` |
| **infra** | 외부 시스템·기술 세부사항 | YouTube `client`, `mapper`, `messaging`, `redis`, JPA 설정 |

**의존성 원칙(목표 방향)**

- `domain` → `infra`의 **구체 클래스**에 직접 의존하지 않는다.
- 외부 API·브로커는 **인터페이스 + infra 구현체**로 주입 가능하게 설계한다(테스트 시 Mock 용이).

## 4. 횡단 관심사와 초기 전제

다음 항목은 **초기부터** 아키텍처에 포함하며, 상세 규칙은 각 spec·`backend-conventions.md`에서 다룬다.

| 관심사 | 위치(개략) | 비고 |
|--------|------------|------|
| **JWT 인증/인가** | `global.security`, `global.auth` | Access/Refresh, 공개·보호 API 구분, 인증 vs 인가 실패 응답 분리 |
| **OpenAPI** | `global.swagger`, Controller/DTO 어노테이션 | 모든 API summary/description, 보안 스키마 |
| **로깅** | `global.logging`, 필터 또는 MDC | requestId/traceId, 민감정보 마스킹 |
| **비동기/이벤트** | `infra.messaging`, `domain` 이벤트 발행 | 브로커 미도입 시에도 이벤트 경계·멱등성을 문서로 먼저 고정 |

## 5. 패키지 구조 초안

루트 패키지: **`com.myapp.learningtube`**

```
com.myapp.learningtube
├── LearningTubeApplication.java
├── global
│   ├── config          # Spring 설정 빈
│   ├── auth            # JWT 발급·검증 보조, Principal 어댑터
│   ├── security        # SecurityFilterChain, 접근 정책
│   ├── error           # 공통 예외 타입, 에러 코드
│   ├── response        # 공통 API 응답 래퍼
│   ├── logging         # 로깅 필터, MDC, 마스킹
│   ├── swagger         # OpenAPI 그룹, 스키마 커스터마이징
│   └── util
├── domain
│   ├── user
│   ├── auth            # 로그인·회원 도메인(글로벌 auth와 이름 충돌 시 api/auth 등으로 조정 가능)
│   ├── youtube
│   ├── channel
│   ├── video
│   ├── collection
│   ├── note
│   ├── highlight
│   ├── transcript
│   ├── queue
│   ├── analytics
│   └── sync
└── infra
    ├── youtube
    │   ├── client
    │   ├── dto         # 외부 API 전용 DTO
    │   └── mapper
    ├── persistence     # JPA 설정, QueryDSL 등
    ├── messaging
    └── redis
```

**조정 가이드**

- `domain.auth`와 `global.auth`가 혼란스러우면 `domain.member` / `domain.session` 등으로 rename 검토.
- 도메인별로 `controller`, `service`, `repository`, `dto` 하위 패키지를 두어 **같은 도메인 내 응집**을 유지한다.

## 6. 요청 처리 흐름(개념)

1. HTTP 요청 → **로깅 필터**: requestId/traceId 설정.  
2. **Spring Security**: 공개 경로 여부, JWT 검증, `Authentication` 설정.  
3. **Controller**: 입력 DTO 검증, 서비스 호출.  
4. **Service**: 비즈니스 규칙, 트랜잭션, Repository·포트 호출.  
5. **infra**: DB 저장, YouTube 호출, (선택) 메시지 발행.  
6. **공통 응답 형식**으로 직렬화; 예외는 **GlobalExceptionHandler**에서 코드·메시지 통일.

## 7. 문서 매핑

| 문서 | 역할 |
|------|------|
| `backend-conventions.md` | 일상 개발 규칙(네이밍, DTO, 예외, Swagger·로깅·JWT 요약) |
| `backend-db-spec.md` / `backend-entities.md` | DB·엔티티 상세 |
| `backend-api-spec.md` | API 목록·DTO·에러 |
| `backend-auth-spec.md` | JWT·필터 체인·토큰 정책 상세 |
| `backend-swagger-spec.md` | 태그·예시·에러 문서화 규칙 상세 |
| `backend-logging-spec.md` | 레벨·마스킹·비동기 로그 상세 |
| `backend-async-spec.md` | 이벤트·재시도·멱등성 상세 |

## 8. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 1 초안 작성 |
