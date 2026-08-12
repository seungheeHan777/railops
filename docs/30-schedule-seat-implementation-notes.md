# ScheduleSeat 구현 노트

이 문서는 운행편별 좌석 상태를 관리하는 ScheduleSeat 백엔드/API와 프론트 좌석 맵 구현 내용을 기록합니다.

## 구현 범위

`Seat`는 열차 객차 안의 물리 좌석이고, `ScheduleSeat`는 특정 운행편에서 그 좌석이 어떤 상태인지 나타내는 좌석 인스턴스입니다.

```text
TrainSchedule + Seat -> ScheduleSeat
```

운행편이 생성되면 해당 열차에 등록된 물리 좌석을 기준으로 `ScheduleSeat`가 자동 생성됩니다.

## 상태 값

```text
AVAILABLE: 선택 가능한 좌석
HELD: 결제 전 임시 점유 좌석, HOLD API 구현 후 사용
RESERVED: 결제 완료 후 예약 확정 좌석, Reservation 구현 후 사용
BLOCKED: 관리자가 판매/선택을 막은 좌석
```

현재 구현된 상태 전이는 관리자 BLOCK/UNBLOCK입니다.

```text
AVAILABLE -> BLOCKED
BLOCKED -> AVAILABLE
```

`HELD`, `RESERVED` 전이는 다음 작업인 Reservation/HOLD/Payment 구현에서 연결합니다.

## 구현된 API

### 사용자 운행편 좌석 조회

```http
GET /api/train-schedules/{scheduleId}/seats
```

로그인 없이 조회 가능합니다. 응답은 객차별 좌석 목록입니다.

```json
{
  "scheduleId": 1,
  "cars": [
    {
      "carId": 10,
      "carNo": 1,
      "seats": [
        {
          "scheduleSeatId": 100,
          "seatId": 20,
          "seatNo": "12A",
          "seatType": "WINDOW",
          "status": "AVAILABLE",
          "holdExpiresAt": null
        }
      ]
    }
  ]
}
```

### 관리자 좌석 BLOCK/UNBLOCK

```http
PATCH /api/admin/schedule-seats/{scheduleSeatId}/block
PATCH /api/admin/schedule-seats/{scheduleSeatId}/unblock
```

ADMIN 권한이 필요합니다. 응답은 변경된 `ScheduleSeatResponse`입니다.

## 생성된 주요 클래스

```text
ScheduleSeat
ScheduleSeatStatus
ScheduleSeatRepository
ScheduleSeatResponse
CarSeatResponse
ScheduleSeatMapResponse
ScheduleSeatService
ScheduleSeatController
AdminScheduleSeatController
ScheduleSeatServiceTest
```

## TrainSchedule 생성과의 연결

`TrainScheduleService.create()`는 운행편을 저장한 뒤 `ScheduleSeatService.createAvailableSeatsForSchedule()`을 호출합니다.

```text
1. 운행편 시간/노선 검증
2. TrainSchedule 저장
3. 해당 Train의 물리 Seat 목록 조회
4. 각 Seat를 ScheduleSeat(AVAILABLE)로 생성
```

이미 같은 운행편에 ScheduleSeat가 있으면 중복 생성을 건너뜁니다.

## 프론트 구현

사용자 운행편 검색 화면에서 `상세` 버튼을 누르면 운행편 상세와 좌석 맵을 함께 조회합니다.

관리자 운행편 탭에서는 운행편을 선택하고 좌석 맵을 조회한 뒤, 좌석별 `BLOCK` 또는 `UNBLOCK`을 실행할 수 있습니다.

수정된 파일:

```text
frontend/src/App.tsx
frontend/src/api/railops.ts
frontend/src/types/api.ts
frontend/src/styles.css
```

## 검증

백엔드 테스트:

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat test
```

결과:

```text
BUILD SUCCESSFUL
```

프론트 빌드:

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run build
```

결과:

```text
빌드 성공
```

## 다음 작업

Reservation HOLD 구현 내용은 `docs/31-reservation-hold-implementation-notes.md`에 기록했습니다.

다음은 결제 시뮬레이션과 예매 확정 흐름입니다.