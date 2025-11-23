export interface GuestResponse {
  id: number;
  fullName: string;
  documentType: string;
  documentNumber: string;
  email: string;
  phone: string;
  address: string;
  previousCancellations: number;
  totalBookingsClient: number;
  hotelId: number;
  hotelName: string;
}
