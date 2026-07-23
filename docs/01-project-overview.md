# 프로젝트 개요

## 프로젝트명

RailOps

## 한 줄 설명

좌석 선택 기반 철도 예매 시스템을 Spring Boot로 구현하고, Docker Compose와 AWS EC2 환경에서 운영 가능한 형태까지 확장하는 백엔드/운영형 포트폴리오 프로젝트입니다.

## 프로젝트 목적

RailOps는 단순한 예매 CRUD가 아니라 실제 철도 예매 서비스를 운영한다고 가정하고 설계합니다. 사용자가 열차를 조회하고 특정 운행편의 객차와 좌석을 직접 선택하며, 결제 전에는 좌석을 임시 점유하고, 결제가 완료되면 예매를 확정합니다.

핵심은 동일 좌석 중복 예매 방지입니다. 이를 위해 좌석 상태 모델, 트랜잭션, 락, 만료 처리, 결제 상태 전이를 명확하게 설계합니다.

## 학습 목표

- Spring Boot 기반 REST API 설계와 구현
- Spring Security 기반 인증/인가 구조 설계
- JPA와 QueryDSL을 활용한 도메인 중심 데이터 접근
- PostgreSQL 기반 트랜잭션과 동시성 제어
- 좌석 HELD 만료 처리를 위한 Spring Scheduler 활용
- Redis 캐시와 TTL의 역할 이해
- Docker Compose 기반 로컬/운영 환경 구성
- Nginx Reverse Proxy 구성
- GitHub Actions 기반 CI/CD 흐름 학습
- Actuator, Prometheus, Grafana 기반 모니터링
- 운영 로그, 장애 대응, 부하 테스트 문서화

## 프로젝트 성격

이 프로젝트는 백엔드 중심입니다. 다만 실제 예매 흐름을 확인하려면 프론트 화면이 필요하므로, React 기반의 최소 UI도 구현합니다.

- 백엔드 개발: 60%
- IT 운영 / 서버 운영: 40%

## 초기 진행 원칙

1. 문서와 설계를 먼저 정리한다.
2. Spring Boot와 React 코드는 문서 구조가 잡힌 뒤 생성한다.
3. 핵심 기능은 좌석 선택, 좌석 임시 점유, 가상 결제, 중복 예매 방지로 둔다.
4. 초기 배포는 단일 EC2 + Docker Compose 기준으로 설계한다.
5. RDS, ElastiCache, 무중단 배포, Kubernetes는 추후 확장 목표로 둔다.
