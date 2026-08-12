# Reservation HOLD 구현 노트

이 문서는 좌석 HOLD API와 HOLD 만료 Scheduler 구현 내용을 기록합니다.

## 구현 범위

사용자가 운행편 좌석을 선택하면 결제 전 임시 점유 상태를 만듭니다.

```text
ScheduleSeat AVAILABLE -> HELD
Reservation PENDING_PAYMENT 생성
ReservationSeat 생성
holdExpiresAt = 요청 시각 + 10분
```

현재 가격 정책은 아직 확정하지 않았으므로 `ReservationSeat.price`와 `Reservation.totalAmount`는 임시로 0을 사용합니다. 실제 가격 모델은 결제 구현 전에 결정합니다.

## 구현된 API

```http
POST /api/reservations/hold
```

권한: 로그인 사용자

Request:

```json
{
  "scheduleId": 1,
  "scheduleSeatIds": [10, 11]
}
```

Response:

```json
{
  "reservationId": 100,
  "scheduleId": 1,
  "scheduleSeatIds": [10, 11],
  "amount": 0,
  "holdExpiresAt": "2026-08-01T09:10:00"
}
```

## 동시성 처리

HOLD 요청은 선택한 `ScheduleSeat`를 PESSIMISTIC_WRITE로 조회합니다.

```text
select ScheduleSeat
where id in (...)
for update
```

같은 좌석에 동시에 HOLD 요청이 들어오면 먼저 잠금을 얻은 요청만 `HELD`로 바꿀 수 있습니다. 뒤늦게 들어온 요청은 상태가 이미 HELD인 것을 보고 `SEAT_ALREADY_HELD`로 실패합니다.

## 검증 정책

HOLD 요청 시 다음을 확인합니다.

```text
사용자 존재 여부
운행편 존재 여부
운행편 상태가 SCHEDULED 또는 DELAYED인지
요청 좌석이 모두 존재하는지
요청 좌석이 같은 운행편에 속하는지
좌석 상태가 AVAILABLE인지
```

실패 케이스:

```text
USER_NOT_FOUND
SCHEDULE_NOT_FOUND
SCHEDULE_NOT_BOOKABLE
SCHEDULE_SEAT_NOT_FOUND
SEAT_BLOCKED
SEAT_ALREADY_HELD
SEAT_ALREADY_RESERVED
SEAT_NOT_AVAILABLE
```

## HOLD 만료 Scheduler

앱 시작 시 `@EnableScheduling`으로 Scheduler를 활성화했습니다.

```text
HoldExpirationScheduler
```

기본 실행 주기:

```text
60초
```

설정 키:

```properties
railops.hold.expiration-fixed-delay-ms=60000
```

Scheduler는 다음 두 가지를 처리합니다.

```text
1. holdExpiresAt이 지난 PENDING_PAYMENT Reservation -> EXPIRED
2. holdExpiresAt이 지난 HELD ScheduleSeat -> AVAILABLE
```

DB 연결 후에는 배치 크기, 인덱스, 대량 만료 처리 방식을 다시 조정합니다.

## 프론트 구현

사용자 운행편 검색 화면에서 상세 조회 후 AVAILABLE 좌석에 `HOLD` 버튼을 표시합니다.

로그인 사용자가 `HOLD`를 누르면 `POST /api/reservations/hold`를 호출하고, 성공 후 좌석 맵을 다시 조회해 HELD 상태를 반영합니다.

수정된 파일:

```text
frontend/src/App.tsx
frontend/src/api/railops.ts
frontend/src/types/api.ts
```

## 생성된 주요 클래스

```text
Reservation
ReservationSeat
ReservationStatus
ReservationRepository
ReservationSeatRepository
ReservationHoldRequest
ReservationHoldResponse
ReservationService
ReservationController
HoldExpirationScheduler
ReservationServiceTest
TimeConfig
```

## 검증

백엔드:

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat test
```

결과:

```text
BUILD SUCCESSFUL
```

프론트:

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run build
```

결과:

```text
빌드 성공
```

## 다음 작업

Payment 시뮬레이션 구현 내용은 `docs/32-payment-simulation-implementation-notes.md`에 기록했습니다.

다음은 내 예매 목록/상세/취소 기능입니다.