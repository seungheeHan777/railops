import { useEffect, useState } from 'react';
import { ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { apiRequest } from './api/client';
import type { PageResult } from './types/api';

type Log = { id: number; actorId: number | null; action: string; targetType: string; targetId: number | null; createdAt: string };
const actions: Record<string, string> = {
  LOGIN_FAILED: '로그인 실패', PROFILE_UPDATED: '프로필 변경', PASSWORD_CHANGED: '비밀번호 변경', ACCOUNT_WITHDRAWN: '회원 탈퇴', LOGOUT: '로그아웃',
  RESERVATION_HELD: '좌석 임시 점유', PAYMENT_SUCCESS: '결제 성공', PAYMENT_FAILED: '결제 실패', PAYMENT_CANCELED: '결제 취소', PAYMENT_EXPIRED: '결제 만료',
  RESERVATION_CANCELED: '예매 취소', PAYMENT_REFUNDED: '환불', SEAT_BLOCKED: '좌석 차단', SEAT_UNBLOCKED: '좌석 해제', SCHEDULE_CANCELED: '운행 취소', ADMIN_CREATED: '관리자 생성',
};
export default function OperationLogsView({ token }: { token: string }) {
  const [action, setAction] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [data, setData] = useState<PageResult<Log> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  useEffect(() => {
    let active = true; setLoading(true); setError('');
    const query = new URLSearchParams({ page: String(page), size: '20' });
    if (action) query.set('action', action); if (from) query.set('from', from); if (to) query.set('to', to);
    apiRequest<PageResult<Log>>(`/admin/operation-logs?${query}`, { token }).then((result) => { if (active) setData(result); })
      .catch((e: unknown) => { if (active) setError(e instanceof Error ? e.message : '로그 조회 실패'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [token, page, action, from, to, revision]);
  return <div className="reservation-view"><div className="reservation-toolbar">
    <label className="field"><span>이벤트</span><select value={action} onChange={(e) => { setAction(e.target.value); setPage(0); }}><option value="">전체</option>{Object.entries(actions).map(([key, value]) => <option key={key} value={key}>{value}</option>)}</select></label>
    <label className="field"><span>시작일</span><input type="date" value={from} onChange={(e) => { setFrom(e.target.value); setPage(0); }} /></label>
    <label className="field"><span>종료일</span><input type="date" value={to} onChange={(e) => { setTo(e.target.value); setPage(0); }} /></label>
    <button className="icon-button" title="로그 새로고침" aria-label="로그 새로고침" disabled={loading} onClick={() => setRevision((v) => v + 1)}><RefreshCw size={18} /></button>
  </div>{error && <div className="notice error" role="alert">{error}</div>}
    <div className="table-frame"><table className="reservation-table"><thead><tr><th>시각</th><th>이벤트</th><th>처리자</th><th>대상</th></tr></thead><tbody>
      {!error && data?.content.map((log) => <tr key={log.id}><td data-label="시각">{log.createdAt.replace('T', ' ').slice(0, 19)}</td><td data-label="이벤트">{actions[log.action] ?? log.action}</td><td data-label="처리자">{log.actorId ?? '시스템'}</td><td data-label="대상">{log.targetType} {log.targetId ?? '-'}</td></tr>)}
      {!data?.content.length && <tr><td colSpan={4} className="table-empty">{loading ? '조회 중' : '로그가 없습니다.'}</td></tr>}
    </tbody></table></div>
    <div className="reservation-pagination"><span>총 {data?.totalElements ?? 0}건 · {page + 1} / {Math.max(1, data?.totalPages ?? 0)}</span>
      <button className="icon-button" title="이전 로그" aria-label="이전 로그" disabled={loading || page === 0} onClick={() => setPage((v) => v - 1)}><ChevronLeft size={18} /></button>
      <button className="icon-button" title="다음 로그" aria-label="다음 로그" disabled={loading || page + 1 >= (data?.totalPages ?? 0)} onClick={() => setPage((v) => v + 1)}><ChevronRight size={18} /></button>
    </div>
  </div>;
}
