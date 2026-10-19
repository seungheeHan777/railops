import { useEffect, useState } from 'react';
import { ChevronLeft, ChevronRight, CreditCard, Eye, RefreshCw, X } from 'lucide-react';
import { cancelReservation, fetchReservation, fetchReservations, simulatePaymentFail, simulatePaymentSuccess } from './api/railops';
import type { PageResult, PaymentStatus, ReservationDetail, ReservationStatus, ReservationSummary } from './types/api';

type Notice = { type: 'success' | 'error' | 'info'; text: string };
const statuses: Record<ReservationStatus, string> = {
  PENDING_PAYMENT: '결제 대기', CONFIRMED: '예매 확정', CANCELED: '취소', EXPIRED: '만료', PAYMENT_FAILED: '결제 실패',
};
const payments: Record<PaymentStatus, string> = {
  READY: '결제 대기', SUCCESS: '결제 완료', FAILED: '결제 실패', CANCELED: '결제 취소', EXPIRED: '만료', REFUNDED: '환불 완료',
};
const money = (value: number) => new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(value);
const dateTime = (value: string) => value.replace('T', ' ').slice(0, 16);

export default function ReservationsView({ token, admin = false, setNotice }: {
  token: string; admin?: boolean; setNotice: (notice: Notice) => void;
}) {
  const [status, setStatus] = useState<ReservationStatus | ''>('');
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PageResult<ReservationSummary> | null>(null);
  const [selected, setSelected] = useState<number | null>(null);
  const [detail, setDetail] = useState<ReservationDetail | null>(null);
  const [revision, setRevision] = useState(0);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    fetchReservations(token, admin, page, status).then((next) => {
      if (!active) return;
      if (page > 0 && page >= next.totalPages) { setPage(Math.max(0, next.totalPages - 1)); return; }
      setResult(next);
    }).catch((e: unknown) => {
      if (active) setError(e instanceof Error ? e.message : '예매 조회 실패');
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [token, admin, page, status, revision]);

  useEffect(() => {
    let active = true;
    setDetail(null);
    if (selected === null) return;
    setDetailLoading(true);
    fetchReservation(token, selected, admin).then((next) => { if (active) setDetail(next); })
      .catch((e: unknown) => { if (active) setNotice({ type: 'error', text: e instanceof Error ? e.message : '상세 조회 실패' }); })
      .finally(() => { if (active) setDetailLoading(false); });
    return () => { active = false; };
  }, [token, admin, selected, revision, setNotice]);

  async function changeReservation(action: 'cancel' | 'pay' | 'fail') {
    if (!detail || busy) return;
    if (action === 'cancel' && !window.confirm(detail.payment.status === 'SUCCESS'
      ? `${money(detail.payment.amount)} 전액 환불 후 예매를 취소할까요?` : '예매를 취소할까요?')) return;
    setBusy(true);
    try {
      if (action === 'cancel') await cancelReservation(token, detail.reservation.id);
      else if (action === 'pay') await simulatePaymentSuccess(token, detail.payment.id);
      else await simulatePaymentFail(token, detail.payment.id);
      setNotice({ type: 'success', text: action === 'cancel' ? '예매 취소 처리 완료' : action === 'pay' ? '가상 결제 완료' : '결제 실패 처리 완료' });
    } catch (e) {
      setNotice({ type: 'error', text: e instanceof Error ? e.message : '처리 실패' });
    } finally { setBusy(false); setRevision((value) => value + 1); }
  }

  return <div className="reservation-view" aria-busy={loading || busy}>
    <div className="reservation-toolbar">
      <h3>{admin ? '전체 예매' : '내 예매'}</h3>
      <label className="field"><span>예매 상태</span><select value={status} disabled={busy} onChange={(e) => { setStatus(e.target.value as ReservationStatus | ''); setPage(0); }}>
        <option value="">전체</option>{Object.entries(statuses).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
      </select></label>
      <button className="icon-button" type="button" title="예매 새로고침" aria-label="예매 새로고침" disabled={loading || busy} onClick={() => setRevision((value) => value + 1)}><RefreshCw size={18} /></button>
    </div>
    {error && <div className="notice error" role="alert">{error}</div>}
    <div className="table-frame"><table className="reservation-table"><thead><tr><th>예매번호</th>{admin && <th>회원</th>}<th>열차 / 구간</th><th>출발</th><th>상태</th><th>금액</th><th>상세</th></tr></thead><tbody>
      {!error && result?.content.map((r) => <tr key={r.id} aria-selected={r.id === selected}>
        <td data-label="예매번호"><span className="reservation-number">{r.reservationNo}</span></td>{admin && <td data-label="회원">{r.userName} (#{r.userId})</td>}
        <td data-label="열차 / 구간"><span>{r.trainNo}<br />{r.origin} → {r.destination}</span></td><td data-label="출발">{dateTime(r.departureTime)}</td>
        <td data-label="상태"><span className={`status-badge ${r.status.toLowerCase()}`}>{statuses[r.status]}</span></td><td data-label="금액">{money(r.totalAmount)}</td>
        <td data-label="상세"><button className="icon-button" type="button" title="예매 상세" aria-label={`${r.reservationNo} 상세`} disabled={busy} onClick={() => setSelected(r.id)}><Eye size={17} /></button></td>
      </tr>)}
      {!error && !result?.content.length && <tr><td className="table-empty" colSpan={admin ? 7 : 6}>{loading ? '조회 중' : '예매 내역이 없습니다.'}</td></tr>}
    </tbody></table></div>
    <div className="reservation-pagination"><span aria-live="polite">총 {result?.totalElements ?? 0}건 · {page + 1} / {Math.max(1, result?.totalPages ?? 0)}</span>
      <button className="icon-button" type="button" title="이전 페이지" aria-label="이전 페이지" disabled={loading || busy || page === 0} onClick={() => setPage((value) => value - 1)}><ChevronLeft size={18} /></button>
      <button className="icon-button" type="button" title="다음 페이지" aria-label="다음 페이지" disabled={loading || busy || page + 1 >= (result?.totalPages ?? 0)} onClick={() => setPage((value) => value + 1)}><ChevronRight size={18} /></button>
    </div>
    {detailLoading && <p role="status">상세 조회 중</p>}
    {detail && <section className="reservation-detail" aria-label="예매 상세">
      <div className="reservation-toolbar"><h3>예매 상세</h3><button className="icon-button" type="button" title="상세 닫기" aria-label="상세 닫기" disabled={busy} onClick={() => setSelected(null)}><X size={18} /></button></div>
      <dl className="reservation-facts">
        <div><dt>예매번호</dt><dd>{detail.reservation.reservationNo}</dd></div>
        <div><dt>상태</dt><dd>{statuses[detail.reservation.status]}</dd></div>
        <div><dt>구간</dt><dd>{detail.reservation.origin} → {detail.reservation.destination} · {detail.reservation.trainNo}</dd></div>
        <div><dt>출발 / 도착</dt><dd>{dateTime(detail.reservation.departureTime)} / {dateTime(detail.reservation.arrivalTime)}</dd></div>
        <div><dt>결제 / 금액</dt><dd>{payments[detail.payment.status]} · {money(detail.payment.amount)}</dd></div>
        <div><dt>환불</dt><dd>{money(detail.payment.refundedAmount)}{detail.payment.refundedAt && ` · ${dateTime(detail.payment.refundedAt)}`}</dd></div>
        {detail.reservation.status === 'PENDING_PAYMENT' && <div><dt>결제 기한</dt><dd>{dateTime(detail.reservation.holdExpiresAt)}</dd></div>}
      </dl>
      <div className="table-frame"><table className="reservation-seat-table"><thead><tr><th>객차</th><th>좌석</th><th>예매 당시 금액</th></tr></thead><tbody>{detail.seats.map((s) => <tr key={s.scheduleSeatId}><td>{s.carNo}호차</td><td>{s.seatNo}</td><td>{money(s.price)}</td></tr>)}</tbody></table></div>
      {!admin && <div className="reservation-actions">
        {detail.canPay && <><button className="primary-button" type="button" disabled={busy} onClick={() => changeReservation('pay')}><CreditCard size={17} /><span>가상 결제</span></button><button className="secondary-button" type="button" disabled={busy} onClick={() => changeReservation('fail')}>결제 실패</button></>}
        {detail.canCancel && <button className="danger-button reservation-cancel" type="button" disabled={busy} onClick={() => changeReservation('cancel')}><X size={17} /><span>{detail.payment.status === 'SUCCESS' ? '취소 및 전액 환불' : '예매 취소'}</span></button>}
      </div>}
    </section>}
  </div>;
}
