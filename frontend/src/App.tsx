import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  Building2,
  CircleUserRound,
  Home,
  LogIn,
  LogOut,
  Plus,
  RefreshCw,
  Search,
  Shield,
  TrainFront,
  Trash2,
  UserPlus,
} from 'lucide-react';
import { fetchMe, login, logout, signup } from './api/auth';
import {
  createStation,
  deleteStation,
  fetchAdminStations,
  fetchStations,
  searchStations,
  updateStation,
} from './api/stations';
import type { LoginResponse, Station, StationPayload, UserSummary } from './types/api';

type View = 'home' | 'login' | 'signup' | 'profile' | 'stations' | 'adminStations';

type Notice = {
  type: 'success' | 'error' | 'info';
  text: string;
};

const emptyStationForm: StationPayload = {
  name: '',
  code: '',
  city: '',
};

const tokenStorageKey = 'railops.accessToken';

export default function App() {
  const [view, setView] = useState<View>('home');
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(tokenStorageKey));
  const [user, setUser] = useState<UserSummary | null>(null);
  const [notice, setNotice] = useState<Notice | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token) {
      setUser(null);
      return;
    }

    fetchMe(token)
      .then(setUser)
      .catch(() => {
        localStorage.removeItem(tokenStorageKey);
        setToken(null);
        setUser(null);
      });
  }, [token]);

  function saveLogin(response: LoginResponse) {
    localStorage.setItem(tokenStorageKey, response.accessToken);
    setToken(response.accessToken);
    setUser(response.user);
    setView('profile');
    setNotice({ type: 'success', text: `${response.user.name}님 로그인됨` });
  }

  async function handleLogout() {
    setLoading(true);
    try {
      if (token) {
        await logout(token);
      }
    } catch {
      // 클라이언트 토큰 삭제를 우선한다.
    } finally {
      localStorage.removeItem(tokenStorageKey);
      setToken(null);
      setUser(null);
      setLoading(false);
      setView('home');
      setNotice({ type: 'info', text: '로그아웃됨' });
    }
  }

  const navItems = useMemo(
    () => [
      { view: 'home' as const, label: '메인', icon: Home },
      { view: 'stations' as const, label: '역 조회', icon: Search },
      { view: 'adminStations' as const, label: '역 관리', icon: Shield },
    ],
    [],
  );

  return (
    <div className="app-shell">
      <header className="topbar">
        <button className="brand" type="button" onClick={() => setView('home')} aria-label="메인으로 이동">
          <span className="brand-mark"><TrainFront size={22} /></span>
          <span>
            <strong>RailOps</strong>
            <small>Reservation Console</small>
          </span>
        </button>

        <nav className="nav-tabs" aria-label="주요 화면">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.view}
                type="button"
                className={view === item.view ? 'active' : ''}
                onClick={() => setView(item.view)}
              >
                <Icon size={17} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        <div className="account-actions">
          {user ? (
            <>
              <button className="ghost-button" type="button" onClick={() => setView('profile')}>
                <CircleUserRound size={17} />
                <span>{user.name}</span>
              </button>
              <button className="icon-button" type="button" onClick={handleLogout} disabled={loading} aria-label="로그아웃">
                <LogOut size={18} />
              </button>
            </>
          ) : (
            <>
              <button className="ghost-button" type="button" onClick={() => setView('login')}>
                <LogIn size={17} />
                <span>로그인</span>
              </button>
              <button className="primary-button compact" type="button" onClick={() => setView('signup')}>
                <UserPlus size={17} />
                <span>가입</span>
              </button>
            </>
          )}
        </div>
      </header>

      <main className="workspace">
        {notice && <NoticeBanner notice={notice} onClose={() => setNotice(null)} />}
        {view === 'home' && <HomeView onNavigate={setView} user={user} />}
        {view === 'login' && <LoginView onLoggedIn={saveLogin} setNotice={setNotice} />}
        {view === 'signup' && <SignupView onSignedUp={() => setView('login')} setNotice={setNotice} />}
        {view === 'profile' && <ProfileView user={user} onNavigate={setView} />}
        {view === 'stations' && <StationsView setNotice={setNotice} />}
        {view === 'adminStations' && <AdminStationsView token={token} user={user} setNotice={setNotice} />}
      </main>
    </div>
  );
}

function NoticeBanner({ notice, onClose }: { notice: Notice; onClose: () => void }) {
  return (
    <div className={`notice ${notice.type}`}>
      <span>{notice.text}</span>
      <button type="button" onClick={onClose} aria-label="알림 닫기">×</button>
    </div>
  );
}

