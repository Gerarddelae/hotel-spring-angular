export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'TRANSFER' | 'CHECK' | 'OTHER';

export const PAYMENT_METHOD_OPTIONS = [
  { value: 'CASH', label: 'Efectivo' },
  { value: 'CREDIT_CARD', label: 'Tarjeta de Crédito' },
  { value: 'DEBIT_CARD', label: 'Tarjeta de Débito' },
  { value: 'TRANSFER', label: 'Transferencia' },
  { value: 'CHECK', label: 'Cheque' },
  { value: 'OTHER', label: 'Otro' }
];
