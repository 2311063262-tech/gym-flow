import { useEffect, useState, type FormEvent } from 'react';
import { getApiErrorMessage, membershipApi } from '../lib/api';
import type { Membership, Plan } from '../types';

export default function Memberships() {
  const [memberships, setMemberships] = useState<Membership[]>([]);
  const [plans, setPlans] = useState<Plan[]>([]);
  const [memberId, setMemberId] = useState('');
  const [planId, setPlanId] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadData() {
    setLoading(true);
    try {
      const [membershipResponse, planResponse] = await Promise.all([
        membershipApi.getMemberships(),
        membershipApi.getPlans(),
      ]);
      setMemberships(membershipResponse.data);
      setPlans(planResponse.data);
      setError('');
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await membershipApi.createMembership({ memberId: Number(memberId), planId: Number(planId) });
      setMemberId('');
      setPlanId('');
      await loadData();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  async function updateStatus(id: number, status: string) {
    setError('');
    try {
      await membershipApi.updateMembership(id, status);
      await loadData();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    }
  }

  return (
    <section className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold">Hội viên</h2>
        <p className="mt-1 text-slate-600">Đăng ký gói tập và theo dõi thời hạn membership.</p>
      </div>

      {error && <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <form onSubmit={handleSubmit} className="grid gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm md:grid-cols-3">
        <label className="text-sm font-medium">
          ID hội viên
          <input required min="1" step="1" type="number" value={memberId} onChange={(event) => setMemberId(event.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal" />
          <span className="mt-1 block text-xs font-normal text-slate-500">ID này do service quản lý hội viên cung cấp.</span>
        </label>
        <label className="text-sm font-medium">
          Gói tập
          <select required value={planId} onChange={(event) => setPlanId(event.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 font-normal">
            <option value="">Chọn gói tập</option>
            {plans.map((plan) => <option key={plan.id} value={plan.id}>{plan.name} · {plan.price.toLocaleString('vi-VN')} đ</option>)}
          </select>
        </label>
        <div className="flex items-end">
          <button disabled={saving || plans.length === 0} className="rounded-lg bg-indigo-600 px-4 py-2 font-semibold text-white hover:bg-indigo-700 disabled:opacity-60">
            {saving ? 'Đang tạo...' : 'Tạo membership'}
          </button>
        </div>
        <p className="text-xs text-slate-500 md:col-span-3">Service tự tạo thanh toán trạng thái PENDING khi đăng ký membership.</p>
      </form>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-200 px-5 py-4 font-semibold">Danh sách membership</div>
        {loading ? <p className="p-5 text-slate-600">Đang tải...</p> : memberships.length === 0 ? <p className="p-5 text-slate-600">Chưa có membership nào.</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-600"><tr><th className="px-5 py-3">Mã</th><th className="px-5 py-3">Hội viên</th><th className="px-5 py-3">Gói</th><th className="px-5 py-3">Bắt đầu</th><th className="px-5 py-3">Kết thúc</th><th className="px-5 py-3">Trạng thái</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {memberships.map((membership) => (
                  <tr key={membership.id}>
                    <td className="px-5 py-4">#{membership.id}</td>
                    <td className="px-5 py-4">#{membership.memberId}</td>
                    <td className="px-5 py-4">{membership.planName}</td>
                    <td className="px-5 py-4">{membership.startDate}</td>
                    <td className="px-5 py-4">{membership.endDate}</td>
                    <td className="px-5 py-4">
                      <select aria-label={`Trạng thái membership ${membership.id}`} value={membership.status} onChange={(event) => void updateStatus(membership.id, event.target.value)} className="rounded-lg border border-slate-300 bg-white px-2 py-1">
                        {['ACTIVE', 'EXPIRED', 'CANCELLED'].map((status) => <option key={status} value={status}>{status}</option>)}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
