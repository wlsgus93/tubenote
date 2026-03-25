# STEP 18 계획 — 프론트 연동용 API 계약 안정화

## 1. 단계 목표

- 브라우저 프론트가 **Dashboard·Videos·상세**부터 실 API로 붙을 수 있도록 CORS·공통 응답·인증·DTO 일관성을 점검·보완한다.

## 2. 설계 대상

- CORS: 개발 서버 origin, `Authorization`, credentials, 노출 헤더.
- 성공 envelope: 선택 필드 `message` 추가(기존 API 호환).
- Dashboard/Video DTO: 프론트 매핑 혼선 완화(`progressSeconds` 별칭, 대시보드 카드 `durationSeconds`).
- 문서: `frontend-backend-contract.md`, `backend-api-spec.md` 보강, OpenAPI 설명.

## 3. 제외

- 신규 도메인·엔티티 추가.

## 4. 다음 단계

- 프론트 인터셉터·목업 제거 순서; 나머지 API 동일 패턴 점검.
