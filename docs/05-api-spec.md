# API 명세

이 문서는 RailOps의 REST API 설계 초안입니다. API는 프론트엔드와 백엔드가 서로 데이터를 주고받는 약속입니다.

## API와 DTO가 무엇인가

### API

API는 클라이언트가 서버 기능을 호출하는 입구입니다.

예를 들어 사용자가 로그인 버튼을 누르면 프론트엔드는 백엔드에 다음 요청을 보냅니다.

```http
POST /api/auth/login
```

백엔드는 이메일과 비밀번호를 확인한 뒤 로그인 결과를 JSON으로 응답합니다.

### DTO

DTO는 Data Transfer Object의 약자입니다. 화면과 서버 사이에서 주고받는 데이터 모양을 정한 객체입니다.

예를 들어 로그인 요청 DTO는 이런 모양입니다.

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

Spring Boot에서는 보통 다음처럼 클래스로 만듭니다.

```java
public record LoginRequest(
    String email,
    String password
) {}
```

DTO를 쓰는 이유:

- DB Entity를 그대로 외부에 노출하지 않기 위해
- 화면에서 필요한 값만 주고받기 위해
- 입력값 검증을 명확히 하기 위해
- API 문서와 Controller 코드를 맞추기 위해

즉, `docs/05-api-spec.md`는 나중에 Controller와 DTO를 만들 때 기준이 되는 문서입니다.

## 공통 규칙

Base URL:

```text
/api
```

공통 성공 응답:

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

인증 헤더:

```http
Authorization: Bearer {accessToken}
```

## Auth API

### 회원가입

```http
POST /api/auth/signup
```

