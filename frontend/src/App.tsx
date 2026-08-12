import { type FormEvent, type ReactNode, useEffect, useMemo, useState } from 'react';
import {
  Armchair,
  Building2,
  CalendarClock,
  CircleUserRound,
  Home,
  Layers3,
  LogIn,
  LogOut,
  Map,
  Plus,
  RefreshCw,
  Search,
  Shield,
  TrainFront,
  Trash2,
  UserPlus,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { fetchMe, login, logout, signup } from './api/auth';
import { createStation, deleteStation, fetchAdminStations, fetchStations, searchStations, updateStation } from './api/stations';
import {
  blockScheduleSeat,
  createCar,
  createRoute,
  createSeat,
  createTrain,
  createTrainSchedule,
  deleteCar,
  deleteRoute,
  deleteSeat,
  deleteTrain,
  deleteTrainSchedule,
  fetchScheduleSeatMap,
  fetchAdminRoutes,
  holdReservation,
  simulatePaymentCancel,
  simulatePaymentFail,
  simulatePaymentSuccess,
  fetchAdminTrainSchedules,
  fetchAdminTrains,
  fetchCarsByTrain,
  fetchSeatsByCar,
  fetchTrainSchedule,
  searchTrainSchedules,
  updateCar,
  updateRoute,
  updateSeat,
  updateTrain,
  updateTrainSchedule,
  unblockScheduleSeat,
  updateTrainScheduleStatus,
} from './api/railops';
import type {
  Car,
  CarPayload,
  LoginResponse,
  ReservationHoldResponse,
  PaymentResultResponse,
  Route,
  RoutePayload,
  Seat,
  SeatPayload,
  SeatType,
  Station,
  StationPayload,
  Train,
  TrainPayload,
  TrainSchedule,
  TrainSchedulePayload,
  TrainScheduleSearchResult,
  TrainScheduleStatus,
  ScheduleSeatMap,
  UserSummary,
} from './types/api';

type View = 'home' | 'login' | 'signup' | 'profile' | 'stations' | 'schedules' | 'admin';
type AdminTab = 'stations' | 'routes' | 'trains' | 'cars' | 'seats' | 'schedules';
type Notice = { type: 'success' | 'error' | 'info'; text: string };

const emptyStationForm: StationPayload = { name: '', code: '', city: '' };
const emptyRouteForm: RoutePayload = { name: '', originStationId: 0, destinationStationId: 0 };
const emptyTrainForm: TrainPayload = { trainNo: '', trainType: '', name: '' };
const emptyCarForm: CarPayload = { carNo: 1, seatCount: 56 };
const emptySeatForm: SeatPayload = { seatNo: '', seatType: 'STANDARD' };
const emptyScheduleForm: TrainSchedulePayload = { trainId: 0, routeId: 0, operationDate: '', departureTime: '', arrivalTime: '' };
const seatTypes: SeatType[] = ['STANDARD', 'PRIORITY', 'WINDOW', 'AISLE'];
const scheduleStatuses: TrainScheduleStatus[] = ['SCHEDULED', 'DELAYED', 'CANCELED', 'COMPLETED'];
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
    fetchMe(token).then(setUser).catch(() => {
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
      if (token) await logout(token);
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

  const navItems = useMemo(() => [
    { view: 'home' as const, label: '메인', icon: Home },
    { view: 'stations' as const, label: '역 조회', icon: Search },
    { view: 'schedules' as const, label: '운행편 검색', icon: CalendarClock },
    { view: 'admin' as const, label: '관리자', icon: Shield },
  ], []);

  return (
    <div className="app-shell">
      <header className="topbar">
        <button className="brand" type="button" onClick={() => setView('home')} aria-label="메인으로 이동">
          <span className="brand-mark"><TrainFront size={22} /></span>
          <span><strong>RailOps</strong><small>Reservation Console</small></span>
        </button>
        <nav className="nav-tabs" aria-label="주요 화면">
          {navItems.map((item) => {
            const Icon = item.icon;
            return <button key={item.view} type="button" className={view === item.view ? 'active' : ''} onClick={() => setView(item.view)}><Icon size={17} /><span>{item.label}</span></button>;
          })}
        </nav>
        <div className="account-actions">
          {user ? (
            <>
              <button className="ghost-button" type="button" onClick={() => setView('profile')}><CircleUserRound size={17} /><span>{user.name}</span></button>
              <button className="icon-button" type="button" onClick={handleLogout} disabled={loading} aria-label="로그아웃"><LogOut size={18} /></button>
            </>
          ) : (
            <>
              <button className="ghost-button" type="button" onClick={() => setView('login')}><LogIn size={17} /><span>로그인</span></button>
              <button className="primary-button compact" type="button" onClick={() => setView('signup')}><UserPlus size={17} /><span>가입</span></button>
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
        {view === 'schedules' && <ScheduleSearchView token={token} setNotice={setNotice} />}
        {view === 'admin' && <AdminConsoleView token={token} user={user} setNotice={setNotice} />}
      </main>
    </div>
  );
}

function NoticeBanner({ notice, onClose }: { notice: Notice; onClose: () => void }) {
  return <div className={`notice ${notice.type}`}><span>{notice.text}</span><button type="button" onClick={onClose} aria-label="알림 닫기">×</button></div>;
}

function HomeView({ onNavigate, user }: { onNavigate: (view: View) => void; user: UserSummary | null }) {
  return (
    <section className="home-grid">
      <div className="hero-panel">
        <div className="rail-visual" aria-hidden="true"><div className="rail-line" /><span className="node node-a" /><span className="node node-b" /><span className="node node-c" /><TrainFront className="train-icon" size={42} /></div>
        <div className="hero-copy">
          <p className="eyebrow">RailOps</p>
          <h1>철도 예매 운영 콘솔</h1>
          <p>회원 인증, 기준 데이터 관리, 운행편 검색까지 구현된 상태입니다.</p>
          <div className="hero-actions">
            <button className="primary-button" type="button" onClick={() => onNavigate('schedules')}><CalendarClock size={18} /><span>운행편 검색</span></button>
            <button className="secondary-button" type="button" onClick={() => onNavigate(user ? 'admin' : 'login')}><Shield size={18} /><span>관리자</span></button>
          </div>
        </div>
      </div>
      <div className="status-strip expanded"><StatusTile label="Auth" value="Ready" /><StatusTile label="Station/Route" value="Ready" /><StatusTile label="Train/Seat" value="Ready" /><StatusTile label="Schedule" value="Ready" /></div>
    </section>
  );
}

function StatusTile({ label, value }: { label: string; value: string }) {
  return <div className="status-tile"><span>{label}</span><strong>{value}</strong></div>;
}

function LoginView({ onLoggedIn, setNotice }: { onLoggedIn: (response: LoginResponse) => void; setNotice: (notice: Notice) => void }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try { onLoggedIn(await login({ email, password })); }
    catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '로그인 실패' }); }
    finally { setSubmitting(false); }
  }
  return <section className="form-layout"><PanelTitle icon={LogIn} title="로그인" /><form className="form-stack" onSubmit={handleSubmit}><Field label="이메일" value={email} onChange={setEmail} type="email" autoComplete="email" /><Field label="비밀번호" value={password} onChange={setPassword} type="password" autoComplete="current-password" /><button className="primary-button" type="submit" disabled={submitting}><LogIn size={18} /><span>{submitting ? '처리 중' : '로그인'}</span></button></form></section>;
}

