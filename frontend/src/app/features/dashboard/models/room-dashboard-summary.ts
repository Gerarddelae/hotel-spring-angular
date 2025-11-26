export interface RoomDashboardSummary {
  roomId: number;
  number: string;
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE';
  roomTypeName: string;
  currentBookingId: number | null;
  capacity?: number;
}
