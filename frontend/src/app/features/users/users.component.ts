import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsersService } from './users.service';
import { TableComponent } from '../../shared/components/table/table.component';
import { Observable } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { UserModalFormComponent } from '../../shared/components/user-modal-form/user-modal-form.component';
import { User } from './models/user.interface';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, TableComponent, MatDialogModule],
  templateUrl: './users.component.html'
})
export class UsersComponent {
  users$: Observable<User[]>;
  showDeleteModal = false;
  userToDelete: { id: number } | null = null;

  columns = ['id', 'username', 'email', 'role', 'hotelId'];

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

  refreshUsers() {
    this.users$ = this.usersService.getUsers();
  }

  /** Abrir modal para crear o editar usuario */
  openUserModal(user?: User) {
    const dialogRef = this.dialog.open(UserModalFormComponent, {
      width: '450px',
      data: { user } // usuario opcional para edición
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (user) {
          this.usersService.updateUser(user.id, result).subscribe(() => this.refreshUsers());
        } else {
          this.usersService.createUser(result).subscribe(() => this.refreshUsers());
        }
      }
    });
  }

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
