# 인증 구현 노트

이 문서는 RailOps 백엔드의 1차 인증 구현 내용을 기록합니다.

## 구현 범위

현재 구현된 인증 기능은 다음과 같습니다.

```text
POST /api/auth/signup
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
```

1차 구현 목표는 사용자 도메인과 JWT access token 기반 인증 흐름의 뼈대를 만드는 것입니다.

## 생성된 주요 패키지

```text
com.railops.user.domain
com.railops.user.repository
com.railops.auth.controller
com.railops.auth.dto
com.railops.auth.security
com.railops.auth.service
```

## User 도메인

`User` 엔티티는 `users` 테이블에 매핑됩니다.

주요 필드:

```text
id
email
password
name
role
status
createdAt
updatedAt
```

현재 권한:

```text
USER
ADMIN
```

현재 상태:

```text
ACTIVE
LOCKED
DELETED
```

회원가입으로 생성되는 사용자는 기본적으로 다음 값을 가집니다.

```text
role = USER
status = ACTIVE
```

## 회원가입 흐름

```text
1. SignupRequest 검증
2. email 중복 확인
3. 비밀번호 BCrypt 해시
4. User 생성
5. DB 저장
6. UserSummaryResponse 반환
```

중복 이메일이면 `DUPLICATE_EMAIL` 에러를 반환합니다.

## 로그인 흐름

```text
1. LoginRequest 검증
2. email로 User 조회
3. 사용자 상태 ACTIVE 확인
4. BCrypt로 비밀번호 검증
5. JWT access token 생성
6. LoginResponse 반환
```

이메일 또는 비밀번호가 맞지 않으면 `INVALID_CREDENTIALS` 에러를 반환합니다.

## JWT 구현 방식

현재 JWT는 외부 JWT 라이브러리를 추가하지 않고 Java 표준 API로 직접 생성합니다.

사용 기술:

```text
HmacSHA256
Base64 URL encoding
javax.crypto.Mac
```

토큰 payload에는 현재 다음 값을 담습니다.

```text
sub = 사용자 email
uid = 사용자 id
exp = 만료 시각 epoch seconds
```

1차 구현에서는 access token만 사용합니다. refresh token, blacklist, 서버 측 로그아웃은 후순위입니다.

## Security 설정

현재 Security 정책:

```text
/api/auth/signup  허용
/api/auth/login   허용
/actuator/health  허용
/api/admin/**     ADMIN 권한 필요
그 외 요청        인증 필요
```

JWT 인증 필터는 `Authorization: Bearer {token}` 헤더를 읽고 토큰이 유효하면 SecurityContext에 인증 정보를 넣습니다.

## 로그아웃 정책

현재 logout API는 서버 상태를 변경하지 않고 성공 응답만 반환합니다. 클라이언트가 access token을 삭제하는 방식입니다.

후순위 확장:

```text
refresh token 저장
refresh token rotation
logout token blacklist
Redis 기반 token denylist
```

## 테스트

추가된 테스트:

```text
JwtTokenProviderTest
AuthServiceTest
```

검증 내용:

- JWT 생성과 검증
- 잘못된 JWT 거부
- 회원가입 성공
- 중복 이메일 거부
- 로그인 성공과 access token 발급
- 잘못된 비밀번호 거부

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

1. 실제 DB 연결 후 회원가입/로그인 API 통합 테스트 작성
2. User 관련 에러 응답이 API에서 원하는 JSON으로 내려오는지 확인
3. 관리자 계정 생성 방법 결정
4. Station 관리자 CRUD 구현 시작