function HomeView({ onNavigate, user }: { onNavigate: (view: View) => void; user: UserSummary | null }) {
  return (
    <section className="home-grid">
      <div className="hero-panel">
        <div className="rail-visual" aria-hidden="true">
          <div className="rail-line" />
          <span className="node node-a" />
          <span className="node node-b" />
          <span className="node node-c" />
          <TrainFront className="train-icon" size={42} />
        </div>
        <div className="hero-copy">
          <p className="eyebrow">RailOps</p>
          <h1>철도 예매 운영 콘솔</h1>
          <p>회원 인증과 역 관리부터 좌석 선택 예매 흐름까지 단계적으로 확장합니다.</p>
          <div className="hero-actions">
            <button className="primary-button" type="button" onClick={() => onNavigate('stations')}>
              <Search size={18} />
              <span>역 조회</span>
            </button>
            <button className="secondary-button" type="button" onClick={() => onNavigate(user ? 'adminStations' : 'login')}>
              <Shield size={18} />
              <span>관리</span>
            </button>
          </div>
        </div>
      </div>

      <div className="status-strip">
        <StatusTile label="Auth" value="Ready" />
        <StatusTile label="Station" value="Ready" />
        <StatusTile label="Route" value="Next" />
      </div>
    </section>
  );
}

function StatusTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="status-tile">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function LoginView({ onLoggedIn, setNotice }: { onLoggedIn: (response: LoginResponse) => void; setNotice: (notice: Notice) => void }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try {
      onLoggedIn(await login({ email, password }));
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : '로그인 실패' });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="form-layout">
      <PanelTitle icon={LogIn} title="로그인" />
      <form className="form-stack" onSubmit={handleSubmit}>
        <Field label="이메일" value={email} onChange={setEmail} type="email" autoComplete="email" />
        <Field label="비밀번호" value={password} onChange={setPassword} type="password" autoComplete="current-password" />
        <button className="primary-button" type="submit" disabled={submitting}>
          <LogIn size={18} />
          <span>{submitting ? '처리 중' : '로그인'}</span>
        </button>
      </form>
    </section>
  );
}

function SignupView({ onSignedUp, setNotice }: { onSignedUp: () => void; setNotice: (notice: Notice) => void }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try {
      const created = await signup({ email, password, name });
      setNotice({ type: 'success', text: `${created.name}님 가입 완료` });
      onSignedUp();
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : '회원가입 실패' });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="form-layout">
      <PanelTitle icon={UserPlus} title="회원가입" />
      <form className="form-stack" onSubmit={handleSubmit}>
        <Field label="이름" value={name} onChange={setName} autoComplete="name" />
        <Field label="이메일" value={email} onChange={setEmail} type="email" autoComplete="email" />
        <Field label="비밀번호" value={password} onChange={setPassword} type="password" autoComplete="new-password" />
        <button className="primary-button" type="submit" disabled={submitting}>
          <UserPlus size={18} />
          <span>{submitting ? '처리 중' : '가입'}</span>
        </button>
      </form>
    </section>
  );
}

function ProfileView({ user, onNavigate }: { user: UserSummary | null; onNavigate: (view: View) => void }) {
  if (!user) {
    return (
      <section className="empty-state">
        <CircleUserRound size={38} />
        <h2>로그인 필요</h2>
        <button className="primary-button" type="button" onClick={() => onNavigate('login')}>
          <LogIn size={18} />
          <span>로그인</span>
        </button>
      </section>
    );
  }

  return (
    <section className="profile-layout">
      <PanelTitle icon={CircleUserRound} title="마이페이지" />
      <dl className="definition-list">
        <div><dt>ID</dt><dd>{user.id ?? '-'}</dd></div>
        <div><dt>이메일</dt><dd>{user.email}</dd></div>
        <div><dt>이름</dt><dd>{user.name}</dd></div>
        <div><dt>권한</dt><dd>{user.role}</dd></div>
      </dl>
    </section>
  );
}

function StationsView({ setNotice }: { setNotice: (notice: Notice) => void }) {
  const [keyword, setKeyword] = useState('');
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState(false);

  async function loadStations(nextKeyword = keyword) {
    setLoading(true);
    try {
      const result = nextKeyword.trim() ? await searchStations(nextKeyword) : await fetchStations();
      setStations(result);
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : '역 조회 실패' });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadStations('');
  }, []);

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void loadStations(keyword);
  }

  return (
    <section className="data-layout">
      <PanelTitle icon={Building2} title="역 조회" />
      <form className="search-bar" onSubmit={handleSearch}>
        <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="서울" />
        <button className="primary-button compact" type="submit" disabled={loading}>
          <Search size={17} />
          <span>검색</span>
        </button>
        <button className="icon-button" type="button" onClick={() => loadStations('')} disabled={loading} aria-label="새로고침">
          <RefreshCw size={18} />
        </button>
      </form>
      <StationTable stations={stations} />
    </section>
  );
}

