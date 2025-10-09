import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RoomService {
  private apiUrl = 'http://127.0.0.1:8080/rooms/hotel/1'; // Ajusta según tu backend

  // TODO: IMPORTANTE REFORMULAR ESTE SERVICIO LUEGO DE REFACTORIZAR BACKEND
  
  // Estado reactivo
  private roomsSubject = new BehaviorSubject<any[]>([]);
  rooms$ = this.roomsSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadRooms(); // Cargar habitaciones al iniciar el servicio
  }

  /** 🔹 Cargar todas las habitaciones desde el backend */
  private loadRooms(): void {
    this.http.get<any[]>(this.apiUrl)
      .subscribe({
        next: rooms => this.roomsSubject.next(rooms),
        error: err => console.error('Error cargando habitaciones:', err)
      });
  }

  /** 🔹 Obtener flujo de habitaciones */
  getRooms(): Observable<any[]> {
    return this.rooms$;
  }

  /** 🔹 Obtener habitación por ID */
  getRoomById(id: number): Observable<any> {
    return this.http.get<any>("http://127.0.0.1:8080/rooms/" + id);
  }

  /** 🔹 Crear una nueva habitación */
  createRoom(room: any): Observable<any> {
    return this.http.post<any>("http://127.0.0.1:8080/rooms", room).pipe(
      tap(() => this.loadRooms()) // Recargar habitaciones después de crear
    );
  }

  /** 🔹 Actualizar una habitación existente */
  updateRoom(id: number, room: any): Observable<any> {
    return this.http.put<any>(`http://127.0.0.1:8080/rooms/${id}`, room).pipe(
      tap(() => this.loadRooms()) // Recargar habitaciones después de actualizar
    );
  }

  /** 🔹 Eliminar una habitación */
  deleteRoom(id: number): Observable<void> {
    return this.http.delete<void>(`http://127.0.0.1:8080/rooms/${id}`).pipe(
      tap(() => this.loadRooms()) // Recargar habitaciones después de eliminar
    );
  }
}
