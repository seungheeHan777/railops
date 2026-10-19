# 예매 관리 구현 기준

작성일: 2026-10-18

## 이번 구현 범위

- 단일 기본요금: 개발 기본값 50,000원. `RAILOPS_DEFAULT_SEAT_PRICE`로 변경한다. 양의 정수 원화만 허용하며, 과거 예매의 가격은 바꾸지 않는다.
- 본인 예매 목록/상세, 관리자 전체 예매 목록/상세. 목록은 페이지 단위로 조회하고 상태로 필터링한다.
- 결제 대기 예매 취소 및 확정 예매의 출발 전 전액 가상 환불. 부분 취소는 지원하지 않는다.
- 취소/환불 후에도 예매와 좌석 연결 이력은 삭제하지 않는다.
- 결제 성공과 취소 요청을 중복 전송해도 같은 최종 상태를 반환한다. 환불된 예매를 다시 결제할 수는 없다.
- 사용자 마이페이지에서 결제 대기 예매의 가상 결제를 재개하고 취소할 수 있다.

## API 계약

| 메서드 | 주소 | 권한 |
| --- | --- | --- |
| GET | `/api/reservations?page=0&size=20&status=CONFIRMED` | 본인 |
| GET | `/api/reservations/{id}` | 본인 |
| POST | `/api/reservations/{id}/cancel` | 본인 |
| GET | `/api/admin/reservations?page=0&size=20&status=CONFIRMED` | ADMIN |
| GET | `/api/admin/reservations/{id}` | ADMIN |

목록 응답은 `content`, `page`, `size`, `totalElements`, `totalPages`를 가진다. 페이지는 0부터 시작하고 size는 1~100이다. 상세에는 운행편/구간/시각, 좌석별 예약 당시 금액, 결제 상태/환불 금액, 취소 및 결제 가능 여부를 포함한다. 본인 API는 관리자라도 다른 회원의 예매를 조회할 수 없다.

## 상태 전이

- 결제 전 취소: Reservation CANCELED, Payment CANCELED, 해당 HOLD만 해제.
- 결제 후 출발 전 취소: Reservation CANCELED, Payment REFUNDED, 전액 환불 기록, 예약 좌석 반환.
- 출발 시각 이상: 새로운 HOLD/결제 및 확정 예매 취소를 거절한다.
- HOLD 만료: Reservation/Payment EXPIRED, 해당 HOLD만 해제. 만료시각과 현재시각이 같은 경우에도 만료다.

환불은 외부 결제사 연동 없이 DB에서만 처리하는 시뮬레이션이다. 위 정책은 개발 기준이며 실제 서비스의 운임/환불 규정은 별도 결정이 필요하다.

## 정합성 보완

결제/취소/만료 처리는 Payment 행을 먼저 잠그고 ScheduleSeat를 ID 오름차순으로 잠근다. HOLD는 ScheduleSeat를 같은 순서로 잠근다. PostgreSQL의 nullable outer join에 대한 잠금 오류를 피하도록 잠금 쿼리는 좌석 행만 조회한다.

만료 처리 직후 HOLD_EXPIRED 예외를 반환해도 만료 상태는 커밋되어야 한다. 이 경우에 한정한 예외 타입을 사용하고 다른 오류는 롤백한다. 과거 HOLD를 정리할 때는 사용자와 만료시각이 모두 일치하는 좌석만 해제해서 새로운 점유를 보호한다.

Scheduler는 만료 후보를 최대 100건씩 읽고 건별 트랜잭션으로 처리한다. 예매/결제 번호는 UUID를 사용해 같은 회원의 동시 요청 충돌을 피한다. 관리자 BLOCK은 AVAILABLE에서만, UNBLOCK은 BLOCKED에서만 가능하다.

## 검증 및 남은 점

단위/통합 전체 78개 테스트와 프론트 빌드를 통과했다. PostgreSQL 통합 테스트 10개는 별도 격리 스키마에서 수행했고, 데스크톱/모바일 브라우저 흐름도 확인했다. 기존 `public` 테이블을 테스트용으로 삭제하거나 초기화하지 않았다. 자세한 결과는 `docs/36-work-report-20261018.md`에 기록했다.

실제 결제 연동, 부분 환불, 운행 취소에 따른 일괄 환불, Flyway 마이그레이션, 운영 로그/배포는 후속 범위다.
