# Database DDL 초안

이 문서는 `docs/04-erd.md`를 바탕으로 작성한 PostgreSQL DDL 초안입니다. DDL은 Data Definition Language의 약자로, 실제 데이터베이스 테이블을 만드는 SQL입니다.

## ERD와 DDL의 차이

```text
ERD
- 설계도
- 테이블과 관계를 설명
- 사람이 구조를 이해하기 위한 문서

DDL
- 실행 가능한 SQL
- CREATE TABLE, CREATE INDEX 같은 명령
- DB에 실제 구조를 만드는 기준
```

즉, ERD로 먼저 구조를 이해하고, DDL로 실제 DB를 만듭니다.

## 1차 적용 원칙

- PostgreSQL 기준으로 작성합니다.
- enum은 1차에서 varchar + check constraint 방식으로 시작합니다.
- 모든 주요 테이블은 `created_at`, `updated_at`을 둡니다.
- 예매 정합성의 핵심은 `schedule_seats` 테이블입니다.
- HOLD 만료 조회를 위해 `status`, `hold_expires_at` 인덱스를 둡니다.

## DDL 초안

```sql
create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    password varchar(255) not null,
    name varchar(100) not null,
    role varchar(20) not null,
    status varchar(20) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint chk_users_role check (role in ('USER', 'ADMIN')),
    constraint chk_users_status check (status in ('ACTIVE', 'LOCKED', 'DELETED'))
);

create table stations (
    id bigserial primary key,
    name varchar(100) not null,
    code varchar(50) not null unique,
    city varchar(100) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table routes (
    id bigserial primary key,
    name varchar(100) not null,
    origin_station_id bigint not null references stations(id),
    destination_station_id bigint not null references stations(id),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint chk_routes_different_stations check (origin_station_id <> destination_station_id),
    constraint uk_routes_name_origin_destination unique (name, origin_station_id, destination_station_id)
);

create table trains (
    id bigserial primary key,
    train_no varchar(50) not null unique,
    train_type varchar(50) not null,
    name varchar(100) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table train_schedules (
    id bigserial primary key,
    train_id bigint not null references trains(id),
    route_id bigint not null references routes(id),
    operation_date date not null,
    departure_time timestamp not null,
    arrival_time timestamp not null,
    status varchar(20) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint chk_train_schedules_status check (status in ('SCHEDULED', 'DELAYED', 'CANCELED', 'COMPLETED')),
    constraint chk_train_schedules_time check (arrival_time > departure_time)
);

create table cars (
    id bigserial primary key,
    train_id bigint not null references trains(id),
    car_no int not null,
    seat_count int not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint uk_cars_train_car_no unique (train_id, car_no),
    constraint chk_cars_seat_count check (seat_count > 0)
);

create table seats (
    id bigserial primary key,
    car_id bigint not null references cars(id),
    seat_no varchar(20) not null,
    seat_type varchar(30) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint uk_seats_car_seat_no unique (car_id, seat_no),
    constraint chk_seats_type check (seat_type in ('STANDARD', 'PRIORITY', 'WINDOW', 'AISLE'))
);

create table schedule_seats (
    id bigserial primary key,
    train_schedule_id bigint not null references train_schedules(id),
    seat_id bigint not null references seats(id),
    status varchar(20) not null,
    held_by_user_id bigint references users(id),
    hold_expires_at timestamp,
    version bigint not null default 0,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint uk_schedule_seats_schedule_seat unique (train_schedule_id, seat_id),
    constraint chk_schedule_seats_status check (status in ('AVAILABLE', 'HELD', 'RESERVED', 'BLOCKED'))
);

create table reservations (
    id bigserial primary key,
    reservation_no varchar(50) not null unique,
    user_id bigint not null references users(id),
    train_schedule_id bigint not null references train_schedules(id),
    status varchar(30) not null,
    total_amount numeric(12, 2) not null,
    created_at timestamp not null default now(),
    confirmed_at timestamp,
    canceled_at timestamp,
    expired_at timestamp,
    updated_at timestamp not null default now(),
    constraint chk_reservations_status check (status in ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELED', 'EXPIRED', 'PAYMENT_FAILED')),
    constraint chk_reservations_total_amount check (total_amount >= 0)
);

create table reservation_seats (
    id bigserial primary key,
    reservation_id bigint not null references reservations(id),
    schedule_seat_id bigint not null references schedule_seats(id),
    price numeric(12, 2) not null,
    created_at timestamp not null default now(),
    constraint chk_reservation_seats_price check (price >= 0)
);

create table payments (
    id bigserial primary key,
    reservation_id bigint not null unique references reservations(id),
    payment_no varchar(50) not null unique,
    amount numeric(12, 2) not null,
    status varchar(20) not null,
    requested_at timestamp not null,
    approved_at timestamp,
    failed_at timestamp,
    canceled_at timestamp,
    expired_at timestamp,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint chk_payments_status check (status in ('READY', 'SUCCESS', 'FAILED', 'CANCELED', 'EXPIRED')),
    constraint chk_payments_amount check (amount >= 0)
);

create table operation_logs (
    id bigserial primary key,
    actor_id bigint references users(id),
    action varchar(50) not null,
    target_type varchar(50) not null,
    target_id bigint,
    detail text,
    ip_address varchar(100),
    created_at timestamp not null default now()
);
```

## 인덱스 초안

```sql
create index idx_stations_name on stations(name);
create index idx_stations_city on stations(city);

create index idx_train_schedules_route_date on train_schedules(route_id, operation_date);
create index idx_train_schedules_date_departure on train_schedules(operation_date, departure_time);
create index idx_train_schedules_status on train_schedules(status);

create index idx_schedule_seats_schedule_status on schedule_seats(train_schedule_id, status);
create index idx_schedule_seats_status_hold_expires_at on schedule_seats(status, hold_expires_at);
create index idx_schedule_seats_held_by_user on schedule_seats(held_by_user_id);

create index idx_reservations_user_created_at on reservations(user_id, created_at desc);
create index idx_reservations_status_created_at on reservations(status, created_at desc);
create index idx_reservations_schedule on reservations(train_schedule_id);

create index idx_reservation_seats_reservation on reservation_seats(reservation_id);
create index idx_reservation_seats_schedule_seat on reservation_seats(schedule_seat_id);

create index idx_payments_status_created_at on payments(status, created_at desc);

create index idx_operation_logs_actor_created_at on operation_logs(actor_id, created_at desc);
create index idx_operation_logs_action_created_at on operation_logs(action, created_at desc);
create index idx_operation_logs_target on operation_logs(target_type, target_id);
```

## HOLD 만료 Scheduler와 관련된 DB 구조

Scheduler는 다음 조건을 자주 조회합니다.

```sql
select *
from schedule_seats
where status = 'HELD'
  and hold_expires_at < now();
```

그래서 아래 인덱스가 중요합니다.

```sql
create index idx_schedule_seats_status_hold_expires_at
on schedule_seats(status, hold_expires_at);
```

이 인덱스가 있으면 만료된 HELD 좌석을 빠르게 찾을 수 있습니다.

## 구현 전 다시 확인할 부분

- 가격을 어디서 계산할지 결정해야 합니다.
- `seat_type`을 더 단순화할지 결정해야 합니다.
- 결제 완료 후 취소를 환불 상태로 분리할지 결정해야 합니다.
- Scheduler가 한 번에 몇 건씩 처리할지 결정해야 합니다.
- 대량 만료 처리 시 `limit`와 별도 배치 전략을 적용할지 검토해야 합니다.
