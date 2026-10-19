# 현재 진행상황 요약

갱신일: 2026-10-18

이 문서는 RailOps 프로젝트의 현재 구현 상태, 로컬 실행 상태, 다음 작업을 한눈에 보기 위해 정리한 문서입니다.

기본요금, 본인 예매 조회/취소, 전액 가상 환불, 관리자 예매 조회와 UI를 구현했습니다. PostgreSQL 격리 스키마 통합 테스트와 데스크톱/모바일 브라우저 검증을 수행했습니다. 최신 검증/남은 문제/실행법은 `docs/36-work-report-20261018.md`, API 계약은 `docs/35-reservation-management.md`를 확인합니다. 역, 노선, 열차, 객차, 좌석, 운행편은 관리자 CRUD로 직접 등록합니다.

## 현재 실행/DB 상태

로컬 PostgreSQL을 연결하고 정상 백엔드와 프론트를 실행했습니다. 기존 11개 테이블을 유지하며 payments에 환불 컬럼과 REFUNDED 상태 제약조건을 반영했습니다. DEMO_SEOUL → DEMO_BUSAN, DEMO-101의 2026-10-19/20 12:00 운행편을 추가했습니다. 계정/기존 예매 데이터는 삭제하지 않았습니다.

```text
DBMS: PostgreSQL
Host: localhost
Port: 5432
Database: railops
User: railops
Profile: local
```

백엔드는 local 프로필로 실행하면 `railops` DB에 접속합니다.

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

실행 로그 기준으로 다음이 확인되었습니다.

```text
active profile: local
JDBC URL: jdbc:postgresql://localhost:5432/railops
schema: railops/public
Tomcat port: 8080
```

DBeaver에서 아래 테이블 자동 생성도 확인했습니다.

```text
cars
payments
reservation_seats
reservations
routes
schedule_seats
seats
stations
train_schedules
trains
users
```

## 완료된 백엔드 기능

### 인증/User

```text
회원가입
로그인
로그아웃 응답
내 정보 조회
BCrypt 비밀번호 암호화
JWT access token 발급/검증
USER/ADMIN 권한 구분
```

회원가입 시 기본 권한은 `USER`입니다. 관리자 계정은 현재 DB에서 `role = 'ADMIN'`으로 직접 변경하는 방식입니다.

### 기준 데이터 관리

관리자 API로 아래 데이터를 관리할 수 있습니다.

```text
Station
Route
Train
Car
Seat
TrainSchedule
```

사용자 공개 API로 역 조회와 운행편 검색/상세 조회가 가능합니다.

### 운행편 좌석 상태

`ScheduleSeat`를 구현했습니다.

```text
Seat = 물리 좌석
ScheduleSeat = 특정 운행편에서의 좌석 상태
```

상태:

```text
AVAILABLE
HELD
RESERVED
BLOCKED
```

운행편 생성 시 해당 열차의 물리 좌석을 기준으로 `ScheduleSeat(AVAILABLE)`가 자동 생성됩니다.

### HOLD/Reservation

좌석 HOLD API를 구현했습니다.

```http
POST /api/reservations/hold
```

처리 흐름:

```text
ScheduleSeat AVAILABLE -> HELD
Reservation PENDING_PAYMENT 생성
ReservationSeat 생성
Payment READY 생성
holdExpiresAt = 요청 시각 + 10분
```

`ScheduleSeat` 잠금 조회와 `@Version` 컬럼을 사용합니다. 결제/취소/만료는 Payment → 좌석 ID 오름차순 잠금으로 통일했으며 PostgreSQL 동시성/부분 실패 롤백을 검증했습니다. 예매/결제 번호는 UUID를 사용합니다.

### HOLD 만료 Scheduler

Spring Scheduler를 활성화했습니다.

```text
HoldExpirationScheduler
```

만료 처리:

