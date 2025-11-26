import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, throwError, of, map } from 'rxjs';
import { Bill, BillCreateRequest, BillUpdateRequest, BillStatus, PaymentMethod } from '../models';
import { RevenueResponse } from '../../dashboard/models';

@Injectable({
  providedIn: 'root'
})
export class BillService {
  private readonly API_URL = 'http://localhost:8080/api/bills';

  private billsSubject = new BehaviorSubject<Bill[]>([]);
  bills$ = this.billsSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Dashboard: Obtiene los ingresos del mes actual
   */
  getMonthlyRevenue(): Observable<RevenueResponse> {
    return this.http.get<RevenueResponse>(`${this.API_URL}/revenue/month`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene todas las facturas
   */
  getAll(): Observable<Bill[]> {
    return this.http.get<Bill[]>(this.API_URL).pipe(
      tap(bills => this.billsSubject.next(bills)),
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene una factura por ID
   */
  getBill(id: number): Observable<Bill> {
    return this.http.get<Bill>(`${this.API_URL}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Crea una nueva factura para un booking
   */
  createBill(bookingId: number, request?: BillCreateRequest): Observable<Bill> {
    return this.http.post<Bill>(`${this.API_URL}/${bookingId}`, request || {}).pipe(
      tap(() => this.refreshBills()),
      catchError(this.handleError)
    );
  }

  /**
   * Actualiza el estado de una factura
   */
  updateStatus(id: number, status: BillStatus): Observable<Bill> {
    return this.http.patch<Bill>(`${this.API_URL}/${id}/status`, JSON.stringify(status), {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      tap(() => this.refreshBills()),
      catchError(this.handleError)
    );
  }

  /**
   * Actualiza el método de pago de una factura
   */
  updatePaymentMethod(id: number, paymentMethod: PaymentMethod): Observable<Bill> {
    return this.http.patch<Bill>(`${this.API_URL}/${id}/payment-method`, JSON.stringify(paymentMethod), {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      tap(() => this.refreshBills()),
      catchError(this.handleError)
    );
  }

  /**
   * Actualiza una factura
   */
  update(id: number, request: BillUpdateRequest): Observable<Bill> {
    return this.http.put<Bill>(`${this.API_URL}/${id}`, request).pipe(
      tap(() => this.refreshBills()),
      catchError(this.handleError)
    );
  }

  /**
   * Elimina una factura
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`).pipe(
      tap(() => this.refreshBills()),
      catchError(this.handleError)
    );
  }

  /**
   * Verifica si existe una factura para un booking
   * Primero intenta el endpoint específico, si falla busca en la lista completa
   */
  getBillByBookingId(bookingId: number): Observable<Bill | null> {
    return this.http.get<Bill>(`${this.API_URL}/booking/${bookingId}`).pipe(
      catchError(() => {
        // Si el endpoint específico no existe, buscar en la lista de facturas
        return this.http.get<Bill[]>(this.API_URL).pipe(
          map(bills => bills.find(bill => bill.bookingId === bookingId) || null),
          catchError(() => of(null))
        );
      })
    );
  }

  /**
   * Refresca la lista de facturas
   */
  private refreshBills(): void {
    this.http.get<Bill[]>(this.API_URL).subscribe({
      next: bills => this.billsSubject.next(bills),
      error: err => console.error('Error refreshing bills:', err)
    });
  }

  /**
   * Manejo de errores
   */
  private handleError(error: any): Observable<never> {
    let message = 'Ha ocurrido un error';
    
    if (error.error?.message) {
      message = error.error.message;
    } else if (error.status === 404) {
      message = 'Factura no encontrada';
    } else if (error.status === 409) {
      message = 'Ya existe una factura para esta reserva';
    } else if (error.status === 400) {
      message = 'Datos inválidos';
    }

    return throwError(() => ({ status: error.status, message }));
  }
}
