import { BillStatus } from './bill-status.enum';
import { PaymentMethod } from './payment-method.enum';

export interface BillCreateRequest {
  notes?: string;
  status?: BillStatus;
  paymentMethod?: PaymentMethod;
}

export interface BillUpdateRequest {
  notes?: string;
  status?: BillStatus;
  paymentMethod?: PaymentMethod;
}