function AdminStationsView({ token, user, setNotice }: { token: string | null; user: UserSummary | null; setNotice: (notice: Notice) => void }) {
  const [stations, setStations] = useState<Station[]>([]);
  const [form, setForm] = useState<StationPayload>(emptyStationForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);

  async function loadStations() {
    if (!token) {
      setNotice({ type: 'error', text: '로그인이 필요합니다.' });
      return;
    }
    setLoading(true);
    try {
      setStations(await fetchAdminStations(token));
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : '관리자 역 조회 실패' });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (token && user?.role === 'ADMIN') {
      void loadStations();
    }
  }, [token, user?.role]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) {
      setNotice({ type: 'error', text: '로그인이 필요합니다.' });
      return;
    }

    setLoading(true);
    try {
      if (editingId === null) {
        await createStation(token, form);
        setNotice({ type: 'success', text: '역 등록 완료' });
      } else {
        await updateStation(token, editingId, form);
        setNotice({ type: 'success', text: '역 수정 완료' });
      }
      setForm(emptyStationForm);
      setEditingId(null);
      await loadStations();
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : '역 저장 실패' });
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(stationId: number) {
    if (!token) {
      return;
    }
    setLoading(true);
    try {
      await deleteStation(token, stationId);
      setNotice({ type: 'success', text: '역 삭제 완료' });
      await loadStations();
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : '역 삭제 실패' });
    } finally {
      setLoading(false);
    }
  }

  function startEdit(station: Station) {
    if (station.id === null) {
      return;
    }
    setEditingId(station.id);
    setForm({ name: station.name, code: station.code, city: station.city });
  }

  return (
    <section className="admin-layout">
      <PanelTitle icon={Shield} title="역 관리" />
      {user?.role !== 'ADMIN' && <div className="inline-alert">ADMIN 권한 필요</div>}
      <form className="station-form" onSubmit={handleSubmit}>
        <Field label="역 이름" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
        <Field label="역 코드" value={form.code} onChange={(value) => setForm({ ...form, code: value })} />
        <Field label="도시" value={form.city} onChange={(value) => setForm({ ...form, city: value })} />
        <div className="button-row">
          <button className="primary-button" type="submit" disabled={loading || user?.role !== 'ADMIN'}>
            <Plus size={18} />
            <span>{editingId === null ? '등록' : '수정'}</span>
          </button>
          <button className="secondary-button" type="button" onClick={() => { setEditingId(null); setForm(emptyStationForm); }}>
            <RefreshCw size={18} />
            <span>초기화</span>
          </button>
        </div>
      </form>
      <AdminStationTable stations={stations} onEdit={startEdit} onDelete={handleDelete} disabled={loading || user?.role !== 'ADMIN'} />
    </section>
  );
}

function PanelTitle({ icon: Icon, title }: { icon: typeof Building2; title: string }) {
  return (
    <div className="panel-title">
      <Icon size={21} />
      <h2>{title}</h2>
    </div>
  );
}

function Field({ label, value, onChange, type = 'text', autoComplete }: { label: string; value: string; onChange: (value: string) => void; type?: string; autoComplete?: string }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input type={type} value={value} autoComplete={autoComplete} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function StationTable({ stations }: { stations: Station[] }) {
  return (
    <div className="table-frame">
      <table>
        <thead>
          <tr>
            <th>역</th>
            <th>코드</th>
            <th>도시</th>
          </tr>
        </thead>
        <tbody>
          {stations.map((station) => (
            <tr key={`${station.id}-${station.code}`}>
              <td>{station.name}</td>
              <td><code>{station.code}</code></td>
              <td>{station.city}</td>
            </tr>
          ))}
          {stations.length === 0 && (
            <tr><td colSpan={3} className="table-empty">조회 결과 없음</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function AdminStationTable({ stations, onEdit, onDelete, disabled }: { stations: Station[]; onEdit: (station: Station) => void; onDelete: (stationId: number) => void; disabled: boolean }) {
  return (
    <div className="table-frame">
      <table>
        <thead>
          <tr>
            <th>역</th>
            <th>코드</th>
            <th>도시</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          {stations.map((station) => (
            <tr key={`${station.id}-${station.code}`}>
              <td>{station.name}</td>
              <td><code>{station.code}</code></td>
              <td>{station.city}</td>
              <td>
                <div className="row-actions">
                  <button className="secondary-button compact" type="button" onClick={() => onEdit(station)} disabled={disabled}>수정</button>
                  <button className="danger-button" type="button" onClick={() => station.id !== null && onDelete(station.id)} disabled={disabled} aria-label={`${station.name} 삭제`}>
                    <Trash2 size={16} />
                  </button>
                </div>
              </td>
            </tr>
          ))}
          {stations.length === 0 && (
            <tr><td colSpan={4} className="table-empty">조회 결과 없음</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}