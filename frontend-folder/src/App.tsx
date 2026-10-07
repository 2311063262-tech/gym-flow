import { BrowserRouter, NavLink, Navigate, Route, Routes } from 'react-router-dom';
import Memberships from './pages/Members';
import Payments from './pages/Payments';
import Plans from './pages/Plans';

const navigation = [
  { to: '/plans', label: 'Gói tập' },
  { to: '/memberships', label: 'Hội viên' },
  { to: '/payments', label: 'Thanh toán' },
];

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-50 text-slate-900">
        <header className="border-b border-slate-200 bg-white">
          <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-5 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.2em] text-indigo-600">GymFlow</p>
              <h1 className="mt-1 text-2xl font-bold">Quản lý membership</h1>
            </div>
            <nav className="flex gap-2" aria-label="Membership navigation">
              {navigation.map(({ to, label }) => (
                <NavLink
                  key={to}
                  to={to}
                  className={({ isActive }) =>
                    `rounded-lg px-4 py-2 text-sm font-semibold transition ${
                      isActive ? 'bg-indigo-600 text-white' : 'text-slate-600 hover:bg-slate-100'
                    }`
                  }
                >
                  {label}
                </NavLink>
              ))}
            </nav>
          </div>
        </header>
        <main className="mx-auto max-w-6xl px-4 py-8">
          <Routes>
            <Route path="/" element={<Navigate to="/plans" replace />} />
            <Route path="/plans" element={<Plans />} />
            <Route path="/memberships" element={<Memberships />} />
            <Route path="/payments" element={<Payments />} />
            <Route path="*" element={<Navigate to="/plans" replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
