import { BookingStatus } from './booking.interface';

export interface BookingFilters {
  guestId?: number;
  roomId?: number;
  status?: BookingStatus;
  checkInFrom?: string;
  checkInTo?: string;
  checkOutFrom?: string;
  checkOutTo?: string;
  searchQuery?: string;
  hotelId?: number;
}

export interface DateRangeFilter {
  startDate?: string;
  endDate?: string;
}

export interface AvailabilityCheckRequest {
  roomId: number;
  checkInDate: string;
  checkOutDate: string;
  excludeBookingId?: number; // Para edición
}

export interface AvailabilityCheckResponse {
  available: boolean;
  conflictingBookings?: number[];
  message?: string;
}
