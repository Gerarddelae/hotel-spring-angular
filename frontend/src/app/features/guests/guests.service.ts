import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { switchMap, take } from 'rxjs/operators';
import { GuestRequest } from './models/guest-request.interface';
import { GuestResponse } from './models/guest-response.interface';

@Injectable({ providedIn: 'root' })
export class GuestsService {
  private apiUrl = 'http://127.0.0.1:8080/guests';

  private guestsSubject = new BehaviorSubject<GuestResponse[]>([]);
  guests$ = this.guestsSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadGuests();
  }

  loadGuests(params?: any) {
    // params can be used to send pagination/query params later
    this.http.get<GuestResponse[]>(this.apiUrl).subscribe((res) => this.guestsSubject.next(res || []));
  }

  list(params?: any): Observable<GuestResponse[]> {
    return this.guests$;
  }

  get(id: number): Observable<GuestResponse> {
    return this.http.get<GuestResponse>(`${this.apiUrl}/${id}`);
  }

  create(payload: GuestRequest): Observable<GuestResponse> {
    // ensure analytics metrics are initialized to 0 if not provided
    const body: GuestRequest = {
      ...payload,
      previousCancellations: payload.previousCancellations ?? 0,
      totalBookingsClient: payload.totalBookingsClient ?? 0,
    };

    return this.http.post<GuestResponse>(this.apiUrl, body).pipe(
      tap(() => this.loadGuests())
    );
  }

  update(id: number, payload: GuestRequest): Observable<GuestResponse> {
    // preserve existing analytics metrics by merging with current entity
    return this.get(id).pipe(
      take(1),
      switchMap((existing) => {
        const body = {
          ...existing,
          ...payload,
          // if payload explicitly provides metrics, use them; otherwise keep existing
          previousCancellations: payload.previousCancellations ?? existing.previousCancellations ?? 0,
          totalBookingsClient: payload.totalBookingsClient ?? existing.totalBookingsClient ?? 0,
        } as any;
        return this.http.put<GuestResponse>(`${this.apiUrl}/${id}`, body).pipe(
          tap(() => this.loadGuests())
        );
      })
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.loadGuests())
    );
  }
}
