# RailOps

RailOps는 좌석 선택 기반 철도 예매 시스템을 Spring Boot 중심으로 구현하는 백엔드/운영형 포트폴리오 프로젝트입니다. 단순한 CRUD 예매 서비스가 아니라, 좌석 임시 점유, 가상 결제, 중복 예매 방지, 로그, 모니터링, Docker 기반 배포까지 포함해 운영 가능한 시스템을 학습하고 설명할 수 있도록 설계합니다.


## 이름의 의미

RailOps는 `Rail`과 `Operations`를 합친 이름입니다. 철도 예매 도메인을 다루면서도 단순 기능 구현에 그치지 않고 배포, 로그, 모니터링, 장애 대응까지 포함하는 운영형 백엔드 프로젝트라는 의미를 담고 있습니다.

## 에이전트 작업 문서

Codex와 후속 에이전트는 작업 전에 루트의 `AGENTS.md`와 `TODO.md`를 먼저 확인합니다. 주요 결정은 `docs/15-decision-log.md`에 기록합니다.

## 목표

- 사용자가 열차를 조회하고 객차/좌석을 직접 선택해 예매할 수 있게 한다.
- 동일 좌석 중복 예매를 방지하기 위해 트랜잭션과 락 기반 동시성 제어를 적용한다.
- 결제 전 좌석을 HELD 상태로 임시 점유하고, 만료 시간이 지나면 자동으로 해제한다.
- 가상 결제 성공, 실패, 취소, 만료 흐름에 따라 예매와 좌석 상태를 일관되게 변경한다.
- 관리자 기능, 운영 로그, 모니터링, 장애 대응 문서를 포함해 운영 관점까지 보여준다.

## 기술 스택

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JPA Repository 조회 (QueryDSL은 후순위)
- PostgreSQL
- Redis 연동 기반 의존성 (캐시/TTL 활용은 후순위)
- JUnit5
- Testcontainers
- Gradle

### Frontend

- React
- Vite
- TypeScript
- CSS
- fetch 기반 API client

프론트엔드는 프로젝트의 핵심은 아니지만, 실제 흐름을 확인할 수 있도록 최소한의 사용자/관리자 UI를 구현합니다.

### Infra / Ops

- Docker
- Docker Compose
- Nginx
- AWS EC2
- GitHub Actions
- Spring Actuator
- Prometheus
- Grafana
- k6

## 주요 기능

### 공통/메인

- 메인 페이지
- 서비스 소개와 열차 검색 진입점
- 로그인 상태에 따른 사용자 메뉴 표시

### 사용자 기능

- 회원가입
- 로그인/로그아웃
- 내 정보 조회
- 이름/비밀번호 변경, 회원 탈퇴, 기존 JWT 무효화
- 열차 검색
- 운행편 상세 조회
- 운행편 좌석 맵 조회
- 객차/좌석 선택
- 좌석 임시 점유(HOLD)
- 가상 결제
- 내 예매 목록 조회
- 예매 상세 조회
- 예매 취소

### 관리자 기능

- 역 관리
- 노선 관리
- 열차 관리
- 운행 편성 관리
- 운행편 좌석 조회
- 좌석 BLOCK / UNBLOCK
- 전체 예매 조회
- 운영 로그 조회
- 운행 취소에 따른 HOLD 취소/전액 가상 환불
- 참조된 기준 데이터 수정·삭제 보호

## 운영 목표

초기 운영 환경은 비용을 고려해 단일 AWS EC2 인스턴스에서 Docker Compose로 구성합니다. 단, Nginx, Spring Boot, PostgreSQL, Redis, Prometheus, Grafana를 컨테이너 단위로 분리하여 향후 RDS, ElastiCache, 다중 WAS 구조로 확장 가능한 형태를 유지합니다.

## 문서 구조

상세 설계와 구현 결정은 `docs/` 아래 문서에 기록합니다. 주요 결정은 구현이 진행되면서 README와 설계 문서에 계속 반영합니다.


## 추가 설계 문서

- `docs/16-screen-map.md`: 메인, 사용자, 관리자 화면 구성
- `docs/17-backend-implementation-plan.md`: Spring Boot 백엔드 구현 순서
- `docs/18-database-ddl.md`: PostgreSQL DDL 초안
- `docs/19-test-scenarios.md`: 인증, 예매, 결제, Scheduler, 동시성 테스트 시나리오
- `docs/20-backend-setup-notes.md`: Spring Boot 백엔드 생성과 초기 공통 설정 기록
- `docs/21-auth-implementation-notes.md`: User, 회원가입, 로그인, JWT access token 구현 기록
- `docs/22-station-implementation-notes.md`: Station 도메인과 공개/관리자 API 구현 기록
- `docs/23-frontend-implementation-notes.md`: 현재 완성된 백엔드 기능에 대응하는 React 1차 UI 구현 기록
- `docs/24-route-implementation-notes.md`: Route 도메인과 관리자 API 구현 기록
- `docs/25-train-implementation-notes.md`: Train 도메인과 관리자 API 구현 기록
- `docs/26-car-seat-implementation-notes.md`: Car/Seat 도메인과 관리자 API 구현 기록
- `docs/27-train-schedule-implementation-notes.md`: TrainSchedule 도메인과 관리자/사용자 조회 API 구현 기록
- `docs/28-frontend-management-implementation-notes.md`: 현재 백엔드 기능에 대응하는 프론트 관리자/운행편 검색 화면 구현 기록
- `docs/29-database-local-setup-notes.md`: 로컬 PostgreSQL 연결과 실행 설정 기록
- `docs/30-schedule-seat-implementation-notes.md`: ScheduleSeat와 운행편 좌석 맵/API/UI 구현 기록
- `docs/31-reservation-hold-implementation-notes.md`: 좌석 HOLD, Reservation 생성, HOLD 만료 Scheduler 구현 기록
- `docs/32-payment-simulation-implementation-notes.md`: Payment 시뮬레이션과 예매/좌석 상태 확정 구현 기록
- `docs/33-fare-policy-notes.md`: 1차 단일 기본요금 정책과 향후 가격 테이블 확장 방향 기록
- `docs/34-current-progress-summary.md`: 현재 구현/DB/프론트/다음 작업 진행상황 요약
- `docs/35-reservation-management.md`: 기본요금, 예매 조회/취소/환불 API 및 정합성 기준
- `docs/36-work-report-20261018.md`: 2026-10-18 작업 결과, 검증, 남은 문제와 주의사항
- `docs/37-service-completion-plan.md`: 회원/운행/로그/마이그레이션 구현 정책
- `docs/38-local-and-container-runbook.md`: 로컬/Docker 실행, 관리자 생성, DB 전환, 모니터링/부하 절차
- `docs/39-work-report-20261019.md`: 최신 구현 결과와 검증·미검증 범위

현재 DB는 Flyway로 변경 이력을 관리하고 Hibernate는 validate만 수행합니다. Docker 구성은 작성했으며 Docker 미설치 환경의 컨테이너 실구동과 실제 서버 배포는 별도 검증이 필요합니다.
