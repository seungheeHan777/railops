# 프론트엔드 구현 노트

이 문서는 RailOps 프론트엔드의 1차 구현 내용을 기록합니다.

## 구현 범위

현재 백엔드에서 완성된 기능에 맞춰 프론트엔드를 구성했습니다.

구현된 화면:

```text
메인 페이지
회원가입
로그인
마이페이지
역 조회
관리자 역 관리
```

연결된 백엔드 API:

```text
POST   /api/auth/signup
POST   /api/auth/login
GET    /api/auth/me
POST   /api/auth/logout
GET    /api/stations
GET    /api/stations/search
POST   /api/admin/stations
GET    /api/admin/stations
PATCH  /api/admin/stations/{stationId}
DELETE /api/admin/stations/{stationId}
```

## 기술 구성

```text
React
TypeScript
Vite
lucide-react
fetch 기반 API client
```

아직 Tailwind CSS는 적용하지 않았습니다. 1차 UI는 별도 CSS로 구성했고, 이후 화면이 늘어나면 Tailwind 도입 여부를 다시 결정합니다.

## 파일 구조

```text
frontend/
├─ index.html
├─ package.json
├─ package-lock.json
├─ tsconfig.json
├─ tsconfig.app.json
├─ vite.config.ts
└─ src/
   ├─ App.tsx
   ├─ main.tsx
   ├─ styles.css
   ├─ vite-env.d.ts
   ├─ api/
   │  ├─ auth.ts
   │  ├─ client.ts
   │  └─ stations.ts
   └─ types/
      └─ api.ts
```

## API 연결 방식

프론트엔드는 기본적으로 `/api`로 요청합니다. Vite 개발 서버는 `/api` 요청을 `http://localhost:8080`으로 프록시합니다.

설정 위치:

```text
frontend/vite.config.ts
```

백엔드 주소를 직접 지정하려면 `.env.local`에 다음 값을 둘 수 있습니다.

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## 인증 처리

로그인 성공 시 access token을 `localStorage`에 저장합니다.

저장 키:

```text
railops.accessToken
```

이후 인증이 필요한 API에는 다음 헤더를 붙입니다.

```text
Authorization: Bearer {token}
```

## 관리자 화면 주의사항

관리자 역 관리 화면은 구현되어 있지만, 현재 백엔드에는 ADMIN 계정을 생성하는 별도 API가 아직 없습니다.

따라서 실제 관리자 CRUD를 화면에서 사용하려면 다음 중 하나가 필요합니다.

```text
관리자 seed data 추가
DB에서 직접 ADMIN 사용자 생성
관리자 전용 가입 API 추가
```

이 결정은 다음 백엔드 작업에서 정리합니다.

## 실행 방법

프론트엔드 실행:

```powershell
cd C:\Users\User\Documents\railops\frontend
npm install
npm run dev
```

접속 주소:

```text
http://127.0.0.1:5173
```

백엔드 API까지 확인하려면 Spring Boot 서버도 실행해야 합니다.

```powershell
cd C:\Users\User\Documents\railops\backend
.\gradlew.bat bootRun
```

## 검증

빌드 명령:

```powershell
cd C:\Users\User\Documents\railops\frontend
npm run build
```

검증 결과:

```text
빌드 성공
```

개발 서버:

```text
http://127.0.0.1:5173
```

## 다음 작업

프론트엔드는 백엔드 기능이 늘어나는 순서에 맞춰 확장합니다.

다음 후보:

```text
Route 관리자 화면
Train 관리자 화면
TrainSchedule 관리자 화면
좌석 배치도 화면
예매 확인 화면
가상 결제 화면
내 예매 목록 화면
```