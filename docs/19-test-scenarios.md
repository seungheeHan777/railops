# 테스트 시나리오

이 문서는 RailOps에서 반드시 검증해야 하는 테스트 시나리오를 정리합니다. 구현 전에 테스트 대상을 먼저 정리하면, 기능을 만들 때 어떤 동작을 보장해야 하는지 명확해집니다.

## 테스트 우선순위

1. 좌석 중복 예매 방지
2. 좌석 HOLD 만료 처리
3. 결제 성공/실패/취소 상태 전이
4. 권한 제어
5. 관리자 CRUD
6. 조회 API와 캐시

## 인증/인가 테스트

### 회원가입 성공

조건:

```text
새 이메일, 유효한 비밀번호, 이름 입력
```

예상 결과:

```text
User 생성
role = USER
status = ACTIVE
```

### 중복 이메일 회원가입 실패

예상 결과:

```text
DUPLICATE_EMAIL 에러
```

### 로그인 성공

예상 결과:

```text
accessToken 반환
사용자 요약 정보 반환
```

### 관리자 API 일반 사용자 접근 실패

예상 결과:

```text
ACCESS_DENIED 에러
```

## 좌석 조회 테스트

### 운행편 좌석 조회 성공

조건:

```text
TrainSchedule과 ScheduleSeat가 존재함
```

예상 결과:

```text
객차별 좌석 상태 반환
AVAILABLE, HELD, RESERVED, BLOCKED 구분 표시
```

## 좌석 HOLD 테스트

### AVAILABLE 좌석 HOLD 성공

조건:

```text
좌석 상태 = AVAILABLE
사용자 로그인 상태
운행편 상태 = SCHEDULED
```

예상 결과:

```text
ScheduleSeat 상태 = HELD
held_by_user_id = 요청 사용자 ID
hold_expires_at = 현재 시간 + 10분
Reservation 상태 = PENDING_PAYMENT
Payment 상태 = READY
```

### HELD 좌석 HOLD 실패

예상 결과:

```text
SEAT_ALREADY_HELD 또는 SEAT_NOT_AVAILABLE 에러
기존 HOLD 상태 유지
```

### RESERVED 좌석 HOLD 실패

예상 결과:

```text
SEAT_ALREADY_RESERVED 또는 SEAT_NOT_AVAILABLE 에러
```

### BLOCKED 좌석 HOLD 실패

예상 결과:

```text
SEAT_BLOCKED 에러
```

### 여러 좌석 중 하나라도 실패하면 전체 실패

조건:

```text
요청 좌석 = [AVAILABLE, RESERVED]
```

예상 결과:

```text
Reservation 생성 안 됨
Payment 생성 안 됨
AVAILABLE 좌석도 HELD로 바뀌지 않음
```

## 결제 테스트

### 결제 성공

조건:

```text
Payment 상태 = READY
Reservation 상태 = PENDING_PAYMENT
ScheduleSeat 상태 = HELD
hold_expires_at이 현재 시간보다 미래
```

예상 결과:

```text
Payment 상태 = SUCCESS
Reservation 상태 = CONFIRMED
ScheduleSeat 상태 = RESERVED
OperationLog 저장
```

### 결제 실패

예상 결과:

```text
Payment 상태 = FAILED
Reservation 상태 = PAYMENT_FAILED
ScheduleSeat 상태 = AVAILABLE
held_by_user_id = null
hold_expires_at = null
```

### 결제 취소

예상 결과:

```text
Payment 상태 = CANCELED
Reservation 상태 = CANCELED
ScheduleSeat 상태 = AVAILABLE
```

### 만료된 HOLD 결제 성공 실패

조건:

```text
hold_expires_at이 현재 시간보다 과거
```

예상 결과:

```text
Payment 상태 = EXPIRED
Reservation 상태 = EXPIRED
ScheduleSeat 상태 = AVAILABLE
HOLD_EXPIRED 에러
```

## Scheduler 테스트

### 만료된 HOLD 자동 해제

조건:

```text
ScheduleSeat 상태 = HELD
hold_expires_at < now()
Reservation 상태 = PENDING_PAYMENT
Payment 상태 = READY
```

예상 결과:

```text
ScheduleSeat 상태 = AVAILABLE
Reservation 상태 = EXPIRED
Payment 상태 = EXPIRED
OperationLog SEAT_HOLD_EXPIRED 저장
```

### 만료되지 않은 HOLD는 유지

조건:

```text
hold_expires_at > now()
```

예상 결과:

```text
상태 변경 없음
```

## 동시성 테스트

### 같은 좌석 동시 HOLD 요청

조건:

```text
두 사용자가 같은 scheduleSeatId로 동시에 POST /api/reservations/hold 요청
```

예상 결과:

```text
한 요청만 성공
다른 요청은 실패
최종 ScheduleSeat는 HELD 하나만 존재
Reservation도 성공 사용자 것만 생성
```

### 여러 좌석 동시 HOLD 요청

조건:

```text
A 사용자 요청 = [10, 11]
B 사용자 요청 = [11, 10]
```

예상 결과:

```text
좌석 ID 오름차순으로 lock
데드락 없이 하나만 성공 또는 충돌 실패
```

## 관리자 테스트

### 좌석 BLOCK

조건:

```text
관리자가 AVAILABLE 좌석을 BLOCK 요청
```

예상 결과:

```text
ScheduleSeat 상태 = BLOCKED
OperationLog ADMIN_BLOCK_SEAT 저장
```

### BLOCKED 좌석 예매 실패

예상 결과:

```text
SEAT_BLOCKED 에러
```

### 좌석 UNBLOCK

예상 결과:

```text
ScheduleSeat 상태 = AVAILABLE
OperationLog ADMIN_UNBLOCK_SEAT 저장
```

## 테스트 도구

```text
JUnit5
Spring Boot Test
Testcontainers PostgreSQL
ExecutorService 또는 CountDownLatch 기반 동시성 테스트
k6 API 부하 테스트
```
