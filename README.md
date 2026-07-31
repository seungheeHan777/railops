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
- QueryDSL
- PostgreSQL
- Redis
- JUnit5
- Testcontainers
- Gradle

### Frontend

- React
- Vite
- TypeScript
- Tailwind CSS
- Axios 또는 TanStack Query

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
- 열차 검색
- 운행편 상세 조회
- 객차/좌석 선택
- 좌석 임시 점유
- 가상 결제
- 내 예매 목록 조회
- 예매 상세 조회
- 예매 취소

### 관리자 기능

- 역 관리
- 노선 관리
- 열차 관리
- 운행 편성 관리
- 좌석 BLOCK / UNBLOCK
- 전체 예매 조회
- 운영 로그 조회

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