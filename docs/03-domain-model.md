# 도메인 모델

RailOps의 도메인은 철도 운행 정보, 좌석 상태, 예매, 결제, 운영 로그로 나뉩니다. 핵심은 물리적 좌석인 `Seat`와 특정 운행편에서의 좌석 상태인 `ScheduleSeat`를 분리하는 것입니다.

## 전체 관계 요약

```text
User 1 ─ N Reservation
User 1 ─ N OperationLog
Station 1 ─ N Route(origin)
Station 1 ─ N Route(destination)
Route 1 ─ N TrainSchedule
Train 1 ─ N TrainSchedule
Train 1 ─ N Car
Car 1 ─ N Seat
TrainSchedule 1 ─ N ScheduleSeat
Seat 1 ─ N ScheduleSeat
Reservation 1 ─ N ReservationSeat
ScheduleSeat 1 ─ 0..1 ReservationSeat
Reservation 1 ─ 1 Payment
```

## User

서비스 이용자입니다. 일반 사용자와 관리자를 모두 표현합니다.

주요 책임:

- 회원가입과 로그인 주체
- 예매 생성 주체
- 관리자 권한 판단 기준
- 좌석 HELD 소유자 판단 기준

주요 속성:

```text
id
email
password
name
role
status
created_at
updated_at
```

권한:

```text
USER
ADMIN
```

상태:

```text
ACTIVE
LOCKED
DELETED
```

## Station

철도역 정보입니다. 열차 검색의 출발역과 도착역 조건으로 사용됩니다.

주요 속성:

```text
id
name
code
city
created_at
updated_at
```

제약:

- `code`는 고유해야 합니다.
- `name`은 검색 대상입니다.

## Route

출발역과 도착역을 연결하는 노선입니다.

주요 책임:

- 출발역/도착역 조합 관리
- 운행편이 어느 구간을 달리는지 표현

주요 속성:

```text
id
name
origin_station_id
destination_station_id
created_at
updated_at
```

제약:

- 출발역과 도착역은 같을 수 없습니다.
- 같은 출발역/도착역/노선명 조합은 중복 등록하지 않습니다.

## Train

열차 자체의 기본 정보입니다. 날짜와 시간에 따른 실제 운행 여부는 `TrainSchedule`에서 관리합니다.

주요 속성:

```text
id
train_no
train_type
name
created_at
updated_at
```

예시:

```text
KTX-101
ITX-203
```

## TrainSchedule

특정 날짜와 시간에 운행하는 편성입니다. 사용자가 검색하고 예매하는 대상입니다.

주요 속성:

```text
id
train_id
route_id
operation_date
departure_time
arrival_time
status
created_at
updated_at
```

상태:

```text
SCHEDULED
DELAYED
CANCELED
COMPLETED
```

정책:

- CANCELED 운행편은 신규 예매할 수 없습니다.
- COMPLETED 운행편은 예매, 취소 대상이 아닙니다.

## Car

열차의 객차 정보입니다.

주요 속성:

```text
id
train_id
car_no
seat_count
created_at
updated_at
```

정책:

- 객차는 Train에 종속됩니다.
- 실제 좌석 배치 정보는 Seat 목록으로 표현합니다.

## Seat

객차 안의 물리적 좌석입니다. 예매 가능 여부를 직접 들고 있지 않습니다.

주요 속성:

```text
id
car_id
seat_no
seat_type
created_at
updated_at
```

좌석 타입 예시:

```text
STANDARD
PRIORITY
WINDOW
AISLE
```

정책:

- 같은 객차 안에서 `seat_no`는 고유해야 합니다.
- 물리 좌석 정보와 운행편별 좌석 상태를 분리하기 위해 예매 상태는 `ScheduleSeat`에 둡니다.

## ScheduleSeat

특정 운행편에서의 좌석 상태입니다. 좌석 예매 정합성의 핵심 테이블입니다.

주요 속성:

```text
id
train_schedule_id
seat_id
status
held_by_user_id
hold_expires_at
version
created_at
updated_at
```

상태:

```text
AVAILABLE  = 예매 가능
HELD       = 결제 전 임시 점유
RESERVED   = 예매 완료
BLOCKED    = 관리자 또는 시스템에 의한 판매 중지
```

정책:

- 같은 `train_schedule_id`와 `seat_id` 조합은 하나만 존재해야 합니다.
- HELD 상태에는 `held_by_user_id`와 `hold_expires_at`이 있어야 합니다.
- AVAILABLE, RESERVED, BLOCKED 상태에서는 `hold_expires_at`을 사용하지 않습니다.
- BLOCKED는 자동 만료되지 않으며 관리자가 해제해야 합니다.
- HOLD 만료의 기준은 PostgreSQL의 `hold_expires_at`입니다.

## Reservation

사용자의 예매 단위입니다. 여러 좌석을 한 번에 예매할 수 있으므로 좌석 목록은 `ReservationSeat`로 분리합니다.

주요 속성:

```text
id
reservation_no
user_id
train_schedule_id
status
total_amount
created_at
confirmed_at
canceled_at
expired_at
updated_at
```

상태:

```text
PENDING_PAYMENT
CONFIRMED
CANCELED
EXPIRED
PAYMENT_FAILED
```

정책:

- PENDING_PAYMENT 상태에서만 결제 성공/실패/취소 처리를 할 수 있습니다.
- CONFIRMED 상태는 좌석이 RESERVED 상태여야 합니다.
- EXPIRED 또는 PAYMENT_FAILED 상태가 되면 관련 HELD 좌석은 AVAILABLE로 해제합니다.

## ReservationSeat

예매에 포함된 좌석입니다.

주요 속성:

```text
id
reservation_id
schedule_seat_id
price
created_at
```

정책:

- 하나의 Reservation은 하나 이상의 ReservationSeat를 가집니다.
- 같은 `schedule_seat_id`가 CONFIRMED 예매에 중복 포함되면 안 됩니다.
- 1차 가격 정책은 구현 전 다시 결정합니다.

## Payment

가상 결제 정보입니다. 실제 PG 연동은 하지 않지만 결제 상태 전이와 예매 확정 흐름은 백엔드에서 처리합니다.

주요 속성:

```text
id
reservation_id
payment_no
amount
status
requested_at
approved_at
failed_at
canceled_at
expired_at
created_at
updated_at
```

상태:

```text
READY
SUCCESS
FAILED
CANCELED
EXPIRED
```

정책:

- Reservation 하나당 Payment 하나를 1차 기준으로 둡니다.
- 결제 성공 시 Payment, Reservation, ScheduleSeat 상태를 같은 트랜잭션에서 변경합니다.
- 결제 성공 API는 `hold_expires_at`이 지나지 않았는지 반드시 확인합니다.

## OperationLog

운영자가 확인할 수 있는 주요 이벤트 이력입니다.

주요 속성:

```text
id
actor_id
action
target_type
target_id
detail
ip_address
created_at
```

이벤트 예시:

```text
RESERVATION_CREATED
RESERVATION_CONFIRMED
PAYMENT_SUCCESS
PAYMENT_FAILED
PAYMENT_CANCELED
SEAT_HOLD_EXPIRED
ADMIN_BLOCK_SEAT
ADMIN_UNBLOCK_SEAT
LOGIN_FAILED
```
