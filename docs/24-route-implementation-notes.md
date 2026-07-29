# Route 구현 노트

이 문서는 RailOps의 노선 Route 도메인과 관리자 API 구현 내용을 기록합니다.

## 구현 범위

Route는 출발역과 도착역을 연결하는 철도 노선입니다. 이후 TrainSchedule이 어떤 구간을 운행하는지 표현할 때 사용합니다.

구현된 API:

```text
POST   /api/admin/routes
GET    /api/admin/routes
GET    /api/admin/routes/{routeId}
PATCH  /api/admin/routes/{routeId}
DELETE /api/admin/routes/{routeId}
```

모든 Route API는 관리자 API입니다.

```text
/api/admin/routes/** -> ADMIN 권한 필요
```

## 생성된 패키지

```text
com.railops.route.domain
com.railops.route.repository
com.railops.route.dto
com.railops.route.service
com.railops.route.controller
```

## Route 엔티티

`Route`는 `routes` 테이블에 매핑됩니다.

주요 필드:

```text
id
name
originStation
destinationStation
createdAt
updatedAt
```

`originStation`과 `destinationStation`은 `Station` 엔티티를 참조합니다.

```text
Route N -> 1 Station(origin)
Route N -> 1 Station(destination)
```

## DTO

사용한 DTO:

```text
RouteCreateRequest
RouteUpdateRequest
RouteResponse
```

생성/수정 요청 예시:

```json
{
  "name": "경부선",
  "originStationId": 1,
  "destinationStationId": 2
}
```

응답 예시:

```json
{
  "id": 1,
  "name": "경부선",
  "originStationId": 1,
  "originStationName": "서울",
  "originStationCode": "SEOUL",
  "destinationStationId": 2,
  "destinationStationName": "부산",
  "destinationStationCode": "BUSAN"
}
```

## Service 정책

### 생성

```text
1. 출발역 ID와 도착역 ID가 다른지 확인
2. 같은 이름/출발역/도착역 조합의 노선이 이미 있는지 확인
3. 출발역 Station 존재 확인
4. 도착역 Station 존재 확인
5. Route 생성
6. 저장 후 RouteResponse 반환
```

실패 케이스:

```text
출발역과 도착역이 같음 -> INVALID_ROUTE_STATIONS
중복 노선 -> DUPLICATE_ROUTE
Station 없음 -> STATION_NOT_FOUND
```

### 수정

```text
1. routeId로 Route 조회
2. 없으면 ROUTE_NOT_FOUND
3. 출발역 ID와 도착역 ID가 다른지 확인
4. 자기 자신을 제외하고 중복 노선 확인
5. 출발역/도착역 Station 존재 확인
6. Route 수정
7. RouteResponse 반환
```

### 삭제

```text
1. routeId로 Route 조회
2. 없으면 ROUTE_NOT_FOUND
3. Route 삭제
```

현재는 TrainSchedule이 Route를 참조하는지 확인하지 않습니다. TrainSchedule 구현 후 사용 중인 Route 삭제 정책을 다시 결정합니다.

## 에러 코드 추가

추가된 에러 코드:

```text
DUPLICATE_ROUTE
INVALID_ROUTE_STATIONS
```

## 테스트

추가된 테스트:

```text
RouteServiceTest
```

검증 내용:

- Route 생성 성공
- 출발역/도착역이 같으면 생성 실패
- 중복 노선 생성 실패
- 존재하지 않는 Station으로 생성 실패
- Route 목록 조회
- 존재하지 않는 Route 조회 실패
- Route 수정 성공
- Route 삭제 시 repository delete 호출

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

Route 다음에는 Train 도메인을 구현합니다.

다음 구현 후보:

```text
Train 엔티티
TrainRepository
TrainCreateRequest / TrainUpdateRequest / TrainResponse
AdminTrainController
TrainService
TrainServiceTest
```