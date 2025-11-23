export interface GuestRequest {
  fullName: string;
  documentType: string;
  documentNumber: string;
  email: string;
  phone: string;
  address: string;
  // metrics are optional in requests — managed server-side / by service defaults
  previousCancellations?: number;
  totalBookingsClient?: number;
}
