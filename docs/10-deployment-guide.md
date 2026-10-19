# 배포 가이드

이 문서는 로컬 Docker Compose 실행, EC2 서버 준비, 환경 변수 관리, Nginx 연결, 배포 절차를 정리합니다. 초기에는 수동 배포를 먼저 문서화하고 이후 GitHub Actions 자동 배포를 추가합니다.

2026-10-19: Dockerfile/Compose/Nginx 및 테스트·이미지 빌드 CI를 작성했습니다. 자동 클라우드 배포는 구성하지 않았습니다. 실제 실행 절차, 환경변수, DB 포트 차이와 Flyway 전환은 `docs/38-local-and-container-runbook.md`를 기준으로 확인합니다. 현재 PC는 Docker 미설치이며 실제 서버 배포에는 서버/도메인/TLS/접근 환경이 필요합니다.

