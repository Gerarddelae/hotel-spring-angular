import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { PredictionResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class PredictionService {
  private readonly API_URL = 'http://localhost:8080/api/predictions';

  constructor(private http: HttpClient) {}

  /**
   * Get cancellation predictions for pending bookings
   */
  getPendingBookingPredictions(): Observable<PredictionResponse> {
    return this.http.get<PredictionResponse>(`${this.API_URL}/pending`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: any): Observable<never> {
    console.error('Error in PredictionService:', error);
    return throwError(() => error);
  }
}
