import axios from 'axios';
import type { CreateMembershipRequest, CreatePaymentRequest, CreatePlanRequest, Membership, Payment, Plan } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
});

export const membershipApi = {
  getPlans: () => api.get<Plan[]>('/api/plans'),
  createPlan: (data: CreatePlanRequest) => api.post<Plan>('/api/plans', data),
  updatePlan: (id: number, data: CreatePlanRequest) => api.put<Plan>(`/api/plans/${id}`, data),
  deletePlan: (id: number) => api.delete(`/api/plans/${id}`),
  getMemberships: () => api.get<Membership[]>('/api/memberships'),
  createMembership: (data: CreateMembershipRequest) => api.post<Membership>('/api/memberships', data),
  updateMembership: (id: number, status: string) =>
    api.put<Membership>(`/api/memberships/${id}`, status, {
      headers: { 'Content-Type': 'text/plain' },
    }),
  getPayments: () => api.get<Payment[]>('/api/payments'),
  createPayment: (data: CreatePaymentRequest) => api.post<Payment>('/api/payments', data),
};

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    return error.response?.data?.message || error.message;
  }
  return error instanceof Error ? error.message : 'Đã xảy ra lỗi không xác định.';
}
