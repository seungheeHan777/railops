# TODO

RailOps 프로젝트 진행 상태를 추적하는 문서입니다. Codex와 후속 에이전트는 작업 전후로 이 문서를 확인하고 필요한 경우 갱신합니다.

## 현재 완료된 큰 범위

- [x] 현재 진행상황 요약 문서 작성: `docs/34-current-progress-summary.md`
- [x] 단일 기본요금 설정 및 예약 당시 가격 스냅샷 구현
- [x] 본인 예매 목록/상세 API와 마이페이지 구현
- [x] 출발 전 예매 취소/전액 가상 환불 및 중복 요청 보호 구현
- [x] 관리자 예매 목록/상세 API와 화면 구현
- [x] PostgreSQL 격리 스키마 동시성/HTTP 통합 테스트 구현 및 실행
- [x] 데스크톱/모바일 브라우저에서 검색/HOLD/결제/환불/관리자 조회 검증
- [x] 로컬 DB 환불 컬럼/상태 제약조건 변경 및 DEMO 데이터 추가

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

## 현재 작업 범위

역/노선/열차/객차/좌석/운행편은 기존 관리자 CRUD로 직접 등록한다. 테스트용 운행편과 좌석을 준비해 서비스의 예매/결제/취소 흐름을 먼저 완성한다.

## 다음 작업 순서

1. 예매가 존재하는 운행편/기준 데이터 수정·삭제 제한과 운행 취소 시 일괄 처리 정책을 정한다.
2. 사용자 정보 수정/비밀번호 변경/회원 탈퇴와 관리자 계정 생성 방식을 정리한다.
3. 운영 로그 저장/조회와 인증 실패 응답을 보완한다.
4. Flyway 마이그레이션으로 스키마 변경을 관리한다. 현재 환불 SQL은 명시적 수동 적용 방식이다.
5. Docker Compose 앱 전체 실행, 배포/모니터링/부하 테스트를 단계적으로 진행한다.

## 보류 중인 결정

- 가격 정책: 개발 기본값 50,000원 구현 완료. 실제 서비스 금액/가격 테이블은 후속 확정
- 취소 정책: 출발 전 전액 가상 환불 구현 완료. 부분 취소/수수료/운행 취소 시 환불은 후속 확정
- JWT 세부 정책: refresh token을 1차에 포함할지 후순위로 둘지 결정 필요
- 관리자 계정 생성 방식: seed data, 별도 admin signup, DB 직접 생성 중 선택 필요
- Station 삭제 정책: Route가 참조 중인 Station 삭제를 막을지 결정 필요
- Route 삭제 정책: TrainSchedule이 참조 중인 Route 삭제를 막을지 결정 필요
- HOLD 만료 처리: DB/테이블 구조 확정 후 Scheduler 쿼리와 배치 크기 재검토 필요
- Java 21은 `C:\Users\User\.jdks\temurin-21.0.9`에 설치되어 Gradle toolchain이 사용 중
