# TODO

RailOps 프로젝트 진행 상태를 추적하는 문서입니다. Codex와 후속 에이전트는 작업 전후로 이 문서를 확인하고 필요한 경우 갱신합니다.

## 현재 완료된 큰 범위

- [x] 현재 진행상황 요약 문서 작성: `docs/34-current-progress-summary.md`

- [x] 프로젝트 루트와 기본 문서 구조 생성
- [x] `AGENTS.md`, `TODO.md`, 설계 문서 작성
- [x] Spring Boot 백엔드 기본 구조 생성
- [x] 공통 API 응답, 공통 에러 코드, 비즈니스 예외, 전역 예외 처리 구현
- [x] Spring Security + JWT 인증 기본 흐름 구현
- [x] User 회원가입/로그인/로그아웃/내 정보 API 구현
- [x] Station 공개 조회와 관리자 CRUD API 구현
- [x] Route 관리자 CRUD API 구현
- [x] Train 관리자 CRUD API 구현
- [x] Car/Seat 물리 편성 관리자 CRUD API 구현
- [x] TrainSchedule 관리자 CRUD/상태 변경 API 구현
- [x] TrainSchedule 사용자 검색/상세 조회 API 구현
- [x] ScheduleSeat 도메인 구현
- [x] 운행편 생성 시 물리 Seat를 ScheduleSeat로 자동 생성
- [x] 운행편 좌석 맵 조회 API 구현
- [x] 관리자 ScheduleSeat BLOCK/UNBLOCK API 구현
- [x] Reservation/ReservationSeat 도메인 구현
- [x] 좌석 HOLD API 구현
- [x] HOLD 만료 Spring Scheduler 구현
- [x] Payment 도메인 구현
- [x] HOLD 생성 시 Payment READY 생성
- [x] Payment 성공/실패/취소 시뮬레이션 API 구현
- [x] 결제 성공 시 Reservation CONFIRMED, ScheduleSeat RESERVED 확정 구현
- [x] 결제 실패/취소 시 좌석 AVAILABLE 해제 구현
- [x] 사용자/관리자 React 기본 UI 구현
- [x] 현재 백엔드 기능용 프론트 API client 구현
- [x] 사용자 운행편 검색/상세/좌석 맵/HOLD/결제 시뮬레이션 화면 구현
- [x] 관리자 역/노선/열차/객차/좌석/운행편 관리 화면 구현
- [x] 관리자 운행편 좌석 BLOCK/UNBLOCK 화면 구현
- [x] 로컬 PostgreSQL 연결 설정 파일 추가
- [x] Docker Compose PostgreSQL 설정 파일 추가
- [x] 백엔드 테스트 통과
- [x] 프론트엔드 빌드 통과

## 다음 작업 후보

1. 가격 정책은 1차 단일 기본요금으로 유지하고, 구현 시 `docs/33-fare-policy-notes.md`를 기준으로 amount=0 임시값을 교체한다.
2. Reservation 목록/상세 API를 구현한다.
3. Reservation 취소 API를 구현한다.
4. 결제 완료 후 예매 취소 정책을 구현한다.
5. 사용자 마이페이지에 내 예매 목록/상세/취소 화면을 연결한다.
6. 관리자 전체 예매 조회 화면을 구현한다.
7. DB 연결 재개 시 PostgreSQL 계정/DB 생성과 마이그레이션 방식을 확정한다.
8. Docker Desktop 설치 후 컨테이너 실행과 Testcontainers 통합 테스트를 다시 점검한다.

## 보류 중인 결정

- 가격 정책: 1차는 거리/노선/좌석 타입 무관 단일 기본요금으로 결정. 실제 구현 시 `docs/33-fare-policy-notes.md` 기준으로 반영
- 결제 완료 후 예매 취소: REFUNDED 상태를 추가할지, 1차에서는 단순 취소로 둘지 결정 필요
- JWT 세부 정책: refresh token을 1차에 포함할지 후순위로 둘지 결정 필요
- 관리자 계정 생성 방식: seed data, 별도 admin signup, DB 직접 생성 중 선택 필요
- Station 삭제 정책: Route가 참조 중인 Station 삭제를 막을지 결정 필요
- Route 삭제 정책: TrainSchedule이 참조 중인 Route 삭제를 막을지 결정 필요
- HOLD 만료 처리: DB/테이블 구조 확정 후 Scheduler 쿼리와 배치 크기 재검토 필요
- Java 21 설치: 프로젝트는 Java 21 기준이므로 로컬 JDK 21 설치 권장