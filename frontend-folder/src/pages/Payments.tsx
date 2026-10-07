import { useEffect, useState, type FormEvent } from 'react';
import { getApiErrorMessage, membershipApi } from '../lib/api';
import type { Membership, Payment } from '../types';

export default function Payments() {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [memberships, setMemberships] = useState<Membership[]>([]);
  const [membershipId, setMembershipId] = useState('');
  const [amount, setAmount] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadData() {
    setLoading(true);
    try {
      const [paymentResponse, membershipResponse] = await Promise.all([
        membershipApi.getPayments(),
        membershipApi.getMemberships(),
      ]);
      setPayments(paymentResponse.data);
      setMemberships(membershipResponse.data);
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
      await membershipApi.createPayment({
        membershipId: Number(membershipId),
        amount: Number(amount),
        notes: notes.trim() || undefined,
      });
      setMembershipId('');
      setAmount('');
      setNotes('');
      await loadData();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold">Thanh toán</h2>
        <p className="mt-1 text-slate-600">Theo dõi và ghi nhận thanh toán cho membership.</p>
      </div>

      {error && <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <form onSubmit={handleSubmit} className="grid gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm md:grid-cols-3">
        <label className="text-sm font-medium">
          Membership
          <select required value={membershipId} onChange={(event) => setMembershipId(event.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 font-normal">
            <option value="">Chọn membership</option>
            {memberships.map((membership) => <option key={membership.id} value={membership.id}>#{membership.id} · Hội viên {membership.memberId} · {membership.planName}</option>)}
          </select>
        </label>
        <label className="text-sm font-medium">
          Số tiền
          <input required min="0.01" step="0.01" type="number" value={amount} onChange={(event) => setAmount(event.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal" />
        </label>
        <label className="text-sm font-medium">
          Ghi chú
          <input value={notes} onChange={(event) => setNotes(event.target.value)} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal" />
        </label>
        <button disabled={saving} className="w-fit rounded-lg bg-indigo-600 px-4 py-2 font-semibold text-white hover:bg-indigo-700 disabled:opacity-60 md:col-span-3">
          {saving ? 'Đang lưu...' : 'Tạo thanh toán'}
        </button>
      </form>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-200 px-5 py-4 font-semibold">Lịch sử thanh toán</div>
        {loading ? <p className="p-5 text-slate-600">Đang tải...</p> : payments.length === 0 ? <p className="p-5 text-slate-600">Chưa có thanh toán nào.</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-600"><tr><th className="px-5 py-3">Mã</th><th className="px-5 py-3">Membership</th><th className="px-5 py-3">Số tiền</th><th className="px-5 py-3">Trạng thái</th><th className="px-5 py-3">Ghi chú</th><th className="px-5 py-3">Ngày tạo</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {payments.map((payment) => (
                  <tr key={payment.id}>
                    <td className="px-5 py-4">#{payment.id}</td>
                    <td className="px-5 py-4">#{payment.membershipId}</td>
                    <td className="px-5 py-4">{payment.amount.toLocaleString('vi-VN')} đ</td>
                    <td className="px-5 py-4">{payment.status}</td>
                    <td className="px-5 py-4">{payment.notes || '—'}</td>
                    <td className="px-5 py-4">{new Date(payment.createdAt).toLocaleString('vi-VN')}</td>
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
