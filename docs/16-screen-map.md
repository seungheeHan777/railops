# 화면 구성

RailOps 프론트엔드는 백엔드 중심 프로젝트의 흐름을 검증하기 위한 최소 UI입니다. 화면은 메인, 사용자, 관리자 영역으로 나눕니다.

## 화면 그룹

```text
공통
- 메인 페이지
- 로그인
- 회원가입

사용자
- 마이페이지
- 열차 검색
- 열차 상세
- 객차/좌석 선택
- 예매 확인
- 가상 결제
- 내 예매 목록
- 예매 상세

관리자
- 관리자 대시보드
- 역 관리
- 노선 관리
- 열차 관리
- 운행 편성 관리
- 좌석 관리
- 전체 예매 조회
- 운영 로그 조회
```

## 기본 라우팅 초안

```text
/                         메인 페이지
/signup                   회원가입
/login                    로그인
/me                       마이페이지
/schedules                열차 검색
/schedules/:scheduleId    열차 상세
/schedules/:scheduleId/seats 좌석 선택
/reservations/confirm     예매 확인
/payments/:paymentId      가상 결제
/my/reservations          내 예매 목록
/my/reservations/:id      예매 상세
/admin                    관리자 대시보드
/admin/stations           역 관리
/admin/routes             노선 관리
/admin/trains             열차 관리
/admin/schedules          운행 편성 관리
/admin/seats              좌석 관리
/admin/reservations       전체 예매 조회
/admin/logs               운영 로그 조회
```

## 메인 페이지

역할:

- 서비스 진입점
- 열차 검색으로 이동
- 로그인 상태에 따른 메뉴 제공

포함 요소:

- 출발역 선택
- 도착역 선택
- 날짜 선택
- 검색 버튼
- 로그인/회원가입 또는 마이페이지/로그아웃 메뉴

## 사용자 좌석 선택 화면

역할:

- 객차 선택
- 좌석 상태 확인
- AVAILABLE 좌석 선택
- 예매하기 요청

좌석 상태 표시:

```text
AVAILABLE  선택 가능
HELD       선택 불가, 임시 점유
RESERVED   선택 불가, 예매 완료
BLOCKED    선택 불가, 판매 중지
```

## 관리자 화면

관리자 화면은 기능 검증을 위한 실용적인 CRUD UI로 구성합니다. 화려한 대시보드보다 목록, 검색, 등록/수정 폼, 상태 변경 버튼을 우선합니다.

관리자 핵심 작업:

- 기본 철도 데이터 등록
- 운행 편성 생성
- 운행편 좌석 생성
- 좌석 판매 중지와 해제
- 예매와 운영 로그 확인