function SignupView({ onSignedUp, setNotice }: { onSignedUp: () => void; setNotice: (notice: Notice) => void }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    try { const created = await signup({ email, password, name }); setNotice({ type: 'success', text: `${created.name}님 가입 완료` }); onSignedUp(); }
    catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '회원가입 실패' }); }
    finally { setSubmitting(false); }
  }
  return <section className="form-layout"><PanelTitle icon={UserPlus} title="회원가입" /><form className="form-stack" onSubmit={handleSubmit}><Field label="이름" value={name} onChange={setName} autoComplete="name" /><Field label="이메일" value={email} onChange={setEmail} type="email" autoComplete="email" /><Field label="비밀번호" value={password} onChange={setPassword} type="password" autoComplete="new-password" /><button className="primary-button" type="submit" disabled={submitting}><UserPlus size={18} /><span>{submitting ? '처리 중' : '가입'}</span></button></form></section>;
}
function ProfileView({ user, onNavigate }: { user: UserSummary | null; onNavigate: (view: View) => void }) {
  if (!user) {
    return <section className="empty-state"><CircleUserRound size={38} /><h2>로그인 필요</h2><button className="primary-button" type="button" onClick={() => onNavigate('login')}><LogIn size={18} /><span>로그인</span></button></section>;
  }
  return <section className="profile-layout"><PanelTitle icon={CircleUserRound} title="마이페이지" /><dl className="definition-list"><div><dt>ID</dt><dd>{user.id ?? '-'}</dd></div><div><dt>이메일</dt><dd>{user.email}</dd></div><div><dt>이름</dt><dd>{user.name}</dd></div><div><dt>권한</dt><dd>{user.role}</dd></div></dl></section>;
}

function StationsView({ setNotice }: { setNotice: (notice: Notice) => void }) {
  const [keyword, setKeyword] = useState('');
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState(false);
  async function loadStations(nextKeyword = keyword) {
    setLoading(true);
    try { setStations(nextKeyword.trim() ? await searchStations(nextKeyword) : await fetchStations()); }
    catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '역 조회 실패' }); }
    finally { setLoading(false); }
  }
  useEffect(() => { void loadStations(''); }, []);
  return <section className="data-layout"><PanelTitle icon={Building2} title="역 조회" /><form className="search-bar" onSubmit={(event) => { event.preventDefault(); void loadStations(keyword); }}><input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="서울" /><button className="primary-button compact" type="submit" disabled={loading}><Search size={17} /><span>검색</span></button><button className="icon-button" type="button" onClick={() => loadStations('')} disabled={loading} aria-label="새로고침"><RefreshCw size={18} /></button></form><StationTable stations={stations} /></section>;
}

