# 백엔드 구현 계획

이 문서는 Spring Boot 구현을 시작할 때 사용할 작업 분해 문서입니다. 현재는 코드 생성 전 계획 단계입니다.

## 1단계: 프로젝트 생성

- Java 21
- Spring Boot
- Gradle
- PostgreSQL 드라이버
- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- Actuator
- QueryDSL
- Redis
- Testcontainers

## 2단계: 공통 기반

- 공통 응답 형식
- 공통 에러 응답
- 전역 예외 처리
- 날짜/시간 응답 규칙
- 인증 사용자 조회 유틸리티
- 테스트 기본 설정

## 3단계: 인증/인가

- 회원가입
- 로그인
- JWT access token 발급
- USER/ADMIN 권한 처리
- `/api/admin/**` 접근 제한
- 로그인 사용자 전용 API 접근 제한

1차에서는 refresh token과 서버 측 logout blacklist는 후순위로 둡니다.

## 4단계: 철도 기본 데이터

- Station CRUD
- Route CRUD
- Train CRUD
- Car CRUD
- Seat CRUD 또는 자동 생성
- TrainSchedule CRUD
- ScheduleSeat 생성

## 5단계: 사용자 조회 API

- 역 목록/검색
- 운행편 검색
- 운행편 상세
- 좌석 조회

## 6단계: 예매 핵심 API

- 좌석 HOLD
- Reservation 생성
- ReservationSeat 생성
- Payment READY 생성
- 내 예매 목록
- 예매 상세
- 예매 취소

## 7단계: 결제 API

- 가상 결제 성공
- 가상 결제 실패
- 가상 결제 취소
- 결제 만료 처리

## 8단계: HOLD 만료 Scheduler

- 만료된 HELD 좌석 조회
- Reservation EXPIRED 처리
- Payment EXPIRED 처리
- ScheduleSeat AVAILABLE 복구
- OperationLog 저장

실제 쿼리, 배치 크기, 실행 주기는 DB 테이블과 인덱스 확정 후 다시 결정합니다.

## 9단계: 테스트

- 도메인 상태 전이 테스트
- 좌석 HOLD 성공/실패 테스트
- 결제 성공/실패/취소 테스트
- HOLD 만료 테스트
- 동시 HOLD 요청 테스트
- 관리자 권한 테스트

## 10단계: 운영 기능

- 애플리케이션 로그 정리
- OperationLog 저장
- Actuator 설정
- Prometheus 지표 노출
- Docker Compose 연동
