import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CalendarEntry } from './models/calendar-entry.interface';

@Injectable({
  providedIn: 'root'
})
export class CalendarEntriesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/calendar';

  /**
   * Fetches calendar entries (check-ins and check-outs) for a date range
   * @param start Start date in YYYY-MM-DD format
   * @param end End date in YYYY-MM-DD format
   * @returns Observable of CalendarEntry array
   */
  getEntries(start: string, end: string): Observable<CalendarEntry[]> {
    const params = new HttpParams()
      .set('start', start)
      .set('end', end);

    return this.http.get<CalendarEntry[]>(`${this.baseUrl}/entries`, { params });
  }
}