function ScheduleSearchView({ token, setNotice }: { token: string | null; setNotice: (notice: Notice) => void }) {
  const [from, setFrom] = useState('SEOUL');
  const [to, setTo] = useState('BUSAN');
  const [date, setDate] = useState('2026-08-01');
  const [schedules, setSchedules] = useState<TrainScheduleSearchResult[]>([]);
  const [detail, setDetail] = useState<TrainSchedule | null>(null);
  const [seatMap, setSeatMap] = useState<ScheduleSeatMap | null>(null);
  const [holdResult, setHoldResult] = useState<ReservationHoldResponse | null>(null);
  const [paymentResult, setPaymentResult] = useState<PaymentResultResponse | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSearch(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault();
    setLoading(true);
    setDetail(null);
    setSeatMap(null);
    setHoldResult(null);
    setPaymentResult(null);
    try { setSchedules(await searchTrainSchedules(from, to, date)); }
    catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '운행편 검색 실패' }); }
    finally { setLoading(false); }
  }

  async function loadDetail(scheduleId: number | null) {
    if (scheduleId === null) return;
    setLoading(true);
    setHoldResult(null);
    setPaymentResult(null);
    try {
      const [nextDetail, nextSeatMap] = await Promise.all([fetchTrainSchedule(scheduleId), fetchScheduleSeatMap(scheduleId)]);
      setDetail(nextDetail);
      setSeatMap(nextSeatMap);
    } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '운행편 상세 조회 실패' }); }
    finally { setLoading(false); }
  }

  async function refreshSeatMap() {
    if (seatMap?.scheduleId) setSeatMap(await fetchScheduleSeatMap(seatMap.scheduleId));
  }

  async function handleHoldSeat(scheduleSeatId: number) {
    if (!token) {
      setNotice({ type: 'error', text: '좌석 HOLD는 로그인 후 사용할 수 있습니다.' });
      return;
    }
    if (!seatMap?.scheduleId) return;
    setLoading(true);
    setPaymentResult(null);
    try {
      const result = await holdReservation(token, { scheduleId: seatMap.scheduleId, scheduleSeatIds: [scheduleSeatId] });
      setHoldResult(result);
      setSeatMap(await fetchScheduleSeatMap(seatMap.scheduleId));
      setNotice({ type: 'success', text: '좌석 HOLD 완료' });
    } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '좌석 HOLD 실패' }); }
    finally { setLoading(false); }
  }

  async function handlePayment(action: 'success' | 'fail' | 'cancel') {
    if (!token || !holdResult?.paymentId) return;
    setLoading(true);
    try {
      const result = action === 'success'
        ? await simulatePaymentSuccess(token, holdResult.paymentId)
        : action === 'fail'
          ? await simulatePaymentFail(token, holdResult.paymentId)
          : await simulatePaymentCancel(token, holdResult.paymentId);
      setPaymentResult(result);
      await refreshSeatMap();
      setNotice({ type: 'success', text: `결제 ${result.paymentStatus}` });
    } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '결제 처리 실패' }); }
    finally { setLoading(false); }
  }

  return (
    <section className="data-layout">
      <PanelTitle icon={CalendarClock} title="운행편 검색" />
      <form className="search-bar schedule-search" onSubmit={handleSearch}>
        <Field label="출발역 코드" value={from} onChange={setFrom} />
        <Field label="도착역 코드" value={to} onChange={setTo} />
        <Field label="운행일" value={date} onChange={setDate} type="date" />
        <button className="primary-button compact" type="submit" disabled={loading}><Search size={17} /><span>검색</span></button>
      </form>
      <div className="table-frame"><table><thead><tr><th>열차</th><th>노선</th><th>구간</th><th>출발</th><th>도착</th><th>상태</th><th>작업</th></tr></thead><tbody>
        {schedules.map((schedule) => <tr key={schedule.scheduleId ?? `${schedule.trainNo}-${schedule.departureTime}`}><td><code>{schedule.trainNo}</code> {schedule.trainType}</td><td>{schedule.routeName}</td><td>{schedule.origin} -&gt; {schedule.destination}</td><td>{formatDateTime(schedule.departureTime)}</td><td>{formatDateTime(schedule.arrivalTime)}</td><td><StatusBadge status={schedule.status} /></td><td><button className="secondary-button compact" type="button" onClick={() => loadDetail(schedule.scheduleId)} disabled={loading}>상세</button></td></tr>)}
        {schedules.length === 0 && <EmptyRow colSpan={7} />}
      </tbody></table></div>
      {detail && <dl className="definition-list detail-list"><div><dt>운행편 ID</dt><dd>{detail.id ?? '-'}</dd></div><div><dt>열차</dt><dd>{detail.trainNo}</dd></div><div><dt>노선</dt><dd>{detail.routeName}</dd></div><div><dt>상태</dt><dd>{detail.status}</dd></div></dl>}
      {holdResult && <div className="inline-alert payment-panel"><span>예약 {holdResult.reservationNo} / 결제 {holdResult.paymentNo} / 만료 {formatDateTime(holdResult.holdExpiresAt)}</span><div className="button-row"><button className="primary-button compact" type="button" disabled={loading || Boolean(paymentResult)} onClick={() => handlePayment('success')}>결제 성공</button><button className="secondary-button compact" type="button" disabled={loading || Boolean(paymentResult)} onClick={() => handlePayment('fail')}>결제 실패</button><button className="danger-button" type="button" disabled={loading || Boolean(paymentResult)} onClick={() => handlePayment('cancel')}>취소</button></div></div>}
      {paymentResult && <div className="inline-alert">결제 {paymentResult.paymentStatus} / 예매 {paymentResult.reservationStatus} / 처리 시각 {formatDateTime(paymentResult.processedAt)}</div>}
      {seatMap && <SeatMapPanel seatMap={seatMap} canHold={Boolean(token)} onHold={handleHoldSeat} disabled={loading} />}
    </section>
  );
}
function AdminConsoleView({ token, user, setNotice }: { token: string | null; user: UserSummary | null; setNotice: (notice: Notice) => void }) {
  const [tab, setTab] = useState<AdminTab>('stations');
  const tabs = [
    { tab: 'stations' as const, label: '역', icon: Building2 },
    { tab: 'routes' as const, label: '노선', icon: Map },
    { tab: 'trains' as const, label: '열차', icon: TrainFront },
    { tab: 'cars' as const, label: '객차', icon: Layers3 },
    { tab: 'seats' as const, label: '좌석', icon: Armchair },
    { tab: 'schedules' as const, label: '운행편', icon: CalendarClock },
  ];
  if (!token || user?.role !== 'ADMIN') return <section className="admin-layout"><PanelTitle icon={Shield} title="관리자 콘솔" /><div className="inline-alert">ADMIN 권한으로 로그인해야 관리자 기능을 사용할 수 있습니다.</div></section>;
  return <section className="admin-layout"><PanelTitle icon={Shield} title="관리자 콘솔" /><nav className="admin-tabs" aria-label="관리자 기능">{tabs.map((item) => { const Icon = item.icon; return <button key={item.tab} type="button" className={tab === item.tab ? 'active' : ''} onClick={() => setTab(item.tab)}><Icon size={17} /><span>{item.label}</span></button>; })}</nav>{tab === 'stations' && <AdminStationsView token={token} setNotice={setNotice} />}{tab === 'routes' && <AdminRoutesView token={token} setNotice={setNotice} />}{tab === 'trains' && <AdminTrainsView token={token} setNotice={setNotice} />}{tab === 'cars' && <AdminCarsView token={token} setNotice={setNotice} />}{tab === 'seats' && <AdminSeatsView token={token} setNotice={setNotice} />}{tab === 'schedules' && <AdminSchedulesView token={token} setNotice={setNotice} />}</section>;
}

