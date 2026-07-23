# 에이전트 작업 가이드

이 문서는 RailOps 프로젝트에서 Codex가 어떤 순서와 기준으로 작업해야 하는지 설명합니다.

## 작업 시작 체크리스트

작업을 시작하기 전에 다음 파일을 확인합니다.

```text
AGENTS.md
TODO.md
README.md
docs/01-project-overview.md
docs/02-requirements.md
```

특정 기능을 수정하거나 구현할 때는 관련 문서를 함께 확인합니다.

```text
도메인/테이블       → docs/03-domain-model.md, docs/04-erd.md
API                 → docs/05-api-spec.md
예매 흐름           → docs/06-reservation-flow.md
결제 흐름           → docs/07-payment-flow.md
동시성 제어         → docs/08-concurrency-strategy.md
인프라/배포         → docs/09-infra-architecture.md, docs/10-deployment-guide.md
모니터링/장애 대응  → docs/11-monitoring-guide.md, docs/12-incident-response.md
```

## 작업 완료 체크리스트

작업을 완료하면 다음을 확인합니다.

- 변경된 코드나 문서가 현재 단계의 목표와 맞는가
- 설계 결정이 문서에 남아 있는가
- TODO 상태가 필요한 만큼 갱신되었는가
- 테스트나 검증이 필요한 작업이면 결과를 남겼는가
- 다음 작업자가 이어받을 수 있을 만큼 맥락이 남아 있는가

## 문서 우선 원칙

RailOps는 학습과 포트폴리오 목적이 강한 프로젝트입니다. 따라서 구현 자체뿐 아니라 왜 그렇게 구현했는지를 설명할 수 있어야 합니다.

특히 아래 항목은 반드시 문서화합니다.

- 좌석 상태 모델
- HOLD 만료 처리 방식
- 결제 상태 전이
- 중복 예매 방지 전략
- Redis를 사용하는 이유와 한계
- Docker Compose 구성 이유
- 장애 대응 절차
- 부하 테스트 결과와 개선점

## 현재 합의된 방향

- 프로젝트 위치: `C:\Users\User\Documents\railops`
- 첫 단계: 문서와 폴더 구조 구성
- 프론트엔드: 최소 UI를 만들되, 사용자 화면과 관리자 화면을 분리
- 메인 페이지: 사용자 진입점으로 별도 구성
- HOLD 만료: PostgreSQL의 `hold_expires_at`을 기준으로 Spring Scheduler가 정리
- 결제 API: 결제 성공 시에도 HOLD 만료 여부 재검증
- 상세 만료 방식: 실제 DB와 테이블 구조가 확정된 뒤 다시 검토
