import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HotelResponse, HotelUpdateRequest } from '../models';
import { AuthService } from '../../../auth/auth.service';

@Injectable({
  providedIn: 'root',
})
export class HotelService {
  private readonly apiUrl = 'http://localhost:8080/api/hotels';

  constructor(private http: HttpClient, private authService: AuthService) {}

  /**
   * Obtiene la información del hotel por ID
   */
  getHotelById(id: number): Observable<HotelResponse> {
    return this.http.get<HotelResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Actualización completa del hotel (PUT)
   */
  updateHotel(id: number, data: HotelUpdateRequest): Observable<HotelResponse> {
    return this.http.put<HotelResponse>(`${this.apiUrl}/${id}`, data);
  }

  /**
   * Actualización parcial del hotel (PATCH)
   */
  patchHotel(id: number, data: Partial<HotelUpdateRequest>): Observable<HotelResponse> {
    return this.http.patch<HotelResponse>(`${this.apiUrl}/${id}`, data);
  }

  /**
   * Obtiene el hotel del usuario actual
   */
  getCurrentHotel(): Observable<HotelResponse> {
    const hotelId = this.authService.getHotelId();
    if (!hotelId) {
      throw new Error('No se pudo obtener el ID del hotel del usuario actual');
    }
    return this.getHotelById(hotelId);
  }

  /**
   * Actualiza el hotel del usuario actual
   */
  updateCurrentHotel(data: HotelUpdateRequest): Observable<HotelResponse> {
    const hotelId = this.authService.getHotelId();
    if (!hotelId) {
      throw new Error('No se pudo obtener el ID del hotel del usuario actual');
    }
    return this.updateHotel(hotelId, data);
  }
}
