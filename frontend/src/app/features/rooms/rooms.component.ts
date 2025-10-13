import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TableComponent } from '../../shared/components/table/table.component';
import { RoomModalFormComponent } from '../../shared/components/room-modal-form/room-modal-form.component';
import { RoomService } from './rooms.service';
import { Room } from './models/room.interface';

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, TableComponent, MatDialogModule],
  templateUrl: './rooms.component.html'
})
export class RoomsComponent {
  rooms$: Observable<Room[]>;
  showDeleteModal = false;
  roomToDelete: { id: number } | null = null;

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

  headersMap = {
    id: 'ID',
    number: 'N° Habitación',
    type: 'Tipo',
    floor: 'Piso',
    capacity: 'Capacidad',
    pricePerNight: 'Precio/Noche',
    status: 'Estado',
    hotelId: 'Hotel'
  };

  constructor(
    private dialog: MatDialog,
    private roomService: RoomService
  ) {
    this.rooms$ = this.roomService.rooms$;
  }

  refreshRooms() {
    this.rooms$ = this.roomService.getRooms();
  }

  /** Abrir modal para crear o editar habitación */
  openRoomModal(room?: Room) {
    const dialogRef = this.dialog.open(RoomModalFormComponent, {
      width: '500px',
      data: { room } // habitación opcional para edición
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (room) {
          this.roomService.updateRoom(room.id!, result).subscribe(() => this.refreshRooms());
        } else {
          this.roomService.createRoom(result).subscribe(() => this.refreshRooms());
        }
      }
    });
  }

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
