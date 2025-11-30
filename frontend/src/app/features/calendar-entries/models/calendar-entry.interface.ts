/**
 * Interface representing a calendar entry from the backend
 * Used to display check-ins and check-outs in the calendar view
 */
export interface CalendarEntry {
  bookingId: number;
  guestName: string;
  roomNumber: string;
  checkInDate: string; // Format: YYYY-MM-DD
  checkOutDate: string; // Format: YYYY-MM-DD
}
