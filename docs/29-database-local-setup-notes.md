# 로컬 DB 연결 노트

이 문서는 RailOps 백엔드를 로컬 PostgreSQL에 연결하기 위한 개발 환경 설정을 기록합니다.

## 기본 방향

개발 DB는 PostgreSQL을 사용합니다.

```text
DBMS: PostgreSQL
Host: localhost
Port: 5432
Database: railops
User: railops
Password: railops
```

프론트엔드는 DB에 직접 연결하지 않습니다.

```text
React -> Spring Boot API -> PostgreSQL
```

## Docker 사용 가능 환경

Docker Desktop이 설치되어 있으면 프로젝트 루트에서 다음 명령으로 PostgreSQL 컨테이너를 실행합니다.

```powershell
cd C:\Users\User\Documents\railops
docker compose up -d postgres
```

추가된 파일:

```text
docker-compose.yml
```

컨테이너 이름:

```text
railops-postgres
```

## 이미 로컬 PostgreSQL이 설치된 환경

현재 PC처럼 5432 포트에 PostgreSQL이 이미 실행 중이면 Docker 없이 기존 PostgreSQL을 사용할 수 있습니다.

필요한 DB와 계정은 다음과 같습니다.

```sql
create user railops with password 'railops';
create database railops owner railops;
grant all privileges on database railops to railops;
```

이미 같은 이름의 user/database가 있으면 생성 명령은 생략합니다.

## Spring Boot local 프로필

추가된 파일:

```text
backend/src/main/resources/application-local.properties
```

주요 설정:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/railops
spring.datasource.username=railops
spring.datasource.password=railops
spring.jpa.hibernate.ddl-auto=update
```

개발 초반에는 `ddl-auto=update`로 Entity 기준 테이블을 자동 생성합니다. 구조가 안정되면 Flyway migration으로 전환합니다.

## 백엔드 실행

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## 프론트 실행

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run dev
```

## 주의 사항

- Docker가 설치되어 있지 않으면 `docker` 명령은 사용할 수 없습니다.
- 로컬 PostgreSQL이 이미 5432 포트를 사용 중이면 Docker PostgreSQL 컨테이너도 같은 포트를 열 수 없습니다.
- PostgreSQL 계정/DB가 없으면 백엔드 실행 시 인증 또는 database not found 오류가 납니다.
- 운영 전환 시에는 비밀번호를 `railops`로 두면 안 되고 환경 변수 또는 secret manager로 분리해야 합니다.