function AdminStationsView({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [stations, setStations] = useState<Station[]>([]);
  const [form, setForm] = useState<StationPayload>(emptyStationForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  async function loadStations() { setLoading(true); try { setStations(await fetchAdminStations(token)); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '관리자 역 조회 실패' }); } finally { setLoading(false); } }
  useEffect(() => { void loadStations(); }, [token]);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setLoading(true); try { if (editingId === null) { await createStation(token, form); setNotice({ type: 'success', text: '역 등록 완료' }); } else { await updateStation(token, editingId, form); setNotice({ type: 'success', text: '역 수정 완료' }); } setForm(emptyStationForm); setEditingId(null); await loadStations(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '역 저장 실패' }); } finally { setLoading(false); } }
  async function handleDelete(stationId: number) { setLoading(true); try { await deleteStation(token, stationId); setNotice({ type: 'success', text: '역 삭제 완료' }); await loadStations(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '역 삭제 실패' }); } finally { setLoading(false); } }
  return <div className="admin-pane"><form className="management-form station-form" onSubmit={handleSubmit}><Field label="역 이름" value={form.name} onChange={(value) => setForm({ ...form, name: value })} /><Field label="역 코드" value={form.code} onChange={(value) => setForm({ ...form, code: value })} /><Field label="도시" value={form.city} onChange={(value) => setForm({ ...form, city: value })} /><FormActions editing={editingId !== null} loading={loading} onReset={() => { setEditingId(null); setForm(emptyStationForm); }} /></form><StationTable stations={stations} onEdit={(station) => { setEditingId(station.id); setForm({ name: station.name, code: station.code, city: station.city }); }} onDelete={handleDelete} disabled={loading} /></div>;
}
function AdminRoutesView({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [stations, setStations] = useState<Station[]>([]);
  const [routes, setRoutes] = useState<Route[]>([]);
  const [form, setForm] = useState<RoutePayload>(emptyRouteForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  async function loadAll() {
    setLoading(true);
    try {
      const [nextStations, nextRoutes] = await Promise.all([fetchAdminStations(token), fetchAdminRoutes(token)]);
      setStations(nextStations); setRoutes(nextRoutes);
      if (nextStations.length > 0 && form.originStationId === 0) setForm((current) => ({ ...current, originStationId: nextStations[0].id ?? 0, destinationStationId: nextStations[1]?.id ?? nextStations[0].id ?? 0 }));
    } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '노선 데이터 조회 실패' }); }
    finally { setLoading(false); }
  }
  useEffect(() => { void loadAll(); }, [token]);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setLoading(true); try { if (editingId === null) { await createRoute(token, form); setNotice({ type: 'success', text: '노선 등록 완료' }); } else { await updateRoute(token, editingId, form); setNotice({ type: 'success', text: '노선 수정 완료' }); } setEditingId(null); await loadAll(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '노선 저장 실패' }); } finally { setLoading(false); } }
  async function handleDelete(routeId: number) { setLoading(true); try { await deleteRoute(token, routeId); setNotice({ type: 'success', text: '노선 삭제 완료' }); await loadAll(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '노선 삭제 실패' }); } finally { setLoading(false); } }
  return <div className="admin-pane"><form className="management-form route-form" onSubmit={handleSubmit}><Field label="노선명" value={form.name} onChange={(value) => setForm({ ...form, name: value })} /><SelectField label="출발역" value={String(form.originStationId)} onChange={(value) => setForm({ ...form, originStationId: Number(value) })}>{stations.map((station) => <option key={station.id} value={station.id ?? 0}>{station.name} ({station.code})</option>)}</SelectField><SelectField label="도착역" value={String(form.destinationStationId)} onChange={(value) => setForm({ ...form, destinationStationId: Number(value) })}>{stations.map((station) => <option key={station.id} value={station.id ?? 0}>{station.name} ({station.code})</option>)}</SelectField><FormActions editing={editingId !== null} loading={loading} onReset={() => { setEditingId(null); setForm(emptyRouteForm); }} /></form><RouteTable routes={routes} onEdit={(route) => { setEditingId(route.id); setForm({ name: route.name, originStationId: route.originStationId ?? 0, destinationStationId: route.destinationStationId ?? 0 }); }} onDelete={handleDelete} disabled={loading} /></div>;
}