Request DTO: `SignupRequest`

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "name": "홍길동"
}
```

Response DTO: `UserSummaryResponse`

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

검증 규칙:

- email은 필수이며 이메일 형식이어야 함
- password는 필수이며 최소 길이 필요
- name은 필수
- email은 중복될 수 없음

### 로그인

```http
POST /api/auth/login
```

Request DTO: `LoginRequest`

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

Response DTO: `LoginResponse`

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

### 내 정보 조회

```http
GET /api/auth/me
```

권한: 로그인 사용자

Response DTO: `UserSummaryResponse`

### 로그아웃

```http
POST /api/auth/logout
```

1차 구현에서는 클라이언트 토큰 삭제 중심으로 처리합니다. 서버 측 refresh token 또는 blacklist는 후순위입니다.

## Station API

### 역 목록

```http
GET /api/stations
```

Response DTO: `StationResponse[]`

```json
[
  {
    "id": 1,
    "name": "서울",
    "code": "SEOUL",
    "city": "서울"
  }
]
```

### 역 검색

```http
GET /api/stations/search?keyword=서울
```

Response DTO: `StationResponse[]`

## Train Schedule API

### 운행편 검색

```http
GET /api/train-schedules?from=SEOUL&to=BUSAN&date=2026-08-01
```

Response DTO: `TrainScheduleSearchResponse[]`

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

캐시 정책:

- Redis에 30초 캐시합니다.
- 좌석 상태와 예매 정합성은 캐시에 의존하지 않습니다.

### 운행편 상세

```http
GET /api/train-schedules/{scheduleId}
```

Response DTO: `TrainScheduleResponse`

## Seat API

### 운행편 좌석 조회

```http
GET /api/train-schedules/{scheduleId}/seats
```

Response DTO: `ScheduleSeatMapResponse`

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

## Reservation API

### 좌석 임시 점유와 예매 생성

```http
POST /api/reservations/hold
```

권한: 로그인 사용자

Request DTO: `ReservationHoldRequest`

```json
{
  "scheduleId": 1,
  "scheduleSeatIds": [10, 11]
}
```

Response DTO: `ReservationHoldResponse`

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

실패 케이스:

- 이미 HELD인 좌석
- 이미 RESERVED인 좌석
- BLOCKED 좌석
- 취소된 운행편
- 로그인하지 않은 사용자
- 선택 좌석 목록이 비어 있음

### 내 예매 목록

```http
GET /api/reservations/me
```

권한: 로그인 사용자

Response DTO: `ReservationSummaryResponse[]`

### 예매 상세

```http
GET /api/reservations/{reservationId}
```

권한: 본인 또는 ADMIN

Response DTO: `ReservationDetailResponse`

### 예매 취소

```http
POST /api/reservations/{reservationId}/cancel
```

권한: 본인 또는 ADMIN

Response DTO: `ReservationDetailResponse`

정책:

- 출발 전 예매만 취소 가능
- 1차 구현에서는 결제 완료 후 환불 상태를 단순화

## Payment API

### 가상 결제 성공

```http
POST /api/payments/{paymentId}/simulate-success
```

권한: 로그인 사용자

Response DTO: `PaymentResultResponse`

정책:

- Payment가 READY인지 확인
- Reservation이 PENDING_PAYMENT인지 확인
- ScheduleSeat가 HELD인지 확인
- `held_by_user_id`가 요청 사용자와 같은지 확인
- `hold_expires_at`이 지나지 않았는지 확인
- 성공 시 Payment SUCCESS, Reservation CONFIRMED, ScheduleSeat RESERVED 처리

### 가상 결제 실패

```http
POST /api/payments/{paymentId}/simulate-fail
```

Response DTO: `PaymentResultResponse`

### 가상 결제 취소

```http
POST /api/payments/{paymentId}/simulate-cancel
```

Response DTO: `PaymentResultResponse`

## Admin API

모든 Admin API 권한: ADMIN

### 역 관리

```http
POST /api/admin/stations
GET /api/admin/stations
GET /api/admin/stations/{stationId}
PATCH /api/admin/stations/{stationId}
DELETE /api/admin/stations/{stationId}
```

Request DTO 예시: `StationCreateRequest`

```json
{
  "name": "서울",
  "code": "SEOUL",
  "city": "서울"
}
```

### 노선 관리

```http
POST /api/admin/routes
GET /api/admin/routes
GET /api/admin/routes/{routeId}
PATCH /api/admin/routes/{routeId}
DELETE /api/admin/routes/{routeId}
```

Request DTO 예시: `RouteCreateRequest`

```json
{
  "name": "경부선",
  "originStationId": 1,
  "destinationStationId": 2
}
```

### 열차 관리

```http
POST /api/admin/trains
GET /api/admin/trains
GET /api/admin/trains/{trainId}
PATCH /api/admin/trains/{trainId}
DELETE /api/admin/trains/{trainId}
```

Request DTO 예시: `TrainCreateRequest`

```json
{
  "trainNo": "KTX-101",
  "trainType": "KTX",
  "name": "KTX 101"
}
```

Response DTO 예시: `TrainResponse`

```json
{
  "id": 1,
  "trainNo": "KTX-101",
  "trainType": "KTX",
  "name": "KTX 101"
}
```

검증 규칙:

- `trainNo`는 필수이며 중복될 수 없음
- `trainNo`와 `trainType`은 영문, 숫자, `_`, `-`만 허용
- `trainNo`와 `trainType`은 저장 시 대문자로 정규화
- `name`은 필수

### 객차 관리

```http
POST /api/admin/trains/{trainId}/cars
GET /api/admin/trains/{trainId}/cars
GET /api/admin/cars/{carId}
PATCH /api/admin/cars/{carId}
DELETE /api/admin/cars/{carId}
```

Request DTO 예시: `CarCreateRequest`

```json
{
  "carNo": 1,
  "seatCount": 56
}
```

Response DTO 예시: `CarResponse`

```json
{
  "id": 10,
  "trainId": 1,
  "trainNo": "KTX-101",
  "trainType": "KTX",
  "carNo": 1,
  "seatCount": 56
}
```

검증 규칙:

- `carNo`는 열차 안에서 중복될 수 없음
- `seatCount`는 1 이상이어야 함

### 물리 좌석 관리

```http
POST /api/admin/cars/{carId}/seats
GET /api/admin/cars/{carId}/seats
GET /api/admin/seats/{seatId}
PATCH /api/admin/seats/{seatId}
DELETE /api/admin/seats/{seatId}
```

Request DTO 예시: `SeatCreateRequest`

```json
{
  "seatNo": "12A",
  "seatType": "WINDOW"
}
```

Response DTO 예시: `SeatResponse`

```json
{
  "id": 100,
  "carId": 10,
  "trainId": 1,
  "carNo": 1,
  "seatNo": "12A",
  "seatType": "WINDOW"
}
```

좌석 타입:

```text
STANDARD
PRIORITY
WINDOW
AISLE
```

검증 규칙:

- `seatNo`는 객차 안에서 중복될 수 없음
- `seatNo`는 저장 시 대문자로 정규화
- `seatType`은 지정된 enum 값만 허용
### 운행 편성 관리

```http
POST /api/admin/train-schedules
GET /api/admin/train-schedules
GET /api/admin/train-schedules/{scheduleId}
PATCH /api/admin/train-schedules/{scheduleId}
PATCH /api/admin/train-schedules/{scheduleId}/status
DELETE /api/admin/train-schedules/{scheduleId}
```

Request DTO 예시: `TrainScheduleCreateRequest`

```json
{
  "trainId": 1,
  "routeId": 1,
  "operationDate": "2026-08-01",
  "departureTime": "2026-08-01T09:00:00",
  "arrivalTime": "2026-08-01T11:40:00"
}
```

Status 변경 Request DTO 예시: `TrainScheduleStatusUpdateRequest`

```json
{
  "status": "DELAYED"
}
```

Response DTO 예시: `TrainScheduleResponse`

```json
{
  "id": 1,
  "trainId": 1,
  "trainNo": "KTX-101",
  "trainType": "KTX",
  "routeId": 1,
  "routeName": "경부선",
  "originStationId": 1,
  "originStationName": "서울",
  "originStationCode": "SEOUL",
  "destinationStationId": 2,
  "destinationStationName": "부산",
  "destinationStationCode": "BUSAN",
  "operationDate": "2026-08-01",
  "departureTime": "2026-08-01T09:00:00",
  "arrivalTime": "2026-08-01T11:40:00",
  "status": "SCHEDULED"
}
```

운행편 상태:

```text
SCHEDULED
DELAYED
CANCELED
COMPLETED
```

검증 규칙:

- Train이 존재해야 함
- Route가 존재해야 함
- arrivalTime은 departureTime보다 늦어야 함
- operationDate는 departureTime의 날짜와 같아야 함
- 같은 Train의 운행 시간이 기존 운행편과 겹칠 수 없음

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

## Monitoring API

```http
GET /actuator/health
GET /actuator/prometheus
```

## 주요 DTO 목록

```text
SignupRequest
LoginRequest
LoginResponse
UserSummaryResponse
StationResponse
StationCreateRequest
StationUpdateRequest
RouteCreateRequest
RouteUpdateRequest
TrainCreateRequest
TrainUpdateRequest
CarCreateRequest
CarUpdateRequest
CarResponse
SeatCreateRequest
SeatUpdateRequest
SeatResponse
TrainScheduleCreateRequest
TrainScheduleUpdateRequest
TrainScheduleStatusUpdateRequest
TrainScheduleResponse
TrainScheduleSearchResponse
TrainScheduleDetailResponse
ScheduleSeatMapResponse
CarSeatResponse
ReservationHoldRequest
ReservationHoldResponse
ReservationSummaryResponse
ReservationDetailResponse
PaymentResultResponse
OperationLogResponse
```

## 주요 에러 코드 초안

```text
AUTH_REQUIRED
ACCESS_DENIED
USER_NOT_FOUND
INVALID_CREDENTIALS
DUPLICATE_EMAIL
STATION_NOT_FOUND
DUPLICATE_STATION_CODE
ROUTE_NOT_FOUND
TRAIN_NOT_FOUND
DUPLICATE_TRAIN_NO
CAR_NOT_FOUND
DUPLICATE_CAR_NO
DUPLICATE_SEAT_NO
SCHEDULE_NOT_FOUND
SCHEDULE_NOT_BOOKABLE
INVALID_SCHEDULE_TIME
TRAIN_SCHEDULE_CONFLICT
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

## 이 문서 다음 단계

이 API 명세를 기준으로 Spring Boot에서 다음 코드를 작성합니다.

```text
Controller
- API 주소와 HTTP 메서드를 받는 클래스

Request DTO
- 클라이언트가 보내는 JSON을 받는 클래스

Response DTO
- 클라이언트에 내려줄 JSON을 만드는 클래스

Service
- 실제 비즈니스 로직을 처리하는 클래스

Exception Handler
- 에러 응답 형식을 통일하는 클래스
```
