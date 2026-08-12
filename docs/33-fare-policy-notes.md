# 가격 정책 노트

이 문서는 RailOps의 가격 정책 결정을 기록합니다.

## 1차 결정

1차 구현에서는 거리, 노선, 열차 종류, 좌석 타입을 구분하지 않고 모든 좌석 가격을 동일하게 둡니다.

```text
모든 좌석 가격 = 단일 기본요금
```

현재 코드의 `ReservationSeat.price`, `Reservation.totalAmount`, `Payment.amount`는 나중에 이 단일 기본요금 계산 결과로 채웁니다.

## 왜 이렇게 시작하는가

현재 프로젝트의 핵심 검증 대상은 가격 알고리즘이 아니라 예매 정합성입니다.

```text
좌석 HOLD
HOLD 만료
중복 예매 방지
결제 성공/실패/취소
예약 목록/상세/취소
```

가격 정책을 복잡하게 먼저 만들면 예매 흐름 구현보다 설계 범위가 커집니다. 따라서 1차는 단일 기본요금으로 고정하고, 예매 흐름이 안정된 뒤 확장합니다.

## 나중에 추가할 가격 기준 테이블 후보

처음에는 아래처럼 단순한 테이블 하나로 충분합니다.

```text
fare_policies
- id
- name
- base_price
- active
- created_at
- updated_at
```

또는 더 단순하게 전역 설정 테이블로 둘 수도 있습니다.

```text
app_settings
- key
- value
```

예:

```text
key = DEFAULT_SEAT_PRICE
value = 50000
```

## 확장 후보

나중에 가격을 세분화하면 아래 순서로 확장합니다.

```text
1. 단일 기본요금
2. 좌석 타입별 가산금
3. 노선별 기본요금
4. 운행편별 override 가격
5. 할인/쿠폰/프로모션
```

## ReservationSeat.price를 유지하는 이유

가격 기준 테이블이 생겨도 `ReservationSeat.price`는 유지합니다.

이유는 `ReservationSeat.price`가 현재 가격 정책이 아니라 예약 당시 확정된 거래 가격이기 때문입니다.

예를 들어 기본요금이 나중에 변경되어도 과거 예약의 결제 금액은 바뀌면 안 됩니다.

```text
가격 정책 테이블 = 현재 계산 기준
ReservationSeat.price = 예약 당시 확정 가격 기록
```

## 다음 구현 시 할 일

```text
1. 단일 기본요금 저장 위치 결정
2. HOLD 생성 시 좌석 수 * 기본요금으로 totalAmount 계산
3. ReservationSeat.price에 좌석별 기본요금 저장
4. Payment.amount에 Reservation.totalAmount 저장
5. 문서와 테스트에서 임시 amount=0 제거
```