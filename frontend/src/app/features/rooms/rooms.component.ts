import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable, Subject, takeUntil } from 'rxjs';
import { map } from 'rxjs/operators';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TableComponent } from '../../shared/components/table/table.component';
import { RoomModalFormComponent } from '../../shared/components/room-modal-form/room-modal-form.component';
import { RoomService } from './rooms.service';
import { Room } from './models/room.interface';

@Component({
  selector: 'app-rooms',
  standalone: true,
  imports: [CommonModule, TableComponent, MatDialogModule],
  templateUrl: './rooms.component.html',
})
export class RoomsComponent implements OnDestroy {
  rooms$: Observable<Room[]>;
  showDeleteModal = false;
  roomToDelete: { id: number } | null = null;
  private destroy$ = new Subject<void>();

  // hide hotel-related columns (hotelId / hotelName) from table view
  columns = [
    'id',
    'number',
    'type',
    'floor',
    'capacity',
    'pricePerNight',
    'status',
  ];

  headersMap = {
    id: 'ID',
    number: 'N° Habitación',
    type: 'Tipo',
    floor: 'Piso',
    capacity: 'Capacidad',
    pricePerNight: 'Precio/Noche',
    status: 'Estado',
  };

  constructor(private dialog: MatDialog, private roomService: RoomService) {
    // Inicialmente cargamos las habitaciones y nos suscribimos al observable
    this.roomService.loadRooms();
    // strip hotel-related fields before sending to table
    this.rooms$ = this.roomService.rooms$.pipe(
      map(items => items.map(it => {
        const { hotelId, hotelName, ...rest } = it as any;
        return rest;
      })),
      takeUntil(this.destroy$)
    );
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  refreshRooms() {
    this.rooms$ = this.roomService.getRooms().pipe(
      takeUntil(this.destroy$)
    );
  }

  /** Abrir modal para crear o editar habitación */
  openRoomModal(room?: Room) {
    const dialogRef = this.dialog.open(RoomModalFormComponent, {
      width: '500px',
      data: { room }, // habitación opcional para edición
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        if (room) {
          this.roomService
            .updateRoom(room.id!, result)
            .subscribe(() => this.refreshRooms());
        } else {
          this.roomService
            .createRoom(result)
            .subscribe(() => this.refreshRooms());
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
      this.roomService.deleteRoom(this.roomToDelete.id).subscribe(() => {
        this.refreshRooms();
        this.cancelDelete();
      });
    }
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.roomToDelete = null;
  }

  columnTransformMap: Record<string, (value: any) => any> = {
    pricePerNight: (value: number) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(value)
  };
}
