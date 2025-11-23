import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AddonRequest } from './models/addon-request.interface';
import { AddonResponse } from './models/addon-response.interface';

@Injectable({ providedIn: 'root' })
export class AddonsService {
  private apiUrl = 'http://127.0.0.1:8080/api/addons';

  private addonsSubject = new BehaviorSubject<AddonResponse[]>([]);
  addons$ = this.addonsSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadAddons();
  }

  loadAddons(params?: any) {
    this.http.get<AddonResponse[]>(this.apiUrl, { params }).subscribe((res) => this.addonsSubject.next(res || []));
  }

  list(params?: any): Observable<AddonResponse[]> {
    return this.addons$;
  }

  get(id: number): Observable<AddonResponse> {
    return this.http.get<AddonResponse>(`${this.apiUrl}/${id}`);
  }

  create(payload: AddonRequest): Observable<AddonResponse> {
    return this.http.post<AddonResponse>(this.apiUrl, payload).pipe(
      tap(() => this.loadAddons())
    );
  }

  update(id: number, payload: AddonRequest): Observable<AddonResponse> {
    return this.http.put<AddonResponse>(`${this.apiUrl}/${id}`, payload).pipe(
      tap(() => this.loadAddons())
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.loadAddons())
    );
  }

  search(name: string): Observable<AddonResponse[]> {
    return this.http.get<AddonResponse[]>(`${this.apiUrl}/search`, { params: { name } }).pipe(
      tap((res) => this.addonsSubject.next(res || []))
    );
  }
}
