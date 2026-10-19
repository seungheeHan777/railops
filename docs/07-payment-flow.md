# 결제 흐름

RailOps의 결제는 실제 PG 연동이 아닌 가상 결제입니다. 그러나 결제 상태, 예매 상태, 좌석 상태 전이는 실제 서비스처럼 백엔드에서 일관되게 처리합니다.

## 상태 모델

Payment 상태:

```text
READY
SUCCESS
FAILED
CANCELED
EXPIRED
REFUNDED
```

Reservation 상태:

```text
PENDING_PAYMENT
CONFIRMED
CANCELED
EXPIRED
PAYMENT_FAILED
```

ScheduleSeat 상태:

```text
AVAILABLE
HELD
RESERVED
BLOCKED
```

## 결제 성공

```text
Payment: READY → SUCCESS
Reservation: PENDING_PAYMENT → CONFIRMED
ScheduleSeat: HELD → RESERVED
```

처리 조건:

- Payment가 READY 상태여야 합니다.
- Reservation이 PENDING_PAYMENT 상태여야 합니다.
- 연결된 모든 ScheduleSeat가 HELD 상태여야 합니다.
- 모든 ScheduleSeat의 `held_by_user_id`가 결제 요청 사용자와 같아야 합니다.
- 모든 ScheduleSeat의 `hold_expires_at`이 현재 시각보다 미래여야 합니다.

## 결제 실패

```text
Payment: READY → FAILED
Reservation: PENDING_PAYMENT → PAYMENT_FAILED
ScheduleSeat: HELD → AVAILABLE
```

추가 처리:

```text
held_by_user_id = null
hold_expires_at = null
```

## 결제 취소

```text
Payment: READY → CANCELED
Reservation: PENDING_PAYMENT → CANCELED
ScheduleSeat: HELD → AVAILABLE
```

결제 화면에서 사용자가 취소 버튼을 누른 경우입니다.

## 결제 시간 초과

```text
Payment: READY → EXPIRED
Reservation: PENDING_PAYMENT → EXPIRED
ScheduleSeat: HELD → AVAILABLE
```

시간 초과는 Spring Scheduler 또는 결제 API의 만료 재확인 로직에서 처리합니다.

## 시퀀스 다이어그램

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant A as Spring API
    participant DB as PostgreSQL

    U->>F: 결제 성공 버튼 클릭
    F->>A: POST /api/payments/{paymentId}/simulate-success
    A->>DB: Payment 조회
    A->>DB: Reservation 조회
    A->>DB: ScheduleSeat 조회 및 Lock
    A->>A: 상태와 만료 시간 검증
    A->>DB: Payment SUCCESS
    A->>DB: Reservation CONFIRMED
    A->>DB: ScheduleSeat RESERVED
    A->>DB: OperationLog 저장
    DB-->>A: Commit
    A-->>F: 결제 성공 응답
    F-->>U: 예매 완료 화면 표시
```

## 결제 완료 후 예매 취소

1차 구현에서는 결제 완료 후 환불 모델을 단순화합니다.

2026-10-18 구현 정책: 출발 전 전액 가상 환불. 부분 취소는 지원하지 않습니다.

```text
Reservation: CONFIRMED → CANCELED
ScheduleSeat: RESERVED → AVAILABLE
Payment: SUCCESS → REFUNDED
refundedAmount = 기존 결제 금액, refundedAt = 처리 시각
```

같은 취소 요청을 반복해도 기존 결과만 반환합니다. 실제 결제사 환불은 연동하지 않았습니다.

## 운영 로그

결제 흐름에서는 다음 이벤트를 저장합니다.

```text
PAYMENT_SUCCESS
PAYMENT_FAILED
PAYMENT_CANCELED
PAYMENT_EXPIRED
RESERVATION_CONFIRMED
```
