# 디버깅 기록 — Logback `%wEx` 기동 실패

> 포트 충돌 등 다른 실행 오류는 `debug-run-troubleshooting.md` 를 참고한다.

## 증상

- IntelliJ **Run** 또는 `gradlew bootRun` 시 프로세스가 **exit code 1** 로 종료.
- Gradle 출력에는 원인이 짧게만 보이고, 스택트레이스를 보면 다음과 유사한 메시지가 나온다.

```
Logback configuration error detected:
There is no conversion class registered for conversion word [wEx]
[wEx] is not a valid conversion word
```

## 원인

- `src/main/resources/logback-spring.xml` 의 콘솔 패턴 끝에 **`%wEx`** 가 사용되어 있었다.
- `%wEx`는 **Spring Boot가 기본 Logback 설정을 로드할 때 등록하는 확장 변환자**이다.
- **커스텀 `logback-spring.xml`만** 두고 `defaults.xml` 등을 포함하지 않으면, 순수 Logback 입장에서는 `%wEx`를 알 수 없어 **설정 파싱 단계에서 애플리케이션이 시작되지 않는다.**

## 조치

- 해당 패턴의 `%wEx`를 **표준 Logback 변환자 `%ex`** 로 교체했다.
- 예: `%msg%n%wEx` → `%msg%n%ex`
- 파일: `src/main/resources/logback-spring.xml`

## 확인

- `.\gradlew.bat bootRun` 실행 시 Spring Boot가 정상 기동되는지 확인.

## 참고 (대안)

- `%wEx`를 유지하고 싶다면 `logback-spring.xml` 상단에 다음을 포함해 Spring Boot 기본 변환자를 등록할 수 있다.

```xml
<include resource="org/springframework/boot/logging/logback/defaults.xml"/>
```

---

| 항목 | 내용 |
|------|------|
| 발생 환경 | Windows, IntelliJ, Gradle, Spring Boot 3.2.x |
| 관련 파일 | `logback-spring.xml` |
| 수정 일자 | 2026-03-25 |
