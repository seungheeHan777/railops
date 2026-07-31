# TrainSchedule 구현 노트

이 문서는 RailOps의 운행편 TrainSchedule 도메인과 관리자/사용자 조회 API 구현 내용을 기록합니다.

## 구현 범위

TrainSchedule은 특정 열차가 특정 노선을 특정 날짜와 시간에 운행하는 편성 정보입니다.

```text
Train + Route + operationDate + departureTime + arrivalTime
```

예시:

```text
KTX-101
경부선 서울 -> 부산
2026-08-01 09:00 출발
2026-08-01 11:40 도착
상태 SCHEDULED
```

## 구현된 API

### 사용자 조회 API

```text
GET /api/train-schedules?from=SEOUL&to=BUSAN&date=2026-08-01
GET /api/train-schedules/{scheduleId}
```

`GET /api/train-schedules`는 로그인 없이 조회할 수 있습니다. 검색 결과는 `SCHEDULED`, `DELAYED` 상태만 반환합니다.

### 관리자 API

```text
POST   /api/admin/train-schedules
GET    /api/admin/train-schedules
GET    /api/admin/train-schedules/{scheduleId}
PATCH  /api/admin/train-schedules/{scheduleId}
PATCH  /api/admin/train-schedules/{scheduleId}/status
DELETE /api/admin/train-schedules/{scheduleId}
```

## 생성된 주요 클래스

```text
TrainSchedule
TrainScheduleStatus
TrainScheduleRepository
TrainScheduleCreateRequest
TrainScheduleUpdateRequest
TrainScheduleStatusUpdateRequest
TrainScheduleResponse
TrainScheduleSearchResponse
TrainScheduleService
TrainScheduleController
AdminTrainScheduleController
TrainScheduleServiceTest
```

## 상태 값

```text
SCHEDULED
DELAYED
CANCELED
COMPLETED
```

## Service 정책

### 생성

```text
1. operationDate, departureTime, arrivalTime 검증
2. 같은 Train의 기존 운행편과 시간이 겹치는지 확인
3. Train 존재 확인
4. Route 존재 확인
5. TrainSchedule 생성
6. 기본 상태 SCHEDULED로 저장
```

실패 케이스:

```text
Train 없음 -> TRAIN_NOT_FOUND
Route 없음 -> ROUTE_NOT_FOUND
도착 시간이 출발 시간보다 빠르거나 같음 -> INVALID_SCHEDULE_TIME
operationDate와 departureTime 날짜가 다름 -> INVALID_SCHEDULE_TIME
같은 열차의 운행 시간이 겹침 -> TRAIN_SCHEDULE_CONFLICT
```

### 수정

```text
1. scheduleId로 TrainSchedule 조회
2. 없으면 SCHEDULE_NOT_FOUND
3. 시간 검증
4. 자기 자신을 제외하고 같은 Train의 시간 겹침 확인
5. Route 존재 확인
6. Route와 시간 정보 수정
```

현재 수정 API에서는 Train 자체는 바꾸지 않습니다. 열차를 바꾸는 기능은 운행편 생성 후 좌석 상태 생성 정책과 연결되므로 후순위로 둡니다.

### 상태 변경

```text
PATCH /api/admin/train-schedules/{scheduleId}/status
```

상태만 별도로 바꿉니다. 예매 기능이 들어오면 `CANCELED`, `COMPLETED` 상태에서의 예매 가능 여부를 더 엄격하게 검증합니다.

### 삭제

현재는 Reservation이 없으므로 삭제를 허용합니다. Reservation 구현 후에는 이미 예매가 있는 운행편 삭제를 막는 정책이 필요합니다.

## 사용자 기능과의 관계

이번 작업부터 사용자 기능의 열차 검색 기반이 생겼습니다.

현재 사용자 관련 구현 상태:

```text
완료: 회원가입
완료: 로그인
완료: JWT 인증
완료: 내 정보 조회
완료: 역 조회
완료: 운행편 검색
완료: 운행편 상세 조회
미완료: 운행편 좌석 조회
미완료: 좌석 HOLD
미완료: 예매 생성
미완료: 결제 시뮬레이션
미완료: 내 예매 목록/상세/취소
```

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

다음은 ScheduleSeat입니다.

`ScheduleSeat`는 특정 운행편에서 특정 물리 좌석이 어떤 상태인지 관리합니다.

```text
TrainSchedule + Seat -> ScheduleSeat
```

상태는 다음 값을 사용합니다.

```text
AVAILABLE
HELD
RESERVED
BLOCKED
```

ScheduleSeat가 구현되면 그 다음부터 좌석 HOLD와 Reservation 기능으로 넘어갈 수 있습니다.