```text
READY Payment -> EXPIRED
PENDING_PAYMENT Reservation -> EXPIRED
HELD ScheduleSeat -> AVAILABLE
```

DB/테이블 구조가 안정되면 배치 크기와 쿼리 최적화를 재검토합니다.

### Payment 시뮬레이션

가상 결제 API를 구현했습니다.

```http
POST /api/payments/{paymentId}/simulate-success
POST /api/payments/{paymentId}/simulate-fail
POST /api/payments/{paymentId}/simulate-cancel
```

상태 전이:

```text
성공: Payment SUCCESS, Reservation CONFIRMED, ScheduleSeat RESERVED
실패: Payment FAILED, Reservation PAYMENT_FAILED, ScheduleSeat AVAILABLE
취소: Payment CANCELED, Reservation CANCELED, ScheduleSeat AVAILABLE
만료 후 성공 시도: Payment EXPIRED, Reservation EXPIRED, ScheduleSeat AVAILABLE, HOLD_EXPIRED 반환
```

## 완료된 프론트 기능

React/Vite 기반 최소 UI를 구현했습니다.

```text
메인 화면
회원가입
로그인/로그아웃
마이페이지
역 조회
운행편 검색
운행편 상세 조회
운행편 좌석 맵 조회
좌석 HOLD
결제 성공/실패/취소 시뮬레이션
관리자 콘솔
관리자 Station/Route/Train/Car/Seat/TrainSchedule 관리
관리자 ScheduleSeat BLOCK/UNBLOCK
```

프론트 실행:

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run dev
```

주소:

```text
http://127.0.0.1:5173
```

## 가격 정책 결정

1차 가격 정책은 단순화했습니다.

```text
거리/노선/열차/좌석 타입 무관 단일 기본요금
```

개발 기본값 50,000원을 반영했으며 `RAILOPS_DEFAULT_SEAT_PRICE`로 변경할 수 있습니다. 새 예매부터 가격을 스냅샷으로 저장합니다. 기존 0원 예매는 그대로 유지합니다. 본인 예매 목록/상세/취소 및 관리자 전체 예매 조회를 구현했으며 출발 전 전액 가상 환불을 제공합니다.

## 주요 문서

```text
docs/04-erd.md
docs/05-api-spec.md
docs/15-decision-log.md
docs/29-database-local-setup-notes.md
docs/30-schedule-seat-implementation-notes.md
docs/31-reservation-hold-implementation-notes.md
docs/32-payment-simulation-implementation-notes.md
docs/33-fare-policy-notes.md
```

## 검증 상태

2026-10-18 구현 후 재검증한 명령 (DB 통합 테스트는 RAILOPS_TEST_DB_URL 설정 필요):

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat test
```

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run build
```

두 명령 모두 통과했습니다. 실제 PostgreSQL 격리 스키마에서 HTTP 전체 흐름과 동시성도 검증했고 테스트 스키마를 정리했습니다. 브라우저에서 검색/HOLD/마이페이지 결제/환불/관리자 상세를 데스크톱과 모바일로 확인했습니다.

## 다음 작업

우선순위는 아래 순서입니다.

```text
1. 예매가 존재하는 운행편/기준 데이터 수정·삭제 정책과 운행 취소 일괄 환불
2. 회원 정보 수정/비밀번호 변경/탈퇴 및 관리자 계정 생성 방식
3. 운영 로그 저장/조회 및 인증 실패 응답 정리
4. Flyway 마이그레이션, Docker 앱 전체 실행, 배포/모니터링/부하 테스트
```

## 아직 보류 중인 결정

```text
실서비스 기본요금/환불 수수료/부분 취소 정책 (현재 개발 정책은 50,000원/출발 전 전액 가상 환불)
JWT refresh token 도입 시점
관리자 계정 생성 방식
Station/Route 삭제 정책
HOLD 만료 Scheduler 배치 크기와 락 범위
Docker Desktop 기반 DB 실행 전환 시점
```
