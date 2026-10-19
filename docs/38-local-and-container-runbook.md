# 로컬 실행과 컨테이너 운영

작성일: 2026-10-19

## 검증한 실행 방식

로컬 PostgreSQL은 localhost:5432의 기존 railops DB를 사용한다. 프론트는 http://127.0.0.1:5173, 백엔드는 http://127.0.0.1:8080, 관리용 health/Prometheus는 http://127.0.0.1:8081 이다.

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run dev
```

실행 중인 서버가 있으면 중복 실행하지 않는다. PC 종료 후에는 다시 시작해야 한다.

## Flyway 전환

이제 Hibernate는 ddl-auto=validate로 스키마를 검증하며 테이블을 자동 변경하지 않는다. V1은 신규 DB용 초기 스키마, V2는 token_version/운영 로그/환불/조회 인덱스 추가다. 신규 DB는 V1→V2, 기존 local DB는 버전 1 baseline→V2로 진행한다.

local 프로필에서만 baseline-on-migrate 기본값이 true이다. 다른 DB를 잘못 지정하지 않도록 URL/계정을 먼저 확인한다. 이미 적용한 V1/V2 파일은 수정하지 않고 이후 변경은 V3 등 새 파일로 추가한다. 적용 중 실패하면 원인을 확인하고 복구하며 flyway_schema_history를 임의 삭제하지 않는다.

2026-10-19 실제 local DB 전환 전 스키마를 프로젝트 외부에 백업했다. 전환 후 기존 users/reservations/payments/schedule_seats 행 수가 동일함을 확인했다. 비밀번호·사용자 권한·예매 이력을 바꾸지 않았다.

기존 dump를 다른 DB에 복원하면 이력 테이블도 함께 복원한다. Flyway 이전 dump에만 RAILOPS_FLYWAY_BASELINE=true를 한 번 사용하고 정상 이행 후 false로 되돌린다.

## 관리자 최초 생성

일반 회원가입은 항상 USER 권한이다. 최초 관리자만 명시적으로 bootstrap 옵션을 켜서 생성한다.

```powershell
$env:RAILOPS_BOOTSTRAP_ADMIN='true'
$env:RAILOPS_ADMIN_EMAIL='본인이 사용할 관리자 이메일'
$env:RAILOPS_ADMIN_PASSWORD='본인이 정한 12자 이상 비밀번호'
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

생성 확인 후 bootstrap을 끄고 해당 환경변수를 제거한다. 기존 일반 회원 이메일을 자동으로 승격하지 않으며 기존 관리자 비밀번호도 재설정하지 않는다. 이미 같은 이메일의 활성 관리자가 있으면 추가 생성하지 않는다. 비밀번호는 최대 72 UTF-8 바이트다. 실제 계정 정보를 Git에 저장하지 않는다.

## Docker 실행 준비

현재 PC에는 Docker가 설치되어 있지 않다. 아래는 구성 파일과 실행 절차이며 이 PC에서 이미지/컨테이너 실구동 검증은 아직 하지 않았다.

1. 사용자가 Docker Desktop 및 필요한 가상화 환경을 설치하고 Docker 엔진을 실행한다.
2. 프로젝트 루트에서 .env.example을 .env로 복사하고 DB 비밀번호/JWT secret/Grafana 비밀번호를 본인 값으로 바꾼다. JWT secret은 최소 32바이트의 임의 문자열을 사용한다.
3. 아래 명령으로 설정 검증 및 이미지를 빌드한다.

```powershell
cd C:\Users\User\Documents\railops
Copy-Item .env.example .env
# .env의 개발 예시 값을 변경한 후 실행
docker compose config --quiet
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 backend
```

주소는 http://127.0.0.1:8090 이다. Nginx가 React 정적 파일을 제공하고 /api를 백엔드로 전달한다. 백엔드와 관리용 포트는 호스트에 공개하지 않는다.

컨테이너 PostgreSQL은 기존 로컬 DB와 별개이며 DBeaver에서는 localhost:55432로 접속한다. 기존 localhost:5432의 데이터가 자동 이동하지 않는다. 계정 railops, DB railops, 비밀번호는 .env 값이다. DB 데이터는 named volume에 유지된다. 기존 volume이 있으면 .env의 비밀번호만 바꿔도 DB 계정 비밀번호가 바뀌지는 않는다.

데모 데이터는 앱 마이그레이션 완료 후 추가한다.

```powershell
Get-Content backend/sql/demo-data.sql -Raw -Encoding UTF8 | docker compose exec -T postgres psql -U railops -d railops -v ON_ERROR_STOP=1
```

중지는 docker compose down으로 한다. 데이터 보존을 위해 volume 삭제 옵션은 사용하지 않는다. 기존 로컬 DB를 옮기려면 dump/restore를 별도 수행한다.

## 모니터링과 부하 확인

```powershell
docker compose --profile monitoring up -d
```

Prometheus: http://127.0.0.1:9090, Grafana: http://127.0.0.1:3000. Grafana 계정은 admin, 비밀번호는 .env 값이며 Prometheus 데이터 소스를 자동 등록한다. JVM 메모리/GC, Hikari 연결, http_server_requests_seconds_count 등 기본 메트릭을 조회한다. 별도 Grafana 대시보드/알람 룰은 후속 운영 작업이다.

```powershell
$env:DATE=(Get-Date).AddDays(1).ToString('yyyy-MM-dd')
docker compose --profile loadtest run --rm k6
```

k6는 DEMO_SEOUL→DEMO_BUSAN을 기본 10 VU/30초로 조회한다. 요청 실패율 1% 미만, p95 500ms 미만, 유효한 조회 결과 검증을 기준으로 한다. 미래 데모 운행편을 먼저 생성해야 한다. 임계치는 개발 기준이며 실제 SLA가 아니다.

Docker 없이 수행할 짧은 읽기 점검도 있다.

```powershell
$env:DATE=(Get-Date).AddDays(1).ToString('yyyy-MM-dd')
node infra/load-smoke.cjs
```

Node 점검은 동시 10개/총 200요청이며 k6 장시간 부하 테스트와 다르다. 실제 수치는 docs/39-work-report-20261019.md 참고.

## CI와 배포

GitHub Actions는 Java 21/Node 22/PostgreSQL 16에서 전체 테스트와 프론트 빌드를 수행하고 Compose 설정 및 이미지를 빌드한다. CI 결과는 GitHub Actions에서 확인하며 로컬 검증과 원격 CI 성공은 구분한다.

실제 서버 배포에는 사용자의 서버/도메인/TLS 설정과 접근 권한이 필요하다. 현재 자동 클라우드 배포는 하지 않는다. 공개 서버로 배포할 때만 RAILOPS_WEB_BIND_IP/PORT를 조정하고 TLS 프록시와 방화벽을 구성한다. DB/관리용/모니터링 포트를 외부에 직접 노출하지 않는다.

## 공식 참고

- [Spring Boot Flyway 초기화](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Spring Boot 스타터 목록](https://docs.spring.io/spring-boot/reference/using/build-systems.html)
