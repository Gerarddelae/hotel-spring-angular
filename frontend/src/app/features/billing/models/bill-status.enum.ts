export type BillStatus = 'PAID' | 'UNPAID' | 'CANCELED';

export const BILL_STATUS_OPTIONS = [
  { value: 'PAID', label: 'Pagada' },
  { value: 'UNPAID', label: 'Pendiente' },
  { value: 'CANCELED', label: 'Cancelada' }
];
