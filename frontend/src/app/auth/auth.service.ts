import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { BehaviorSubject, Observable } from 'rxjs';

import { AuthRequest } from './interfaces/auth-request.interface';
import { RegisterRequest } from './interfaces/register-request.interface';

import { AuthResponse } from './interfaces/auth-response.interface';
import { CurrentUser } from './interfaces/current-user.interface';
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  private userSubject = new BehaviorSubject<CurrentUser | null>(null);
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromStorage();
  }

  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token);
        this.userSubject.next({
          username: response.username,
          hotelName: response.hotelName,
          authorities: response.authorities,
        });
      })
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token);
        this.userSubject.next({
          username: response.username,
          hotelName: response.hotelName,
          authorities: response.authorities,
        });
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.userSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('token');
    if (!token) return;
    this.userSubject.next(null);
  }

  getCurrentUser(): CurrentUser | null {
    return this.userSubject.value;
  }
}
