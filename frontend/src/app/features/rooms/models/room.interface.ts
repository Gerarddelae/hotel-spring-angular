export interface Room {
  id?: number;           // opcional para creación
  number: string;
  type: 'SINGLE' | 'DOUBLE' | 'SUITE'; // según RoomType
  floor: number;
  capacity: number;
  pricePerNight: number;
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'; // según RoomStatus
  hotelId: number;
  hotelName?: string;    // opcional, solo para mostrar
}
