import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Room } from './models/room.interface';

@Injectable({ providedIn: 'root' })
export class RoomService {
  private apiUrl = 'http://127.0.0.1:8080/rooms'; 

  private roomsSubject = new BehaviorSubject<Room[]>([]);
  rooms$ = this.roomsSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadRooms();
  }

  /** 🔹 Cargar todas las habitaciones desde el backend */
  private loadRooms(): void {
    this.http.get<Room[]>(this.apiUrl).subscribe({
      next: (rooms) => this.roomsSubject.next(rooms),
      error: (err) => console.error('Error cargando habitaciones:', err),
    });
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
}
