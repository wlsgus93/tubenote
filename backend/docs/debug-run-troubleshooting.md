# 디버깅 기록 — 로컬에서 Spring Boot 실행이 실패할 때

---

## 1. Logback `%wEx` (기동 초기 단계에서 실패)

### 증상

- `There is no conversion class registered for conversion word [wEx]`

### 원인·조치

- `logback-spring.xml` 패턴의 `%wEx` → `%ex` 로 변경.  
- 상세: 기존 `docs/debug-fix-logback-wex.md` 참고.

---

## 2. 포트 8080 이미 사용 중 (Tomcat 시작 직후 실패)

### 증상 (로그 핵심)

```
APPLICATION FAILED TO START

Description:
Web server failed to start. Port 8080 was already in use.

Action:
Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.
```

- 그 전 로그에는 `Tomcat initialized with port 8080`, `Starting ProtocolHandler ["http-nio-8080"]` 까지는 정상으로 보일 수 있음.
- **원인**: 이미 다른 프로세스가 **8080** 을 점유 중 (예: 이전에 띄운 `bootRun`, IntelliJ Run이 종료되지 않음, 다른 웹서버).

### 조치 (택 1)

**A. 8080 쓰는 프로세스 종료 (Windows)**

PowerShell 또는 CMD:

```powershell
netstat -ano | findstr :8080
```

`LISTENING` 행의 **마지막 숫자(PID)** 확인 후:

```powershell
taskkill /PID <PID> /F
```

**B. 애플리케이션 포트 변경**

`src/main/resources/application.yml`:

```yaml
server:
  port: 8081
```

또는 실행 시 한 번만:

```text
.\gradlew.bat bootRun --args='--server.port=8081'
```

### 확인

- 8080을 비우거나 다른 포트로 기동한 뒤 `Tomcat started on port ...` 로 끝나는지 확인.

| 항목 | 내용 |
|------|------|
| 관련 설정 | `server.port` (`application.yml`) |
| 기록 일자 | 2026-03-25 |
