# 백엔드 세팅 노트

이 문서는 RailOps Spring Boot 백엔드 프로젝트 생성과 초기 설정 내용을 기록합니다.

## 생성 방식

Spring Initializr를 사용해 `backend/` 폴더에 Gradle 기반 Spring Boot 프로젝트를 생성했습니다.

기본 설정:

```text
Group: com.railops
Artifact: backend
Package: com.railops
Java: 21
Build: Gradle
Spring Boot: Spring Initializr 기본 버전
```

## 포함된 주요 의존성

```text
Spring Web MVC
Spring Data JPA
Spring Security
Spring Validation
Spring Actuator
Spring Data Redis
PostgreSQL Driver
Testcontainers
JUnit5
```

## 현재 구현된 공통 코드

```text
com.railops.common.response.ApiResponse
com.railops.common.error.ErrorCode
com.railops.common.error.BusinessException
com.railops.common.error.GlobalExceptionHandler
com.railops.config.SecurityConfig
```

역할:

- `ApiResponse`: API 성공/실패 응답 형식 통일
- `ErrorCode`: 공통 에러 코드와 HTTP 상태 관리
- `BusinessException`: 서비스 로직에서 사용하는 비즈니스 예외
- `GlobalExceptionHandler`: 예외를 공통 에러 응답으로 변환
- `SecurityConfig`: JWT 구현 전까지 모든 요청을 임시 허용

## 테스트 상태

현재 테스트 명령:

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat test
```

결과:

```text
BUILD SUCCESSFUL
```

## Java 버전 주의사항

프로젝트는 Java 21 toolchain 기준입니다. 현재 로컬 PC에는 Java 17이 설치되어 있습니다.

Gradle toolchain 또는 IDE 설정에 따라 자동으로 Java 21을 내려받을 수 있지만, 안정적으로 개발하려면 JDK 21 설치를 권장합니다.

확인 명령:

```powershell
java -version
```

## Testcontainers 주의사항

Spring Initializr가 PostgreSQL과 Redis Testcontainers 설정을 생성했습니다.

```text
TestcontainersConfiguration.java
TestRailOpsApplication.java
```

다만 현재 기본 테스트에서는 Docker가 없어도 통과하도록 `contextLoads` 테스트 대신 `ApiResponse` 단위 테스트를 사용합니다.

DB/Redis 연동 테스트를 시작할 때는 Docker Desktop 실행 후 Testcontainers 기반 통합 테스트를 다시 활성화합니다.

## 다음 구현 순서

1. 인증/인가 구조 설계 반영
2. User 도메인과 회원가입/로그인 API 구현
3. 공통 요청 검증과 에러 코드 확장
4. Station, Route, Train, TrainSchedule 관리자 CRUD 구현
5. ScheduleSeat와 Reservation/Payment 핵심 흐름 구현
