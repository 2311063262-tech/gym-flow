import { useEffect, useState, type FormEvent } from 'react';
import { getApiErrorMessage, membershipApi } from '../lib/api';
import type { CreatePlanRequest, Plan } from '../types';

const emptyForm = { name: '', price: '', duration: '', features: '' };

export default function Plans() {
  const [plans, setPlans] = useState<Plan[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadPlans() {
    setLoading(true);
    try {
      const response = await membershipApi.getPlans();
      setPlans(response.data);
      setError('');
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadPlans();
  }, []);

  function startEdit(plan: Plan) {
    setEditingId(plan.id);
    setForm({
      name: plan.name,
      price: String(plan.price),
      duration: String(plan.duration),
      features: (plan.features || []).join(', '),
    });
    setError('');
  }

  function resetForm() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const payload: CreatePlanRequest = {
      name: form.name.trim(),
      price: Number(form.price),
      duration: Number(form.duration),
      features: form.features.split(',').map((feature) => feature.trim()).filter(Boolean),
    };
    setSaving(true);
    setError('');
    try {
      if (editingId === null) {
        await membershipApi.createPlan(payload);
      } else {
        await membershipApi.updatePlan(editingId, payload);
      }
      resetForm();
      await loadPlans();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(plan: Plan) {
    if (!window.confirm(`Xóa gói "${plan.name}"?`)) return;
    setError('');
    try {
      await membershipApi.deletePlan(plan.id);
      await loadPlans();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    }
  }

  return (
    <section className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold">Gói tập</h2>
        <p className="mt-1 text-slate-600">Tạo và quản lý các gói membership.</p>
      </div>

      {error && <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <form onSubmit={handleSubmit} className="grid gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm md:grid-cols-2">
        <label className="text-sm font-medium">
          Tên gói
          <input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal" />
        </label>
        <label className="text-sm font-medium">
          Giá
          <input required min="0.01" step="0.01" type="number" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal" />
        </label>
        <label className="text-sm font-medium">
          Thời hạn (ngày)
          <input required min="1" step="1" type="number" value={form.duration} onChange={(event) => setForm({ ...form, duration: event.target.value })} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal" />
        </label>
        <label className="text-sm font-medium">
          Quyền lợi (phân cách bằng dấu phẩy)
          <input value={form.features} onChange={(event) => setForm({ ...form, features: event.target.value })} className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal" />
        </label>
        <div className="flex gap-2 md:col-span-2">
          <button disabled={saving} className="rounded-lg bg-indigo-600 px-4 py-2 font-semibold text-white hover:bg-indigo-700 disabled:opacity-60">
            {saving ? 'Đang lưu...' : editingId === null ? 'Tạo gói' : 'Lưu thay đổi'}
          </button>
          {editingId !== null && <button type="button" onClick={resetForm} className="rounded-lg border border-slate-300 px-4 py-2 font-semibold">Hủy sửa</button>}
        </div>
      </form>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-200 px-5 py-4 font-semibold">Danh sách gói</div>
        {loading ? <p className="p-5 text-slate-600">Đang tải...</p> : plans.length === 0 ? <p className="p-5 text-slate-600">Chưa có gói tập nào.</p> : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-600"><tr><th className="px-5 py-3">Tên gói</th><th className="px-5 py-3">Giá</th><th className="px-5 py-3">Thời hạn</th><th className="px-5 py-3">Quyền lợi</th><th className="px-5 py-3">Thao tác</th></tr></thead>
              <tbody className="divide-y divide-slate-100">
                {plans.map((plan) => (
                  <tr key={plan.id}>
                    <td className="px-5 py-4 font-medium">{plan.name}</td>
                    <td className="px-5 py-4">{plan.price.toLocaleString('vi-VN')} đ</td>
                    <td className="px-5 py-4">{plan.duration} ngày</td>
                    <td className="px-5 py-4">{plan.features?.join(', ') || '—'}</td>
                    <td className="whitespace-nowrap px-5 py-4">
                      <button onClick={() => startEdit(plan)} className="mr-3 font-semibold text-indigo-600 hover:text-indigo-800">Sửa</button>
                      <button onClick={() => void handleDelete(plan)} className="font-semibold text-red-600 hover:text-red-800">Xóa</button>
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
