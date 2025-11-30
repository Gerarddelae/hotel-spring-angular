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
        // Guardar token y datos del usuario
        localStorage.setItem('token', response.token);
        localStorage.setItem('authorities', JSON.stringify(response.authorities));
        localStorage.setItem('username', response.username);
        localStorage.setItem('hotelName', response.hotelName);
        
        // Actualizar el BehaviorSubject
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
        // Guardar token y datos del usuario
        localStorage.setItem('token', response.token);
        localStorage.setItem('authorities', JSON.stringify(response.authorities));
        
        this.userSubject.next({
          username: response.username,
          hotelName: response.hotelName,
          authorities: response.authorities,
        });
      })
    );
  }

  logout(): void {
    // Limpiar todo el localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('authorities');
    localStorage.removeItem('hotelName');
    localStorage.removeItem('hotelId');
    localStorage.removeItem('username');

    this.userSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  private isTokenValid(token: string): boolean {
    try {
      const tokenData = JSON.parse(atob(token.split('.')[1]));
      const expirationDate = new Date(tokenData.exp * 1000);
      return expirationDate > new Date();
    } catch {
      return false;
    }
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('token');
    const authorities = localStorage.getItem('authorities');
    const username = localStorage.getItem('username');
    const hotelName = localStorage.getItem('hotelName');

    if (!token || !this.isTokenValid(token)) {
      // No navegar desde el constructor/initializer: limpiar storage y estado
      localStorage.removeItem('token');
      localStorage.removeItem('authorities');
      localStorage.removeItem('hotelName');
      localStorage.removeItem('hotelId');
      localStorage.removeItem('username');
      this.userSubject.next(null);
      return;
    }

    try {
        const user: CurrentUser = {
            username: username || '',
            hotelName: hotelName || '',
            authorities: authorities ? JSON.parse(authorities) : [],
        };
        console.log('Usuario cargado del storage:', user);
        this.userSubject.next(user);
    } catch (e) {
      console.error('Error al cargar usuario desde storage:', e);
      // Evitar navegación desde el constructor: limpiar estado sin redirigir
      localStorage.removeItem('token');
      localStorage.removeItem('authorities');
      localStorage.removeItem('hotelName');
      localStorage.removeItem('hotelId');
      localStorage.removeItem('username');
      this.userSubject.next(null);
    }
  }

  getCurrentUser(): CurrentUser | null {
    return this.userSubject.value;
  }
}
