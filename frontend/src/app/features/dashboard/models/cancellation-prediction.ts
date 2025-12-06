export interface CancellationPrediction {
  bookingId: number;
  guestName: string;
  guestEmail: string;
  guestPhone: string;
  roomNumber: string;
  checkInDate: string;
  checkOutDate: string;
  totalAmount: number;
  cancellationProbability: number;
  willCancel: boolean;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
}

export interface PredictionResponse {
  predictions: CancellationPrediction[];
  totalBookings: number;
  highRiskCount: number;
  mediumRiskCount: number;
  lowRiskCount: number;
  averageCancellationProbability: number;
}
