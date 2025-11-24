export interface Booking {
  id: number;
  guestId: number;
  guestName?: string;
  roomId: number;
  roomNumber?: string;
  checkInDate: string;
  checkOutDate: string;
  status: BookingStatus;
  createdBy: string;
  bookingLeadTime: string;
  notes?: string;
  totalAmount?: number;
  accommodationSubtotal?: number;
  addonsSubtotal?: number;
  hotelId: number;
  addons?: BookingAddon[];
}

export interface BookingRequest {
  guestId: number;
  roomId: number;
  checkInDate: string;
  checkOutDate: string;
  status: BookingStatus;
  createdBy: string;
  bookingLeadTime: string;
  notes?: string;
  hotelId?: number;
  addons?: BookingAddonRequest[];
}

export interface BookingResponseDTO extends Booking {
  createdAt?: string;
  updatedAt?: string;
}

export interface BookingAddon {
  id?: number;
  addonId: number;
  addonName?: string;
  price: number;
  quantity: number;
  subtotal?: number;
}

export interface BookingAddonRequest {
  addonId: number;
  quantity: number;
}

export type BookingStatus = 
  | 'PENDING' 
  | 'CONFIRMED' 
  | 'CHECKED_IN' 
  | 'CHECKED_OUT' 
  | 'CANCELLED';

export const BOOKING_STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Pendiente' },
  { value: 'CONFIRMED', label: 'Confirmada' },
  { value: 'CHECKED_IN', label: 'Check-in realizado' },
  { value: 'CHECKED_OUT', label: 'Check-out realizado' },
  { value: 'CANCELLED', label: 'Cancelada' }
];