function AdminTrainsView({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [trains, setTrains] = useState<Train[]>([]);
  const [form, setForm] = useState<TrainPayload>(emptyTrainForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  async function loadTrains() { setLoading(true); try { setTrains(await fetchAdminTrains(token)); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '열차 조회 실패' }); } finally { setLoading(false); } }
  useEffect(() => { void loadTrains(); }, [token]);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setLoading(true); try { if (editingId === null) { await createTrain(token, form); setNotice({ type: 'success', text: '열차 등록 완료' }); } else { await updateTrain(token, editingId, form); setNotice({ type: 'success', text: '열차 수정 완료' }); } setForm(emptyTrainForm); setEditingId(null); await loadTrains(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '열차 저장 실패' }); } finally { setLoading(false); } }
  async function handleDelete(trainId: number) { setLoading(true); try { await deleteTrain(token, trainId); setNotice({ type: 'success', text: '열차 삭제 완료' }); await loadTrains(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '열차 삭제 실패' }); } finally { setLoading(false); } }
  return <div className="admin-pane"><form className="management-form train-form" onSubmit={handleSubmit}><Field label="열차 번호" value={form.trainNo} onChange={(value) => setForm({ ...form, trainNo: value })} /><Field label="타입" value={form.trainType} onChange={(value) => setForm({ ...form, trainType: value })} /><Field label="이름" value={form.name} onChange={(value) => setForm({ ...form, name: value })} /><FormActions editing={editingId !== null} loading={loading} onReset={() => { setEditingId(null); setForm(emptyTrainForm); }} /></form><TrainTable trains={trains} onEdit={(train) => { setEditingId(train.id); setForm({ trainNo: train.trainNo, trainType: train.trainType, name: train.name }); }} onDelete={handleDelete} disabled={loading} /></div>;
}

function AdminCarsView({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [trains, setTrains] = useState<Train[]>([]);
  const [selectedTrainId, setSelectedTrainId] = useState(0);
  const [cars, setCars] = useState<Car[]>([]);
  const [form, setForm] = useState<CarPayload>(emptyCarForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  useEffect(() => { fetchAdminTrains(token).then((nextTrains) => { setTrains(nextTrains); if (nextTrains.length > 0) setSelectedTrainId(nextTrains[0].id ?? 0); }).catch((error) => setNotice({ type: 'error', text: error instanceof Error ? error.message : '열차 조회 실패' })); }, [token]);
  async function loadCars(trainId = selectedTrainId) { if (!trainId) { setCars([]); return; } setLoading(true); try { setCars(await fetchCarsByTrain(token, trainId)); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '객차 조회 실패' }); } finally { setLoading(false); } }
  useEffect(() => { void loadCars(selectedTrainId); }, [selectedTrainId]);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (!selectedTrainId) { setNotice({ type: 'error', text: '열차를 먼저 선택해야 합니다.' }); return; } setLoading(true); try { if (editingId === null) { await createCar(token, selectedTrainId, form); setNotice({ type: 'success', text: '객차 등록 완료' }); } else { await updateCar(token, editingId, form); setNotice({ type: 'success', text: '객차 수정 완료' }); } setForm(emptyCarForm); setEditingId(null); await loadCars(selectedTrainId); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '객차 저장 실패' }); } finally { setLoading(false); } }
  async function handleDelete(carId: number) { setLoading(true); try { await deleteCar(token, carId); setNotice({ type: 'success', text: '객차 삭제 완료' }); await loadCars(selectedTrainId); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '객차 삭제 실패' }); } finally { setLoading(false); } }
  return <div className="admin-pane"><form className="management-form car-form" onSubmit={handleSubmit}><SelectField label="열차" value={String(selectedTrainId)} onChange={(value) => { setSelectedTrainId(Number(value)); setEditingId(null); }}><option value="0">열차 선택</option>{trains.map((train) => <option key={train.id} value={train.id ?? 0}>{train.trainNo} ({train.trainType})</option>)}</SelectField><Field label="객차 번호" value={String(form.carNo)} onChange={(value) => setForm({ ...form, carNo: toPositiveNumber(value) })} type="number" /><Field label="좌석 수" value={String(form.seatCount)} onChange={(value) => setForm({ ...form, seatCount: toPositiveNumber(value) })} type="number" /><FormActions editing={editingId !== null} loading={loading} onReset={() => { setEditingId(null); setForm(emptyCarForm); }} /></form><CarTable cars={cars} onEdit={(car) => { setEditingId(car.id); setForm({ carNo: car.carNo, seatCount: car.seatCount }); }} onDelete={handleDelete} disabled={loading} /></div>;
}

