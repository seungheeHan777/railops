# Train 구현 노트

이 문서는 RailOps의 열차 Train 도메인과 관리자 API 구현 내용을 기록합니다.

## 구현 범위

Train은 실제 열차 편성의 기본 정보입니다. 이후 Car/Seat가 Train에 연결되고, TrainSchedule이 특정 날짜와 노선에 Train을 배정합니다.

구현된 API:

```text
POST   /api/admin/trains
GET    /api/admin/trains
GET    /api/admin/trains/{trainId}
PATCH  /api/admin/trains/{trainId}
DELETE /api/admin/trains/{trainId}
```

모든 Train API는 관리자 API입니다.

```text
/api/admin/trains/** -> ADMIN 권한 필요
```

## 생성된 패키지

```text
com.railops.train.domain
com.railops.train.repository
com.railops.train.dto
com.railops.train.service
com.railops.train.controller
```

## Train 엔티티

`Train`은 `trains` 테이블에 매핑됩니다.

주요 필드:

```text
id
trainNo
trainType
name
createdAt
updatedAt
```

현재 `trainNo`는 고유값입니다. 예시는 `KTX-101`, `ITX-201`처럼 관리합니다.

## DTO

사용한 DTO:

```text
TrainCreateRequest
TrainUpdateRequest
TrainResponse
```

생성/수정 요청 예시:

```json
{
  "trainNo": "KTX-101",
  "trainType": "KTX",
  "name": "KTX 101"
}
```

응답 예시:

```json
{
  "id": 1,
  "trainNo": "KTX-101",
  "trainType": "KTX",
  "name": "KTX 101"
}
```

## Service 정책

### 생성

```text
1. trainNo를 trim + upper-case로 정규화
2. 같은 trainNo가 이미 있는지 확인
3. Train 생성
4. 저장 후 TrainResponse 반환
```

실패 케이스:

```text
중복 열차 번호 -> DUPLICATE_TRAIN_NO
```

### 수정

```text
1. trainId로 Train 조회
2. 없으면 TRAIN_NOT_FOUND
3. trainNo를 trim + upper-case로 정규화
4. 자기 자신을 제외하고 중복 trainNo 확인
5. Train 수정
6. TrainResponse 반환
```

### 삭제

```text
1. trainId로 Train 조회
2. 없으면 TRAIN_NOT_FOUND
3. Train 삭제
```

현재는 Car나 TrainSchedule이 Train을 참조하는지 확인하지 않습니다. Car/Seat와 TrainSchedule 구현 후 사용 중인 Train 삭제 정책을 다시 결정합니다.

## 에러 코드 추가

추가된 에러 코드:

```text
DUPLICATE_TRAIN_NO
```

기존 에러 코드 중 `TRAIN_NOT_FOUND`를 조회/수정/삭제 실패에 사용합니다.

## 테스트

추가된 테스트:

```text
TrainServiceTest
```

검증 내용:

- Train 생성 성공
- 중복 trainNo 생성 실패
- Train 목록 조회
- 존재하지 않는 Train 조회 실패
- Train 수정 성공
- 수정 시 중복 trainNo 실패
- Train 삭제 시 repository delete 호출

실행 명령:

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat clean test
```

```text
BUILD SUCCESSFUL
```

## 다음 작업

Train 다음에는 객차와 좌석 도메인을 구현합니다.

다음 구현 후보:

```text
Car 엔티티
Seat 엔티티
CarRepository / SeatRepository
Car/Seat 관리자 API 또는 Train 하위 관리 API
Car/Seat 단위 테스트
```