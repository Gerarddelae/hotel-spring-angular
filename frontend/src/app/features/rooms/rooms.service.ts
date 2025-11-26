import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Room } from './models/room.interface';
import { OccupiedRoomsCountResponse, RoomDashboardSummary } from '../dashboard/models';

@Injectable({ providedIn: 'root' })
export class RoomService {
  private apiUrl = 'http://127.0.0.1:8080/rooms'; 

  private roomsSubject = new BehaviorSubject<Room[]>([]);
  rooms$ = this.roomsSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadRooms();
  }

  /** 🔹 Cargar todas las habitaciones desde el backend */
  loadRooms(): void {
    this.http.get<Room[]>(this.apiUrl).subscribe({
      next: (rooms) => this.roomsSubject.next(rooms),
      error: (err) => console.error('Error cargando habitaciones:', err),
    });
  }

  /** 🔹 Limpiar el estado de las habitaciones */
  clearRooms(): void {
    this.roomsSubject.next([]);
  }

  /** 🔹 Obtener flujo reactivo de habitaciones */
  getRooms(): Observable<Room[]> {
    return this.rooms$;
  }

  /** 🔹 Obtener una habitación por ID */
  getRoomById(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.apiUrl}/${id}`);
  }

  /** 🔹 Crear una nueva habitación */
  createRoom(room: Room): Observable<Room> {
    return this.http.post<Room>(this.apiUrl, room).pipe(
      tap(() => this.loadRooms())
    );
  }

  /** 🔹 Actualizar una habitación existente */
  updateRoom(id: number, room: Room): Observable<Room> {
    return this.http.put<Room>(`${this.apiUrl}/${id}`, room).pipe(
      tap(() => this.loadRooms())
    );
  }

  /** 🔹 Eliminar una habitación */
  deleteRoom(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.loadRooms())
    );
  }

  /** 🔹 Dashboard: Obtener conteo de habitaciones ocupadas */
  getOccupiedCount(): Observable<OccupiedRoomsCountResponse> {
    return this.http.get<OccupiedRoomsCountResponse>(`${this.apiUrl}/occupied-count`);
  }

  /** 🔹 Dashboard: Obtener resumen de habitaciones para el dashboard */
  getDashboardSummary(): Observable<RoomDashboardSummary[]> {
    return this.http.get<RoomDashboardSummary[]>(`${this.apiUrl}/dashboard-summary`);
  }
}