function AdminSeatsView({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [trains, setTrains] = useState<Train[]>([]);
  const [cars, setCars] = useState<Car[]>([]);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedTrainId, setSelectedTrainId] = useState(0);
  const [selectedCarId, setSelectedCarId] = useState(0);
  const [form, setForm] = useState<SeatPayload>(emptySeatForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  useEffect(() => { fetchAdminTrains(token).then((nextTrains) => { setTrains(nextTrains); if (nextTrains.length > 0) setSelectedTrainId(nextTrains[0].id ?? 0); }).catch((error) => setNotice({ type: 'error', text: error instanceof Error ? error.message : '열차 조회 실패' })); }, [token]);
  useEffect(() => { if (!selectedTrainId) { setCars([]); setSelectedCarId(0); return; } fetchCarsByTrain(token, selectedTrainId).then((nextCars) => { setCars(nextCars); setSelectedCarId(nextCars[0]?.id ?? 0); }).catch((error) => setNotice({ type: 'error', text: error instanceof Error ? error.message : '객차 조회 실패' })); }, [token, selectedTrainId]);
  async function loadSeats(carId = selectedCarId) { if (!carId) { setSeats([]); return; } setLoading(true); try { setSeats(await fetchSeatsByCar(token, carId)); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '좌석 조회 실패' }); } finally { setLoading(false); } }
  useEffect(() => { void loadSeats(selectedCarId); }, [selectedCarId]);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (!selectedCarId) { setNotice({ type: 'error', text: '객차를 먼저 선택해야 합니다.' }); return; } setLoading(true); try { if (editingId === null) { await createSeat(token, selectedCarId, form); setNotice({ type: 'success', text: '좌석 등록 완료' }); } else { await updateSeat(token, editingId, form); setNotice({ type: 'success', text: '좌석 수정 완료' }); } setForm(emptySeatForm); setEditingId(null); await loadSeats(selectedCarId); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '좌석 저장 실패' }); } finally { setLoading(false); } }
  async function handleDelete(seatId: number) { setLoading(true); try { await deleteSeat(token, seatId); setNotice({ type: 'success', text: '좌석 삭제 완료' }); await loadSeats(selectedCarId); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '좌석 삭제 실패' }); } finally { setLoading(false); } }
  return <div className="admin-pane"><form className="management-form seat-form" onSubmit={handleSubmit}><SelectField label="열차" value={String(selectedTrainId)} onChange={(value) => { setSelectedTrainId(Number(value)); setEditingId(null); }}><option value="0">열차 선택</option>{trains.map((train) => <option key={train.id} value={train.id ?? 0}>{train.trainNo}</option>)}</SelectField><SelectField label="객차" value={String(selectedCarId)} onChange={(value) => { setSelectedCarId(Number(value)); setEditingId(null); }}><option value="0">객차 선택</option>{cars.map((car) => <option key={car.id} value={car.id ?? 0}>{car.carNo}호차</option>)}</SelectField><Field label="좌석 번호" value={form.seatNo} onChange={(value) => setForm({ ...form, seatNo: value })} /><SelectField label="좌석 타입" value={form.seatType} onChange={(value) => setForm({ ...form, seatType: value as SeatType })}>{seatTypes.map((seatType) => <option key={seatType} value={seatType}>{seatType}</option>)}</SelectField><FormActions editing={editingId !== null} loading={loading} onReset={() => { setEditingId(null); setForm(emptySeatForm); }} /></form><SeatTable seats={seats} onEdit={(seat) => { setEditingId(seat.id); setForm({ seatNo: seat.seatNo, seatType: seat.seatType }); }} onDelete={handleDelete} disabled={loading} /></div>;
}
function AdminSchedulesView({ token, setNotice }: { token: string; setNotice: (notice: Notice) => void }) {
  const [trains, setTrains] = useState<Train[]>([]);
  const [routes, setRoutes] = useState<Route[]>([]);
  const [schedules, setSchedules] = useState<TrainSchedule[]>([]);
  const [selectedScheduleId, setSelectedScheduleId] = useState(0);
  const [seatMap, setSeatMap] = useState<ScheduleSeatMap | null>(null);
  const [form, setForm] = useState<TrainSchedulePayload>(emptyScheduleForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  async function loadAll() {
    setLoading(true);
    try {
      const [nextTrains, nextRoutes, nextSchedules] = await Promise.all([fetchAdminTrains(token), fetchAdminRoutes(token), fetchAdminTrainSchedules(token)]);
      setTrains(nextTrains);
      setRoutes(nextRoutes);
      setSchedules(nextSchedules);
      setSelectedScheduleId((current) => current || nextSchedules[0]?.id || 0);
      setForm((current) => ({ ...current, trainId: current.trainId || nextTrains[0]?.id || 0, routeId: current.routeId || nextRoutes[0]?.id || 0 }));
    } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '운행편 데이터 조회 실패' }); }
    finally { setLoading(false); }
  }
  useEffect(() => { void loadAll(); }, [token]);
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    try {
      const saved = editingId === null ? await createTrainSchedule(token, form) : await updateTrainSchedule(token, editingId, form);
      setNotice({ type: 'success', text: editingId === null ? '운행편 등록 완료' : '운행편 수정 완료' });
      setEditingId(null);
      if (saved.id !== null) setSelectedScheduleId(saved.id);
      await loadAll();
    } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '운행편 저장 실패' }); }
    finally { setLoading(false); }
  }
  async function handleStatus(scheduleId: number, status: TrainScheduleStatus) { setLoading(true); try { await updateTrainScheduleStatus(token, scheduleId, status); setNotice({ type: 'success', text: '운행편 상태 변경 완료' }); await loadAll(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '상태 변경 실패' }); } finally { setLoading(false); } }
  async function handleDelete(scheduleId: number) { setLoading(true); try { await deleteTrainSchedule(token, scheduleId); setNotice({ type: 'success', text: '운행편 삭제 완료' }); setSeatMap(null); await loadAll(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '운행편 삭제 실패' }); } finally { setLoading(false); } }
  async function loadScheduleSeats(scheduleId = selectedScheduleId) {
    if (!scheduleId) return;
    setLoading(true);
    try { setSeatMap(await fetchScheduleSeatMap(scheduleId)); }
    catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '운행편 좌석 조회 실패' }); }
    finally { setLoading(false); }
  }
  async function handleBlockSeat(scheduleSeatId: number) { setLoading(true); try { await blockScheduleSeat(token, scheduleSeatId); setNotice({ type: 'success', text: '좌석 BLOCK 완료' }); await loadScheduleSeats(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '좌석 BLOCK 실패' }); } finally { setLoading(false); } }
  async function handleUnblockSeat(scheduleSeatId: number) { setLoading(true); try { await unblockScheduleSeat(token, scheduleSeatId); setNotice({ type: 'success', text: '좌석 UNBLOCK 완료' }); await loadScheduleSeats(); } catch (error) { setNotice({ type: 'error', text: error instanceof Error ? error.message : '좌석 UNBLOCK 실패' }); } finally { setLoading(false); } }
  return <div className="admin-pane"><form className="management-form schedule-form" onSubmit={handleSubmit}><SelectField label="열차" value={String(form.trainId)} onChange={(value) => setForm({ ...form, trainId: Number(value) })} disabled={editingId !== null}><option value="0">열차 선택</option>{trains.map((train) => <option key={train.id} value={train.id ?? 0}>{train.trainNo}</option>)}</SelectField><SelectField label="노선" value={String(form.routeId)} onChange={(value) => setForm({ ...form, routeId: Number(value) })}><option value="0">노선 선택</option>{routes.map((route) => <option key={route.id} value={route.id ?? 0}>{route.name}</option>)}</SelectField><Field label="운행일" value={form.operationDate} onChange={(value) => setForm({ ...form, operationDate: value })} type="date" /><Field label="출발" value={form.departureTime} onChange={(value) => setForm({ ...form, departureTime: value })} type="datetime-local" /><Field label="도착" value={form.arrivalTime} onChange={(value) => setForm({ ...form, arrivalTime: value })} type="datetime-local" /><FormActions editing={editingId !== null} loading={loading} onReset={() => { setEditingId(null); setForm(emptyScheduleForm); }} /></form><ScheduleTable schedules={schedules} onEdit={(schedule) => { setEditingId(schedule.id); setForm({ trainId: schedule.trainId ?? 0, routeId: schedule.routeId ?? 0, operationDate: schedule.operationDate, departureTime: toDateTimeLocal(schedule.departureTime), arrivalTime: toDateTimeLocal(schedule.arrivalTime) }); }} onStatus={handleStatus} onDelete={handleDelete} disabled={loading} /><div className="schedule-seat-tools"><SelectField label="좌석 조회 운행편" value={String(selectedScheduleId)} onChange={(value) => { setSelectedScheduleId(Number(value)); setSeatMap(null); }}><option value="0">운행편 선택</option>{schedules.map((schedule) => <option key={schedule.id} value={schedule.id ?? 0}>{schedule.trainNo} {formatDateTime(schedule.departureTime)}</option>)}</SelectField><button className="secondary-button" type="button" onClick={() => loadScheduleSeats()} disabled={loading || !selectedScheduleId}><Armchair size={18} /><span>좌석 조회</span></button></div>{seatMap && <SeatMapPanel seatMap={seatMap} canManage onBlock={handleBlockSeat} onUnblock={handleUnblockSeat} disabled={loading} />}</div>;
}
function PanelTitle({ icon: Icon, title }: { icon: LucideIcon; title: string }) {
  return <div className="panel-title"><Icon size={21} /><h2>{title}</h2></div>;
}

