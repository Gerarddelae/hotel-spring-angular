import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User } from './models/user.interface';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private apiUrl = 'http://127.0.0.1:8080/users';
  
  // Agregar BehaviorSubject para manejar el estado
  private usersSubject = new BehaviorSubject<User[]>([]);
  users$ = this.usersSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUsers(); // Cargar usuarios al iniciar el servicio
  }

  // Método para cargar usuarios
  private loadUsers() {
    this.http.get<User[]>(this.apiUrl)
      .subscribe(users => this.usersSubject.next(users));
  }

  getUsers(): Observable<User[]> {
    return this.users$;
  }

  // Obtener un usuario por ID
  getUserById(id: number): Observable<User> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  // Crear un nuevo usuario
  createUser(user: any): Observable<User> {
    return this.http.post<any>(this.apiUrl+"/employees", user).pipe(
      tap(() => this.loadUsers()) // Recargar usuarios después de crear
    );
  }

  // Actualizar un usuario existente
  updateUser(id: number, user: User): Observable<User> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, user).pipe(
      tap(() => this.loadUsers()) // Recargar usuarios después de actualizar
    );
  }

  // Eliminar un usuario por ID
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.loadUsers()) // Recargar usuarios después de eliminar
    );
  }
}
