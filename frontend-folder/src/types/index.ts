export interface Plan {
  id: number;
  name: string;
  price: number;
  duration: number;
  features: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CreatePlanRequest {
  name: string;
  price: number;
  duration: number;
  features: string[];
}

export interface Membership {
  id: number;
  memberId: number;
  planId: number;
  planName: string;
  startDate: string;
  endDate: string;
  status: string;
}

export interface CreateMembershipRequest {
  memberId: number;
  planId: number;
}

export interface Payment {
  id: number;
  membershipId: number;
  amount: number;
  status: string;
  notes?: string;
  createdAt: string;
}

export interface CreatePaymentRequest {
  membershipId: number;
  amount: number;
  notes?: string;
}
