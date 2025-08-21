import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsersService } from './users.service';
import { TableComponent } from '../../shared/components/table/table.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, TableComponent],
  templateUrl: './users.component.html'
})
export class UsersComponent {
  users$: Observable<any[]>;

  // Columnas a mostrar en la tabla
  columns = ['id', 'username', 'email', 'role', 'hotelId'];

  // Mapeo para headers personalizados
  headersMap = {
    id: 'ID',
    username: 'Usuario',
    email: 'Correo',
    role: 'Rol',
    hotelId: 'Hotel'
  };

  constructor(private usersService: UsersService) {
    this.users$ = this.usersService.getUsers();
  }
}
