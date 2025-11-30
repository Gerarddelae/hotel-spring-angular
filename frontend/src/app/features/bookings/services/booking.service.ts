import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, throwError, BehaviorSubject, of, tap } from 'rxjs';
import { 
  Booking, 
  BookingRequest, 
  BookingAddon, 
  BookingAddonRequest,
  BookingResponseDTO 
} from '../models/booking.interface';
import { 
  BookingFilters, 
  AvailabilityCheckRequest, 
  AvailabilityCheckResponse 
} from '../models/booking-filters.interface';
import { BookingStatusCountResponse, ActiveGuestsCountResponse } from '../../dashboard/models';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private readonly API_URL = 'http://localhost:8080/bookings';
  private readonly ROOMS_API_URL = 'http://localhost:8080/rooms';
  private readonly ADDONS_API_URL = 'http://localhost:8080/api/addons';

  constructor(private http: HttpClient) {}

  // Simple in-memory cache to avoid reloading the list on every navigation
  private bookingsSubject = new BehaviorSubject<Booking[] | null>(null);

  /**
   * Limpia el cache de reservas (llamar al hacer logout)
   */
  clearBookings(): void {
    this.bookingsSubject.next(null);
  }

  /**
   * Dashboard: Obtiene el conteo de reservas por estado
   */
  getBookingStatusCount(): Observable<BookingStatusCountResponse> {
    return this.http.get<BookingStatusCountResponse>(`${this.API_URL}/count-by-status`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Dashboard: Obtiene el conteo de huéspedes activos
   */
  getActiveGuestsCount(): Observable<ActiveGuestsCountResponse> {
    return this.http.get<ActiveGuestsCountResponse>(`${this.API_URL}/active-guests-count`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Devuelve las reservas desde cache o carga desde el backend si no existe.
   * @param forceRefresh fuerza recarga desde servidor
   */
  getCachedAll(forceRefresh = false): Observable<Booking[]> {
    const cached = this.bookingsSubject.value;
    if (!forceRefresh && cached && Array.isArray(cached) && cached.length > 0) {
      return of(cached);
    }

    return this.http.get<Booking[]>(this.API_URL).pipe(
      tap(bookings => this.bookingsSubject.next(bookings)),
      catchError(this.handleError)
    );
  }

  /**
   * Fuerza la recarga de reservas desde el backend y actualiza el cache.
   */
  refreshAll(): Observable<Booking[]> {
    return this.http.get<Booking[]>(this.API_URL).pipe(
      tap(bookings => this.bookingsSubject.next(bookings)),
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene todas las reservas con filtros opcionales
   */
  getAll(filters?: BookingFilters): Observable<Booking[]> {
    let params = new HttpParams();
    
    if (filters) {
      if (filters.guestId) params = params.set('guestId', filters.guestId.toString());
      if (filters.roomId) params = params.set('roomId', filters.roomId.toString());
      if (filters.status) params = params.set('status', filters.status);
      if (filters.checkInFrom) params = params.set('checkInFrom', filters.checkInFrom);
      if (filters.checkInTo) params = params.set('checkInTo', filters.checkInTo);
      if (filters.checkOutFrom) params = params.set('checkOutFrom', filters.checkOutFrom);
      if (filters.checkOutTo) params = params.set('checkOutTo', filters.checkOutTo);
      if (filters.searchQuery) params = params.set('search', filters.searchQuery);
    }

    return this.http.get<Booking[]>(this.API_URL, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene una reserva por ID
   */
  getById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.API_URL}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Crea una nueva reserva
   */
  create(booking: BookingRequest): Observable<Booking> {
    return this.http.post<Booking>(this.API_URL, booking).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Actualiza una reserva existente (PUT /bookings/{id}).
   * En esta API el body representa el estado final deseado y puede incluir `addons`.
   * Devuelve un BookingResponseDTO enriquecido (puede incluir version/updatedAt/ETag).
   */
  update(id: number, booking: BookingRequest): Observable<BookingResponseDTO> {
    return this.http.put<BookingResponseDTO>(`${this.API_URL}/${id}`, booking).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Elimina una reserva
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Cancela una reserva (cambia estado a CANCELLED)
   */
  cancel(id: number): Observable<Booking> {
    return this.http.patch<Booking>(`${this.API_URL}/${id}/cancel`, {}).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene reservas en un rango de fechas
   */
  getBookingsBetween(startDate: string, endDate: string): Observable<Booking[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<Booking[]>(`${this.API_URL}/range`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Verifica disponibilidad de una habitación en un rango de fechas
   */
  checkRoomAvailability(request: AvailabilityCheckRequest): Observable<boolean> {
    let params = new HttpParams()
      .set('checkIn', request.checkInDate)
      .set('checkOut', request.checkOutDate);

    // If editing an existing booking, backend can exclude that booking from the check
    if (request.excludeBookingId) {
      params = params.set('excludeBookingId', request.excludeBookingId.toString());
    }

    return this.http.get<AvailabilityCheckResponse>(
      `${this.API_URL}/room/${request.roomId}/availability`, 
      { params }
    ).pipe(
      map(response => response.available),
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene habitaciones disponibles en un rango de fechas
   */
  getAvailableRooms(checkIn: string, checkOut: string): Observable<any[]> {
    const params = new HttpParams()
      .set('checkIn', checkIn)
      .set('checkOut', checkOut);

    return this.http.get<any[]>(`${this.ROOMS_API_URL}/available`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene reservas por huésped
   */
  getByGuest(guestId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.API_URL}/guest/${guestId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene reservas por habitación
   */
  getByRoom(roomId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.API_URL}/room/${roomId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene los addons de una reserva
   */
  getAddons(bookingId: number): Observable<BookingAddon[]> {
    return this.http.get<BookingAddon[]>(`${this.API_URL}/${bookingId}/addons`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Reemplaza completamente la lista de addons de una reserva (NUEVO - Idempotente)
   * @param bookingId ID de la reserva
   * @param addons Lista completa de addons a asignar
   * @returns BookingResponseDTO con addons actualizados
   */
  replaceBookingAddons(bookingId: number, addons: BookingAddonRequest[]): Observable<BookingResponseDTO> {
    return this.http.put<BookingResponseDTO>(`${this.API_URL}/${bookingId}/addons`, addons).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Añade addons a una reserva
   * @deprecated Usar replaceBookingAddons para gestión idempotente
   */
  addAddons(bookingId: number, addons: BookingAddonRequest[]): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${bookingId}/addons`, addons).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Actualiza la cantidad de un addon en una reserva
   * @deprecated Usar replaceBookingAddons para gestión idempotente
   */
  updateAddonQuantity(bookingId: number, addonId: number, quantity: number): Observable<void> {
    return this.http.patch<void>(
      `${this.API_URL}/${bookingId}/addons/${addonId}`, 
      { quantity }
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Elimina un addon de una reserva
   * @deprecated Usar replaceBookingAddons para gestión idempotente
   */
  removeAddon(bookingId: number, addonId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${bookingId}/addons/${addonId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene addons activos disponibles
   */
  getActiveAddons(): Observable<any[]> {
    return this.http.get<any[]>(`${this.ADDONS_API_URL}/active`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Calcula el total de una reserva incluyendo addons
   */
  // NOTE: total calculation must be performed by backend. Frontend should use booking.totalAmount.

  /**
   * Valida que las fechas sean correctas
   */
  validateDates(checkIn: string, checkOut: string): boolean {
    const checkInDate = new Date(checkIn);
    const checkOutDate = new Date(checkOut);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return checkOutDate > checkInDate && checkInDate >= today;
  }

  /**
   * Manejo centralizado de errores
   */
  private handleError(error: any): Observable<never> {
    console.error('Error en BookingService:', error);
    
    let errorMessage = 'Ha ocurrido un error';
    
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'No se puede conectar con el servidor';
    } else if (error.status === 404) {
      errorMessage = 'Reserva no encontrada';
    } else if (error.status === 400) {
      errorMessage = 'Datos inválidos';
    } else if (error.status === 422) {
      errorMessage = 'Error de lógica de negocio';
    }

    // Preserve backend error body/details so caller can show field errors when available
    const backendErrorBody = error.error ?? null;

    return throwError(() => ({ 
      status: error.status, 
      message: errorMessage,
      details: backendErrorBody,
      timestamp: new Date().toISOString()
    }));
  }
}
