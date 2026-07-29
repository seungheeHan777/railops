# TODO

RailOps 프로젝트 진행 상태를 추적하는 문서입니다. Codex와 후속 에이전트는 작업 전후로 이 문서를 확인하고 필요한 경우 갱신합니다.

## 현재 단계

- [x] 프로젝트 루트 폴더 생성
- [x] 초기 폴더 구조 생성
- [x] README 초안 작성
- [x] 기본 docs 문서 생성
- [x] 에이전트 작업 지침 문서 생성
- [x] 요구사항 문서 상세화
- [x] 도메인 모델 상세화
- [x] ERD 초안 작성
- [x] API 명세 초안 작성
- [x] 예매/결제/동시성 흐름 문서 상세화
- [x] 화면 구성 문서 작성
- [x] 백엔드 구현 계획 문서 작성
- [x] ERD와 API/DTO 설명 보강
- [x] PostgreSQL DDL 초안 작성
- [x] 테스트 시나리오 문서 작성
- [x] Spring Boot backend 프로젝트 생성
- [x] 공통 API 응답 구조 작성
- [x] 공통 에러 코드와 비즈니스 예외 작성
- [x] 전역 예외 처리 구조 작성
- [x] 임시 Security 설정 작성
- [x] backend 기본 테스트 통과
- [x] User 도메인 구현
- [x] 회원가입 API 구현
- [x] 로그인 API 구현
- [x] JWT access token 발급 구현
- [x] JWT 인증 필터 구현
- [x] 인증 단위 테스트 통과
- [x] Station 도메인 구현
- [x] Station 공개 조회 API 구현
- [x] Station 관리자 CRUD API 구현
- [x] Station 단위 테스트 통과
- [x] 프론트엔드 1차 UI 구현
- [x] 인증/Station API client 구현
- [x] 프론트엔드 빌드 통과
- [x] Route 도메인 구현
- [x] Route 관리자 CRUD API 구현
- [x] Route 생성/수정 검증 구현
- [x] Route 단위 테스트 통과

## 다음 작업 후보

1. Train 도메인과 관리자 CRUD API를 구현한다.
2. Train 타입과 train_no 중복 정책을 정리한다.
3. Car/Seat 도메인 설계를 구현 코드로 옮긴다.
4. TrainSchedule 도메인과 관리자 CRUD API를 구현한다.
5. Route 관리자 화면을 프론트엔드에 추가한다.
6. Docker Desktop 실행 후 Testcontainers 통합 테스트 환경을 다시 활성화한다.

## 보류 중인 결정

- 실제 가격 모델: 노선 기준 가격, 운행편 기준 가격, 좌석 타입 기준 가산 중 어떤 방식으로 시작할지 결정 필요
- 결제 완료 후 예매 취소: REFUNDED 상태를 추가할지, 1차에서는 단순 취소로 둘지 결정 필요
- JWT 세부 정책: refresh token을 1차에 포함할지 후순위로 둘지 결정 필요
- 관리자 계정 생성 방식: seed data, 별도 admin signup, DB 직접 생성 중 선택 필요
- Station 삭제 정책: Route가 참조 중인 Station 삭제를 막을지 결정 필요
- Route 삭제 정책: TrainSchedule이 참조 중인 Route 삭제를 막을지 결정 필요
- HOLD 만료 처리: DB/테이블 구조 확정 후 Scheduler 쿼리와 배치 크기 재검토 필요
- Java 21 설치: 프로젝트는 Java 21 기준이므로 로컬 JDK 21 설치 권장