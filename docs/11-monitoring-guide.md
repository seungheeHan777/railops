# 모니터링 가이드

Spring Actuator, Prometheus, Grafana를 사용해 애플리케이션 상태, API 요청 수, 응답 시간, 에러율, 예매/결제 지표, DB/Redis 상태를 확인합니다.

2026-10-19 구현: Actuator health/Prometheus를 별도 관리용 8081 포트에 구성했습니다. 로컬 health UP과 JVM 메트릭 응답을 확인했습니다. Compose의 monitoring 프로필은 Prometheus/Grafana 및 데이터 소스를 제공합니다. 컨테이너 수집 실구동, 대시보드/알람은 후속 검증입니다. Redis는 업무 캐시에 아직 사용하지 않아 Redis health 검사를 비활성화했습니다.

실행법과 공개 포트 제한은 `docs/38-local-and-container-runbook.md`를 확인합니다. 현재 DB 운영 로그는 ADMIN 화면/API에서 조회할 수 있으며 비밀번호/토큰/입력 이메일을 저장하지 않습니다.

