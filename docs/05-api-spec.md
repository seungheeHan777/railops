# API 명세

이 문서는 RailOps의 REST API 초안입니다. 구현 단계에서 실제 DTO 이름, 검증 규칙, 에러 코드는 조정할 수 있습니다.

## 공통 규칙

Base URL:

```text
/api
```

공통 응답 형식:

```json
{
  "success": true,
  "data": {},
  "message": null
}
```

공통 에러 응답:

```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

인증:

```http
Authorization: Bearer {accessToken}
```

## Auth

### 회원가입

```http
POST /api/auth/signup
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "name": "홍길동"
}
```

Response:

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

### 로그인

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

Response:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "role": "USER"
  }
}
```

### 내 정보

```http
GET /api/auth/me
```

권한: 로그인 사용자

### 로그아웃

```http
POST /api/auth/logout
```

1차 구현에서는 클라이언트 토큰 삭제 중심으로 처리합니다. 서버 측 refresh token 또는 blacklist는 후순위입니다.

## Stations

### 역 목록

```http
GET /api/stations
```

### 역 검색

```http
GET /api/stations/search?keyword=서울
```

## Train Schedules

### 운행편 검색

```http
GET /api/train-schedules?from=SEOUL&to=BUSAN&date=2026-08-01
```

Response:

```json
[
  {
    "scheduleId": 1,
    "trainNo": "KTX-101",
    "trainType": "KTX",
    "routeName": "경부선",
    "origin": "서울",
    "destination": "부산",
    "departureTime": "2026-08-01T09:00:00",
    "arrivalTime": "2026-08-01T11:40:00",
    "status": "SCHEDULED"
  }
]
```

캐시:

- Redis에 30초 캐시합니다.
- 예매 정합성과 직접 관련된 좌석 상태는 캐시에 의존하지 않습니다.

### 운행편 상세

```http
GET /api/train-schedules/{scheduleId}
```

## Seats

### 운행편 좌석 조회

```http
GET /api/train-schedules/{scheduleId}/seats
```

Response:

```json
{
  "scheduleId": 1,
  "cars": [
    {
      "carId": 1,
      "carNo": 3,
      "seats": [
        {
          "scheduleSeatId": 10,
          "seatNo": "12A",
          "seatType": "WINDOW",
          "status": "AVAILABLE"
        }
      ]
    }
  ]
}
```

## Reservations

### 좌석 임시 점유와 예매 생성

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
  "reservationNo": "R202608010001",
  "paymentId": 200,
  "paymentNo": "P202608010001",
  "status": "PENDING_PAYMENT",
  "amount": 120000,
  "holdExpiresAt": "2026-08-01T09:10:00"
}
```

실패 예시:

- 이미 HELD인 좌석
- 이미 RESERVED인 좌석
- BLOCKED 좌석
- 취소된 운행편
- 로그인하지 않은 사용자

### 내 예매 목록

```http
GET /api/reservations/me
```

권한: 로그인 사용자

### 예매 상세

```http
GET /api/reservations/{reservationId}
```

권한: 본인 또는 ADMIN

### 예매 취소

```http
POST /api/reservations/{reservationId}/cancel
```

권한: 본인 또는 ADMIN

정책:

- 출발 전 예매만 취소 가능
- 1차 구현에서는 결제 완료 후 환불 상태를 단순화

## Payments

### 가상 결제 성공

```http
POST /api/payments/{paymentId}/simulate-success
```

권한: 로그인 사용자

정책:

- Reservation이 PENDING_PAYMENT인지 확인
- ScheduleSeat가 HELD인지 확인
- `hold_expires_at`이 지나지 않았는지 확인
- 성공 시 Payment SUCCESS, Reservation CONFIRMED, ScheduleSeat RESERVED 처리

### 가상 결제 실패

```http
POST /api/payments/{paymentId}/simulate-fail
```

### 가상 결제 취소

```http
POST /api/payments/{paymentId}/simulate-cancel
```

## Admin

권한: ADMIN

### 역 관리

```http
POST /api/admin/stations
GET /api/admin/stations
GET /api/admin/stations/{stationId}
PATCH /api/admin/stations/{stationId}
DELETE /api/admin/stations/{stationId}
```

### 노선 관리

```http
POST /api/admin/routes
GET /api/admin/routes
GET /api/admin/routes/{routeId}
PATCH /api/admin/routes/{routeId}
DELETE /api/admin/routes/{routeId}
```

### 열차 관리

```http
POST /api/admin/trains
GET /api/admin/trains
GET /api/admin/trains/{trainId}
PATCH /api/admin/trains/{trainId}
DELETE /api/admin/trains/{trainId}
```

### 운행 편성 관리

```http
POST /api/admin/train-schedules
GET /api/admin/train-schedules
GET /api/admin/train-schedules/{scheduleId}
PATCH /api/admin/train-schedules/{scheduleId}
PATCH /api/admin/train-schedules/{scheduleId}/status
```

### 좌석 관리

```http
PATCH /api/admin/schedule-seats/{scheduleSeatId}/block
PATCH /api/admin/schedule-seats/{scheduleSeatId}/unblock
```

### 전체 예매 조회

```http
GET /api/admin/reservations
GET /api/admin/reservations/{reservationId}
```

### 운영 로그 조회

```http
GET /api/admin/operation-logs?action=PAYMENT_SUCCESS&from=2026-08-01&to=2026-08-31
```

## Monitoring

```http
GET /actuator/health
GET /actuator/prometheus
```

## 주요 에러 코드 초안

```text
AUTH_REQUIRED
ACCESS_DENIED
USER_NOT_FOUND
INVALID_CREDENTIALS
STATION_NOT_FOUND
ROUTE_NOT_FOUND
TRAIN_NOT_FOUND
SCHEDULE_NOT_FOUND
SCHEDULE_NOT_BOOKABLE
SEAT_NOT_FOUND
SEAT_NOT_AVAILABLE
SEAT_ALREADY_HELD
SEAT_ALREADY_RESERVED
SEAT_BLOCKED
RESERVATION_NOT_FOUND
RESERVATION_NOT_OWNED
RESERVATION_NOT_PENDING_PAYMENT
PAYMENT_NOT_FOUND
PAYMENT_NOT_READY
HOLD_EXPIRED
CONCURRENCY_CONFLICT
```
