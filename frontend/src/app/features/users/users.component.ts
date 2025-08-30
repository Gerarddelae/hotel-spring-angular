import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsersService } from './users.service';
import { TableComponent } from '../../shared/components/table/table.component';
import { Observable } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ModalFormComponent } from '../../shared/components/modal-form/modal-form.component';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, TableComponent, MatDialogModule],
  templateUrl: './users.component.html'
})
export class UsersComponent {
  users$: Observable<any[]>;
  showDeleteModal = false;
  userToDelete: any = null;

  // Columnas visibles en la tabla
  columns = ['id', 'username', 'email', 'role', 'hotelId'];

  // Mapeo para headers personalizados
  headersMap = {
    id: 'ID',
    username: 'Usuario',
    email: 'Correo',
    role: 'Rol',
    hotelId: 'Hotel'
  };

  constructor(
    private dialog: MatDialog,
    private usersService: UsersService
  ) {
    this.users$ = this.usersService.users$;
  }

  /** Recargar usuarios */
  refreshUsers() {
    this.users$ = this.usersService.getUsers();
  }

  /** Crear nuevo usuario */
  onCreate() {
    const dialogRef = this.dialog.open(ModalFormComponent, {
      width: '450px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.usersService.createUser(result).subscribe(() => this.refreshUsers());
      }
    });
  }

  /** Editar usuario */
  onEdit(user: any) {
    const dialogRef = this.dialog.open(ModalFormComponent, {
      width: '450px',
      data: { mode: 'edit', user }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.usersService.updateUser(user.id, result).subscribe(() => this.refreshUsers());
      }
    });
  }

  /** Eliminar usuario */
  onDelete(id: number) {
    this.userToDelete = { id };
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.userToDelete) {
      this.usersService.deleteUser(this.userToDelete.id)
        .subscribe(() => {
          this.refreshUsers();
          this.cancelDelete();
        });
    }
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.userToDelete = null;
  }
}
