import { BillStatus } from './bill-status.enum';
import { PaymentMethod } from './payment-method.enum';
import { BillAddon } from './bill-addon.model';

export interface Bill {
  id: number;
  bookingId: number;
  guestId?: number;
  guestName?: string;
  roomId?: number;
  roomNumber?: string;
  roomPricePerNight?: number;
  checkInDate?: string;
  checkOutDate?: string;
  status: BillStatus;
  paymentMethod: PaymentMethod;
  notes?: string;
  totalAmount: number;
  accommodationSubtotal?: number;
  addonsSubtotal?: number;
  nights?: number;
  createdAt: string;
  updatedAt?: string;
  addons?: BillAddon[];
  // Campos adicionales que puede devolver el backend
  booking?: {
    id: number;
    guestId: number;
    guestName?: string;
    roomId: number;
    roomNumber?: string;
    checkInDate: string;
    checkOutDate: string;
  };
}
