import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { BehaviorSubject, Observable } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

import { AuthRequest } from './interfaces/auth-request.interface';
import { RegisterRequest } from './interfaces/register-request.interface';
import { AuthResponse } from './interfaces/auth-response.interface';
import { JwtPayload } from './interfaces/jwt-payload.interface';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth'; // ajusta si tu backend cambia

  private userSubject = new BehaviorSubject<JwtPayload | null>(null);
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromToken();
  }

  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap((response) => {
          localStorage.setItem('token', response.token);
          const decoded = this.decodeToken(response.token);
          this.userSubject.next(decoded); 
        })
      );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    console.log(data);
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token); // opcional: si también se devuelve el token al registrarse
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  decodeToken(token: string): JwtPayload | null {
    try {
      return jwtDecode<JwtPayload>(token);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  private loadUserFromToken(): void {
    const token = localStorage.getItem('token');
    if (token) {
      const decoded = this.decodeToken(token);
      console.log(decoded);
      this.userSubject.next(decoded);
    }
  }

  getCurrentUser(): JwtPayload | null {
    return this.userSubject.value;
  }
}
