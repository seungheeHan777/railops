import { useState, type FormEvent } from 'react';
import { KeyRound, Save, UserRoundX } from 'lucide-react';
import { apiRequest } from './api/client';
import type { UserSummary } from './types/api';

type Notice = { type: 'success' | 'error' | 'info'; text: string };
export default function AccountSettings({ token, user, onUpdated, onSessionEnded, setNotice }: {
  token: string; user: UserSummary; onUpdated: (user: UserSummary) => void;
  onSessionEnded: () => void; setNotice: (notice: Notice) => void;
}) {
  const [name, setName] = useState(user.name);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [withdrawPassword, setWithdrawPassword] = useState('');
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent, action: 'name' | 'password' | 'withdraw') {
    event.preventDefault();
    if (action === 'withdraw' && !window.confirm('회원 탈퇴 후 로그인이 차단됩니다. 예매 이력은 보존됩니다. 탈퇴할까요?')) return;
    setBusy(true);
    try {
      if (action === 'name') {
        onUpdated(await apiRequest<UserSummary>('/users/me', { token, method: 'PATCH', body: { name } }));
        setNotice({ type: 'success', text: '회원 정보가 변경되었습니다.' });
      } else {
        await apiRequest(`/users/me/${action}`, { token, method: 'POST', body: action === 'password' ? { currentPassword, newPassword } : { currentPassword: withdrawPassword } });
        setCurrentPassword(''); setNewPassword(''); setWithdrawPassword(''); onSessionEnded();
        setNotice({ type: 'success', text: action === 'password' ? '비밀번호가 변경되었습니다. 다시 로그인해 주세요.' : '회원 탈퇴가 완료되었습니다.' });
      }
    } catch (e) { setNotice({ type: 'error', text: e instanceof Error ? e.message : '회원 정보 변경 실패' }); }
    finally { setBusy(false); }
  }
  return <details className="account-settings"><summary>계정 관리</summary><div className="account-settings-forms">
    <form className="form-stack" aria-label="프로필 변경" onSubmit={(e) => submit(e, 'name')}>
      <h3>이름 변경</h3><label className="field"><span>이름</span><input required maxLength={50} value={name} onChange={(e) => setName(e.target.value)} autoComplete="name" /></label>
      <button className="secondary-button" disabled={busy}><Save size={17} /><span>저장</span></button>
    </form>
    <form className="form-stack" aria-label="비밀번호 변경" onSubmit={(e) => submit(e, 'password')}>
      <h3>비밀번호 변경</h3>
      <label className="field"><span>현재 비밀번호</span><input required type="password" maxLength={72} value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} autoComplete="current-password" /></label>
      <label className="field"><span>새 비밀번호</span><input required type="password" minLength={8} maxLength={72} value={newPassword} onChange={(e) => setNewPassword(e.target.value)} autoComplete="new-password" /></label>
      <button className="secondary-button" disabled={busy}><KeyRound size={17} /><span>비밀번호 변경</span></button>
    </form>
    {user.role !== 'ADMIN' && <form className="form-stack" aria-label="회원 탈퇴" onSubmit={(e) => submit(e, 'withdraw')}>
      <h3>회원 탈퇴</h3><label className="field"><span>현재 비밀번호</span><input required type="password" maxLength={72} value={withdrawPassword} onChange={(e) => setWithdrawPassword(e.target.value)} autoComplete="current-password" /></label>
      <button className="danger-button reservation-cancel" disabled={busy}><UserRoundX size={17} /><span>회원 탈퇴</span></button>
    </form>}
  </div></details>;
}
