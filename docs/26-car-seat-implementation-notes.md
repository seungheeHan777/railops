# Car/Seat 구현 노트

이 문서는 RailOps의 객차 Car와 물리 좌석 Seat 도메인, 관리자 API 구현 내용을 기록합니다.

## 구현 범위

Car는 Train에 속한 객차입니다. Seat는 Car에 속한 물리 좌석입니다.

```text
Train 1 -> N Car
Car 1 -> N Seat
```

이번 구현은 아직 예약 좌석 상태를 만들지 않습니다. 실제 운행편별 좌석 상태는 다음 단계의 `ScheduleSeat`에서 관리합니다.

## 구현된 API

### Car 관리자 API

```text
POST   /api/admin/trains/{trainId}/cars
GET    /api/admin/trains/{trainId}/cars
GET    /api/admin/cars/{carId}
PATCH  /api/admin/cars/{carId}
DELETE /api/admin/cars/{carId}
```

### Seat 관리자 API

```text
POST   /api/admin/cars/{carId}/seats
GET    /api/admin/cars/{carId}/seats
GET    /api/admin/seats/{seatId}
PATCH  /api/admin/seats/{seatId}
DELETE /api/admin/seats/{seatId}
```

모든 API는 관리자 API입니다.

```text
/api/admin/** -> ADMIN 권한 필요
```

## 생성된 주요 클래스

```text
Car
Seat
SeatType
CarRepository
SeatRepository
CarCreateRequest / CarUpdateRequest / CarResponse
SeatCreateRequest / SeatUpdateRequest / SeatResponse
CarService
SeatService
AdminCarController
AdminSeatController
CarServiceTest
SeatServiceTest
```

## Car 정책

주요 필드:

```text
id
train
carNo
seatCount
createdAt
updatedAt
```

정책:

```text
같은 Train 안에서 carNo는 중복될 수 없음
seatCount는 1 이상이어야 함
```

실패 케이스:

```text
Train 없음 -> TRAIN_NOT_FOUND
Car 없음 -> CAR_NOT_FOUND
중복 객차 번호 -> DUPLICATE_CAR_NO
```

## Seat 정책

주요 필드:

```text
id
car
seatNo
seatType
createdAt
updatedAt
```

SeatType:

```text
STANDARD
PRIORITY
WINDOW
AISLE
```

정책:

```text
같은 Car 안에서 seatNo는 중복될 수 없음
seatNo는 trim + upper-case로 정규화
seatType은 enum 값만 허용
```

실패 케이스:

```text
Car 없음 -> CAR_NOT_FOUND
Seat 없음 -> SEAT_NOT_FOUND
중복 좌석 번호 -> DUPLICATE_SEAT_NO
```

## 예약과의 관계

이번에 만든 Seat는 물리 좌석입니다.

```text
KTX-101 1호차 12A
```

하지만 예매 상태는 아직 여기에 저장하지 않습니다. 같은 좌석이라도 운행일과 운행편에 따라 상태가 달라지기 때문입니다.

```text
2026-08-01 KTX-101 1호차 12A = RESERVED
2026-08-02 KTX-101 1호차 12A = AVAILABLE
```

그래서 다음 단계에서 `TrainSchedule`과 `ScheduleSeat`를 만들고, `ScheduleSeat`가 `AVAILABLE`, `HELD`, `RESERVED`, `BLOCKED` 상태를 갖게 됩니다.

## 테스트

실행 명령:

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat clean test
```

검증 결과:

```text
BUILD SUCCESSFUL
```

## 다음 작업

다음 구현은 TrainSchedule입니다.

TrainSchedule에서는 다음 관계를 연결합니다.

```text
Train + Route + operationDate + departureTime + arrivalTime
```

이후 ScheduleSeat 생성 정책을 정하면 예약/HOLD 기능으로 넘어갈 수 있습니다.