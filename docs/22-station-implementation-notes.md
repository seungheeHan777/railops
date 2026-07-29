# Station 구현 노트

이 문서는 RailOps의 역 Station 도메인과 API 구현 내용을 기록합니다.

## 구현 범위

역 정보는 노선 Route, 운행편 TrainSchedule, 열차 검색의 기반 데이터입니다. 이번 단계에서는 공개 조회 API와 관리자 CRUD API를 함께 구현했습니다.

구현된 API:

```text
GET    /api/stations
GET    /api/stations/search?keyword=서울
POST   /api/admin/stations
GET    /api/admin/stations
GET    /api/admin/stations/{stationId}
PATCH  /api/admin/stations/{stationId}
DELETE /api/admin/stations/{stationId}
```

## 생성된 패키지

```text
com.railops.station.domain
com.railops.station.repository
com.railops.station.dto
com.railops.station.service
com.railops.station.controller
```

## Station 엔티티

`Station`은 `stations` 테이블에 매핑됩니다.

주요 필드:

```text
id
name
code
city
createdAt
updatedAt
```

설계 규칙:

- `code`는 unique입니다.
- `code`는 저장 전에 대문자로 정규화합니다.
- 예: `seoul` 입력 -> `SEOUL` 저장

## DTO

사용한 DTO:

```text
StationCreateRequest
StationUpdateRequest
StationResponse
```

Request DTO에는 validation을 적용했습니다.

검증 규칙:

```text
name: 필수, 최대 100자
code: 필수, 영문/숫자/_/- 허용, 최대 50자
city: 필수, 최대 100자
```

## Service 정책

### 생성

```text
1. code 대문자 정규화
2. code 중복 확인
3. Station 생성
4. 저장 후 StationResponse 반환
```

중복 코드이면 `DUPLICATE_STATION_CODE` 에러를 반환합니다.

### 수정

```text
1. stationId로 Station 조회
2. 없으면 STATION_NOT_FOUND
3. 변경할 code 정규화
4. 다른 Station이 같은 code를 쓰는지 확인
5. Station 수정
6. StationResponse 반환
```

### 삭제

```text
1. stationId로 Station 조회
2. 없으면 STATION_NOT_FOUND
3. Station 삭제
```

현재는 실제 Route 참조 여부를 확인하지 않습니다. Route 구현 후에는 사용 중인 Station 삭제 정책을 다시 정해야 합니다.

## Controller 분리

공개 조회 API:

```text
StationController
/api/stations
```

관리자 API:

```text
AdminStationController
/api/admin/stations
```

권한 제어는 `SecurityConfig`에서 `/api/admin/**`를 ADMIN 권한으로 제한합니다.

## 테스트

추가된 테스트:

```text
StationServiceTest
```

검증 내용:

- Station 생성 성공
- 중복 code 생성 실패
- 빈 검색어는 전체 목록 반환
- 없는 Station 조회 실패
- Station 수정 성공
- Station 삭제 시 repository delete 호출

실행 명령:

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat test
```

검증 결과:

```text
BUILD SUCCESSFUL
```

## 다음 작업

Station 다음에는 Route를 구현합니다. Route는 출발역과 도착역 Station을 참조하므로, Station 구현이 선행되어야 합니다.

다음 구현 후보:

```text
Route 도메인
Route 관리자 CRUD
Route 생성 시 origin/destination Station 존재 검증
origin과 destination이 같은 경우 거부
```