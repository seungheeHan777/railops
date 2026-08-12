# 프론트 관리자/운행편 검색 구현 노트

이 문서는 현재 백엔드에 구현된 기능에 맞춰 React 프론트엔드를 확장한 내용을 기록합니다.

## 구현 범위

기존 프론트는 회원가입, 로그인, 마이페이지, 역 조회, 역 관리 중심이었습니다.

이번 작업으로 다음 화면을 추가했습니다.

```text
사용자 운행편 검색
사용자 운행편 상세 조회
관리자 노선 관리
관리자 열차 관리
관리자 객차 관리
관리자 물리 좌석 관리
관리자 운행편 관리
관리자 운행편 상태 변경
```

## 주요 화면

### 운행편 검색

```text
GET /api/train-schedules?from=SEOUL&to=BUSAN&date=2026-08-01
GET /api/train-schedules/{scheduleId}
```

사용자는 출발역 코드, 도착역 코드, 운행일로 운행편을 검색할 수 있습니다.

### 관리자 콘솔

관리자 콘솔은 탭 구조로 구성했습니다.

```text
역
노선
열차
객차
좌석
운행편
```

각 탭에서 현재 백엔드가 지원하는 CRUD API를 호출합니다.

## 추가된 프론트 API client

```text
frontend/src/api/railops.ts
```

포함된 API client:

```text
Route CRUD
Train CRUD
Car CRUD
Seat CRUD
TrainSchedule 검색/상세/관리자 CRUD/상태 변경
```

## 타입 확장

```text
frontend/src/types/api.ts
```

추가된 주요 타입:

```text
Route
Train
Car
Seat
SeatType
TrainSchedule
TrainScheduleStatus
TrainScheduleSearchResult
각 Payload 타입
```

## 검증

실행 명령:

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run build
```

검증 결과:

```text
빌드 성공
```

개발 서버:

```text
http://127.0.0.1:5173
```

## 이후 프론트 작업

ScheduleSeat 좌석 지도 화면은 `docs/30-schedule-seat-implementation-notes.md`에 기록했습니다.

아직 남은 화면은 좌석 선택 이후의 예매 흐름입니다.

```text
좌석 선택
예약 생성
결제 시뮬레이션
내 예매 목록/상세/취소
```
