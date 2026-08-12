# 현재 진행상황 요약

작성일: 2026-08-12

이 문서는 RailOps 프로젝트의 현재 구현 상태, 로컬 실행 상태, 다음 작업을 한눈에 보기 위해 정리한 문서입니다.

## 현재 실행/DB 상태

로컬 PostgreSQL 연동을 시작했습니다.

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

동시성 처리는 `ScheduleSeat` 잠금 조회와 `@Version` 컬럼을 기반으로 확장 가능한 상태입니다.

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

현재 코드는 아직 `amount = 0` 임시값을 사용합니다. 다음 가격 구현 시 `docs/33-fare-policy-notes.md` 기준으로 단일 기본요금을 반영합니다.

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

최근 확인한 명령:

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat test
```

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run build
```

두 명령 모두 통과한 상태입니다.

## 다음 작업

우선순위는 아래 순서입니다.

```text
1. 단일 기본요금 구현으로 amount=0 임시값 제거
2. Reservation 목록/상세 API 구현
3. Reservation 취소 API 구현
4. 결제 완료 후 예매 취소 정책 구현
5. 사용자 마이페이지에 내 예매 목록/상세/취소 화면 연결
6. 관리자 전체 예매 조회 화면 구현
7. 관리자 계정 생성 방식 정리
8. Flyway 마이그레이션 도입 검토
```

## 아직 보류 중인 결정

```text
결제 완료 후 취소 시 REFUNDED 상태를 도입할지 여부
JWT refresh token 도입 시점
관리자 계정 생성 방식
Station/Route 삭제 정책
HOLD 만료 Scheduler 배치 크기와 락 범위
Docker Desktop 기반 DB 실행 전환 시점
```