function Field({ label, value, onChange, type = 'text', autoComplete }: { label: string; value: string; onChange: (value: string) => void; type?: string; autoComplete?: string }) {
  return <label className="field"><span>{label}</span><input type={type} value={value} autoComplete={autoComplete} onChange={(event) => onChange(event.target.value)} /></label>;
}

function SelectField({ label, value, onChange, children, disabled = false }: { label: string; value: string; onChange: (value: string) => void; children: ReactNode; disabled?: boolean }) {
  return <label className="field"><span>{label}</span><select value={value} onChange={(event) => onChange(event.target.value)} disabled={disabled}>{children}</select></label>;
}

function FormActions({ editing, loading, onReset }: { editing: boolean; loading: boolean; onReset: () => void }) {
  return <div className="button-row form-actions"><button className="primary-button" type="submit" disabled={loading}><Plus size={18} /><span>{editing ? '수정' : '등록'}</span></button><button className="secondary-button" type="button" onClick={onReset} disabled={loading}><RefreshCw size={18} /><span>초기화</span></button></div>;
}

function StationTable({ stations, onEdit, onDelete, disabled = false }: { stations: Station[]; onEdit?: (station: Station) => void; onDelete?: (stationId: number) => void; disabled?: boolean }) {
  return <div className="table-frame"><table><thead><tr><th>역</th><th>코드</th><th>도시</th>{onEdit && <th>작업</th>}</tr></thead><tbody>{stations.map((station) => <tr key={`${station.id}-${station.code}`}><td>{station.name}</td><td><code>{station.code}</code></td><td>{station.city}</td>{onEdit && <ActionCell item={station} id={station.id} onEdit={onEdit} onDelete={onDelete} disabled={disabled} label={station.name} />}</tr>)}{stations.length === 0 && <EmptyRow colSpan={onEdit ? 4 : 3} />}</tbody></table></div>;
}

function RouteTable({ routes, onEdit, onDelete, disabled }: { routes: Route[]; onEdit: (route: Route) => void; onDelete: (routeId: number) => void; disabled: boolean }) {
  return <div className="table-frame"><table><thead><tr><th>노선</th><th>출발</th><th>도착</th><th>작업</th></tr></thead><tbody>{routes.map((route) => <tr key={route.id}><td>{route.name}</td><td>{route.originStationName} <code>{route.originStationCode}</code></td><td>{route.destinationStationName} <code>{route.destinationStationCode}</code></td><ActionCell item={route} id={route.id} onEdit={onEdit} onDelete={onDelete} disabled={disabled} label={route.name} /></tr>)}{routes.length === 0 && <EmptyRow colSpan={4} />}</tbody></table></div>;
}

function TrainTable({ trains, onEdit, onDelete, disabled }: { trains: Train[]; onEdit: (train: Train) => void; onDelete: (trainId: number) => void; disabled: boolean }) {
  return <div className="table-frame"><table><thead><tr><th>열차 번호</th><th>타입</th><th>이름</th><th>작업</th></tr></thead><tbody>{trains.map((train) => <tr key={train.id}><td><code>{train.trainNo}</code></td><td>{train.trainType}</td><td>{train.name}</td><ActionCell item={train} id={train.id} onEdit={onEdit} onDelete={onDelete} disabled={disabled} label={train.trainNo} /></tr>)}{trains.length === 0 && <EmptyRow colSpan={4} />}</tbody></table></div>;
}

