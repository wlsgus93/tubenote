# 디버깅 노트 — OAuth2 Client id must not be empty

## 증상

애플리케이션 부팅 중 아래 예외로 컨텍스트 초기화가 실패함.

- `Client id must not be empty.`
- 발생 위치: `OAuth2ClientRegistrationRepositoryConfiguration` → `OAuth2ClientProperties.validateRegistration`

## 원인

`spring.security.oauth2.client.registration.google.client-id`가 빈 값일 때,
Spring Boot OAuth2 Client 자동설정이 `ClientRegistrationRepository` 빈 생성에 실패하며 애플리케이션이 종료된다.

현재 설정은 아래처럼 환경변수 기본값이 비어 있었고( `:}` ),
로컬 실행에서 `GOOGLE_CLIENT_ID`를 주입하지 않아 빈 값이 되었다.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:}
```

## 해결

로컬 실행 편의를 위해 `client-id`는 기본값을 제공하고,
운영/배포 환경에서는 환경변수로 덮어쓰도록 변경한다.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:101542779091-hfde6c8d1d88ehlshnjels7rbpfa8se6.apps.googleusercontent.com}
            client-secret: ${GOOGLE_CLIENT_SECRET:}
```

> 주의: `client-secret`은 민감정보이므로 파일에 원문 저장하지 않고 환경변수로만 주입한다.

## 재발 방지 체크리스트

- 로컬/CI 실행 시 `GOOGLE_CLIENT_ID` 환경변수 주입 여부 확인
- 운영 배포에서는 반드시 `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`을 시크릿/환경변수로 주입

