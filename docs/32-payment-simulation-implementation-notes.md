# Payment 시뮬레이션 구현 노트

이 문서는 RailOps의 가상 결제 API와 예매/좌석 상태 확정 흐름을 기록합니다.

## 구현 범위

실제 PG 연동 대신 백엔드 API로 결제 성공, 실패, 취소를 시뮬레이션합니다.

HOLD 생성 시 함께 생성되는 데이터:

```text
Reservation: PENDING_PAYMENT
ReservationSeat: 선택 좌석 목록
Payment: READY
ScheduleSeat: HELD
```

결제 API는 `paymentId`를 기준으로 동작합니다.

## 구현된 API

```http
POST /api/payments/{paymentId}/simulate-success
POST /api/payments/{paymentId}/simulate-fail
POST /api/payments/{paymentId}/simulate-cancel
```

권한: 로그인 사용자

응답 DTO: `PaymentResultResponse`

```json
{
  "paymentId": 600,
  "paymentNo": "P20260801090000U1",
  "reservationId": 500,
  "paymentStatus": "SUCCESS",
  "reservationStatus": "CONFIRMED",
  "amount": 0,
  "processedAt": "2026-08-01T09:02:00"
}
```

## 상태 전이

### 결제 성공

```text
Payment READY -> SUCCESS
Reservation PENDING_PAYMENT -> CONFIRMED
ScheduleSeat HELD -> RESERVED
```

검증 조건:

```text
Payment가 READY여야 함
Reservation이 PENDING_PAYMENT여야 함
요청 사용자가 Reservation 소유자여야 함
Reservation holdExpiresAt이 지나지 않아야 함
모든 ScheduleSeat가 HELD여야 함
모든 ScheduleSeat heldByUser가 요청 사용자여야 함
모든 ScheduleSeat holdExpiresAt이 지나지 않아야 함
```

### 결제 실패

```text
Payment READY -> FAILED
Reservation PENDING_PAYMENT -> PAYMENT_FAILED
ScheduleSeat HELD -> AVAILABLE
```

### 결제 취소

```text
Payment READY -> CANCELED
Reservation PENDING_PAYMENT -> CANCELED
ScheduleSeat HELD -> AVAILABLE
```

### HOLD 만료 후 결제 성공 시도

결제 성공 시점에 HOLD가 만료되어 있으면 다음 상태로 정리한 뒤 `HOLD_EXPIRED` 예외를 반환합니다.

```text
Payment READY -> EXPIRED
Reservation PENDING_PAYMENT -> EXPIRED
ScheduleSeat HELD -> AVAILABLE
```

## 동시성 처리

결제 처리 시 다음 데이터를 잠금 조회합니다.

```text
Payment: PESSIMISTIC_WRITE
ReservationSeat -> ScheduleSeat: PESSIMISTIC_WRITE
```

같은 결제에 대한 중복 요청은 먼저 처리된 요청만 상태를 바꾸고, 이후 요청은 `PAYMENT_NOT_READY`로 실패합니다.

## 생성된 주요 클래스

```text
Payment
PaymentStatus
PaymentRepository
PaymentResultResponse
PaymentService
PaymentController
PaymentServiceTest
```

## 프론트 구현

사용자 운행편 검색 화면에서 좌석 HOLD 성공 후 다음 버튼을 표시합니다.

```text
결제 성공
결제 실패
취소
```

결제 성공 시 좌석 맵을 다시 조회해 `RESERVED` 상태를 반영합니다. 결제 실패/취소 시 좌석은 다시 `AVAILABLE`로 보입니다.

수정된 파일:

```text
frontend/src/App.tsx
frontend/src/api/railops.ts
frontend/src/types/api.ts
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

```text
1. 내 예매 목록/상세 API 구현
2. 예매 취소 API 구현
3. 결제 완료 후 취소 정책 정리
4. 사용자 마이페이지에 내 예매 화면 연결
5. 관리자 전체 예매 조회 화면 구현
```