function CarTable({ cars, onEdit, onDelete, disabled }: { cars: Car[]; onEdit: (car: Car) => void; onDelete: (carId: number) => void; disabled: boolean }) {
  return <div className="table-frame"><table><thead><tr><th>열차</th><th>객차</th><th>좌석 수</th><th>작업</th></tr></thead><tbody>{cars.map((car) => <tr key={car.id}><td><code>{car.trainNo}</code></td><td>{car.carNo}호차</td><td>{car.seatCount}</td><ActionCell item={car} id={car.id} onEdit={onEdit} onDelete={onDelete} disabled={disabled} label={`${car.carNo}호차`} /></tr>)}{cars.length === 0 && <EmptyRow colSpan={4} />}</tbody></table></div>;
}

function SeatTable({ seats, onEdit, onDelete, disabled }: { seats: Seat[]; onEdit: (seat: Seat) => void; onDelete: (seatId: number) => void; disabled: boolean }) {
  return <div className="table-frame"><table><thead><tr><th>객차</th><th>좌석</th><th>타입</th><th>작업</th></tr></thead><tbody>{seats.map((seat) => <tr key={seat.id}><td>{seat.carNo}호차</td><td><code>{seat.seatNo}</code></td><td>{seat.seatType}</td><ActionCell item={seat} id={seat.id} onEdit={onEdit} onDelete={onDelete} disabled={disabled} label={seat.seatNo} /></tr>)}{seats.length === 0 && <EmptyRow colSpan={4} />}</tbody></table></div>;
}

function ScheduleTable({ schedules, onEdit, onStatus, onDelete, disabled }: { schedules: TrainSchedule[]; onEdit: (schedule: TrainSchedule) => void; onStatus: (scheduleId: number, status: TrainScheduleStatus) => void; onDelete: (scheduleId: number) => void; disabled: boolean }) {
  return <div className="table-frame"><table><thead><tr><th>열차</th><th>노선</th><th>구간</th><th>출발</th><th>도착</th><th>상태</th><th>작업</th></tr></thead><tbody>{schedules.map((schedule) => <tr key={schedule.id}><td><code>{schedule.trainNo}</code></td><td>{schedule.routeName}</td><td>{schedule.originStationName} -&gt; {schedule.destinationStationName}</td><td>{formatDateTime(schedule.departureTime)}</td><td>{formatDateTime(schedule.arrivalTime)}</td><td><select className="inline-select" value={schedule.status} onChange={(event) => schedule.id !== null && onStatus(schedule.id, event.target.value as TrainScheduleStatus)} disabled={disabled}>{scheduleStatuses.map((status) => <option key={status} value={status}>{status}</option>)}</select></td><ActionCell item={schedule} id={schedule.id} onEdit={onEdit} onDelete={onDelete} disabled={disabled} label={schedule.trainNo} /></tr>)}{schedules.length === 0 && <EmptyRow colSpan={7} />}</tbody></table></div>;
}

function ActionCell<T>({ item, id, onEdit, onDelete, disabled, label }: { item: T; id: number | null; onEdit: (item: T) => void; onDelete?: (id: number) => void; disabled: boolean; label: string }) {
  return <td><div className="row-actions"><button className="secondary-button compact" type="button" onClick={() => onEdit(item)} disabled={disabled || id === null}>수정</button>{onDelete && <button className="danger-button" type="button" onClick={() => id !== null && onDelete(id)} disabled={disabled || id === null} aria-label={`${label} 삭제`}><Trash2 size={16} /></button>}</div></td>;
}

function SeatMapPanel({ seatMap, canManage = false, canHold = false, onBlock, onUnblock, onHold, disabled = false }: { seatMap: ScheduleSeatMap; canManage?: boolean; canHold?: boolean; onBlock?: (scheduleSeatId: number) => void; onUnblock?: (scheduleSeatId: number) => void; onHold?: (scheduleSeatId: number) => void; disabled?: boolean }) {
  return <div className="seat-map-panel">{seatMap.cars.map((car) => <section className="seat-car" key={car.carId ?? car.carNo}><h3>{car.carNo}호차</h3><div className="seat-grid">{car.seats.map((seat) => <div className={`seat-cell ${seat.status.toLowerCase()}`} key={seat.scheduleSeatId ?? seat.seatNo}><strong>{seat.seatNo}</strong><span>{seat.seatType}</span><em>{seat.status}</em>{canHold && seat.status === 'AVAILABLE' && seat.scheduleSeatId !== null && <button className="primary-button compact" type="button" disabled={disabled} onClick={() => onHold?.(seat.scheduleSeatId as number)}>HOLD</button>}{canManage && seat.scheduleSeatId !== null && <button className="secondary-button compact" type="button" disabled={disabled} onClick={() => seat.status === 'BLOCKED' ? onUnblock?.(seat.scheduleSeatId as number) : onBlock?.(seat.scheduleSeatId as number)}>{seat.status === 'BLOCKED' ? 'UNBLOCK' : 'BLOCK'}</button>}</div>)}</div></section>)}{seatMap.cars.length === 0 && <div className="inline-alert">이 운행편에 생성된 좌석이 없습니다. 운행편 생성 전에 열차의 객차/좌석이 등록되어 있어야 합니다.</div>}</div>;
}

function EmptyRow({ colSpan }: { colSpan: number }) { return <tr><td colSpan={colSpan} className="table-empty">조회 결과 없음</td></tr>; }
function StatusBadge({ status }: { status: TrainScheduleStatus }) { return <span className={`status-badge ${status.toLowerCase()}`}>{status}</span>; }
function formatDateTime(value: string) { return value ? value.replace('T', ' ').slice(0, 16) : '-'; }
function toDateTimeLocal(value: string) { return value ? value.slice(0, 16) : ''; }
function toPositiveNumber(value: string) { const parsed = Number(value); return Number.isFinite(parsed) && parsed > 0 ? parsed : 1; }