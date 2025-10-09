import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TableComponent } from '../../shared/components/table/table.component';
import { ModalFormComponent } from '../../shared/components/modal-form/modal-form.component';
import { RoomService } from './rooms.service';

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, TableComponent, MatDialogModule],
  templateUrl: './rooms.component.html'
})
export class RoomsComponent {
  rooms$: Observable<any[]>;
  showDeleteModal = false;
  roomToDelete: any = null;

  /** Columnas visibles en la tabla */
  columns = [
    'id',
    'number',
    'type',
    'floor',
    'capacity',
    'pricePerNight',
    'status',
    'hotelId'
  ];

  /** Mapeo de headers personalizados */
  headersMap = {
    id: 'ID',
    number: 'N° Habitación',
    type: 'Tipo',
    floor: 'Piso',
    capacity: 'Capacidad',
    pricePerNight: 'Precio por noche',
    status: 'Estado',
    hotelId: 'Hotel'
  };

  constructor(
    private dialog: MatDialog,
    private roomService: RoomService
  ) {
    this.rooms$ = this.roomService.rooms$;
  }

  /** Recargar habitaciones */
  refreshRooms() {
    this.rooms$ = this.roomService.getRooms();
  }

  /** Crear nueva habitación */
  onCreate() {
    const dialogRef = this.dialog.open(ModalFormComponent, {
      width: '500px',
      data: {
        mode: 'create',
        entity: 'room',
        formFields: {
          number: '',
          type: '',
          floor: 0,
          capacity: 1,
          pricePerNight: 0,
          status: '',
          hotelId: null
        }
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.roomService.createRoom(result).subscribe(() => this.refreshRooms());
      }
    });
  }

  /** Editar habitación */
  onEdit(room: any) {
    const dialogRef = this.dialog.open(ModalFormComponent, {
      width: '500px',
      data: {
        mode: 'edit',
        entity: 'room',
        formFields: { ...room }
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.roomService.updateRoom(room.id, result)
          .subscribe(() => this.refreshRooms());
      }
    });
  }

  /** Eliminar habitación */
  onDelete(id: number) {
    this.roomToDelete = { id };
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (this.roomToDelete) {
      this.roomService.deleteRoom(this.roomToDelete.id)
        .subscribe(() => {
          this.refreshRooms();
          this.cancelDelete();
        });
    }
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.roomToDelete = null;
  }
}
