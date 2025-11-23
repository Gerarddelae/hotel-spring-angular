import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { Router } from '@angular/router';
import { Booking, BOOKING_STATUS_OPTIONS } from '../../models/booking.interface';
import { BookingFilters } from '../../models/booking-filters.interface';
import { BookingService } from '../../services/booking.service';
import { BookingModalFormComponent } from '../../../../shared/components/booking-modal-form/booking-modal-form.component';

@Component({
  selector: 'app-booking-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule,
    MatSnackBarModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule
  ],
  templateUrl: './booking-list.component.html',
  styleUrls: ['./booking-list.component.scss']
})
export class BookingListComponent implements OnInit {
  displayedColumns: string[] = [
    'id',
    'guestName',
    'roomNumber',
    'checkInDate',
    'checkOutDate',
    'status',
    'totalAmount',
    'actions'
  ];

  bookings: Booking[] = [];
  filteredBookings: Booking[] = [];
  isLoading = false;
  
  filterForm: FormGroup;
  statusOptions = BOOKING_STATUS_OPTIONS;

  constructor(
    private bookingService: BookingService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.filterForm = this.fb.group({
      searchQuery: [''],
      status: [''],
      checkInFrom: [''],
      checkInTo: [''],
      checkOutFrom: [''],
      checkOutTo: ['']
    });
  }

  ngOnInit(): void {
    this.loadBookings();
    this.setupFilters();
  }

  /**
   * Carga todas las reservas
   */
  loadBookings(): void {
    this.isLoading = true;
    this.bookingService.getAll().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        this.showError('Error al cargar las reservas');
        this.isLoading = false;
      }
    });
  }

  /**
   * Configura los filtros reactivos
   */
  private setupFilters(): void {
    this.filterForm.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  /**
   * Aplica los filtros a la lista de reservas
   */
  applyFilters(): void {
    const filters = this.filterForm.value;
    
    this.filteredBookings = this.bookings.filter(booking => {
      // Filtro de búsqueda por texto
      if (filters.searchQuery) {
        const query = filters.searchQuery.toLowerCase();
        const matchesGuest = booking.guestName?.toLowerCase().includes(query);
        const matchesRoom = booking.roomNumber?.toLowerCase().includes(query);
        const matchesId = booking.id.toString().includes(query);
        
        if (!matchesGuest && !matchesRoom && !matchesId) {
          return false;
        }
      }

      // Filtro de estado
      if (filters.status && booking.status !== filters.status) {
        return false;
      }

      // Filtro de fecha de check-in
      if (filters.checkInFrom && booking.checkInDate < filters.checkInFrom) {
        return false;
      }
      if (filters.checkInTo && booking.checkInDate > filters.checkInTo) {
        return false;
      }

      // Filtro de fecha de check-out
      if (filters.checkOutFrom && booking.checkOutDate < filters.checkOutFrom) {
        return false;
      }
      if (filters.checkOutTo && booking.checkOutDate > filters.checkOutTo) {
        return false;
      }

      return true;
    });
  }

  /**
   * Limpia todos los filtros
   */
  clearFilters(): void {
    this.filterForm.reset({
      searchQuery: '',
      status: '',
      checkInFrom: '',
      checkInTo: '',
      checkOutFrom: '',
      checkOutTo: ''
    });
  }

  /**
   * Abre el modal para crear una nueva reserva
   */
  openCreateDialog(): void {
    const dialogRef = this.dialog.open(BookingModalFormComponent, {
      width: '800px',
      maxWidth: '90vw',
      data: {},
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createBooking(result);
      }
    });
  }

  /**
   * Abre el modal para editar una reserva
   */
  openEditDialog(booking: Booking): void {
    const dialogRef = this.dialog.open(BookingModalFormComponent, {
      width: '800px',
      maxWidth: '90vw',
      data: { booking },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateBooking(booking.id, result);
      }
    });
  }

  /**
   * Crea una nueva reserva
   */
  private createBooking(data: any): void {
    this.isLoading = true;
    
    this.bookingService.create(data.booking).subscribe({
      next: (booking) => {
        // Si hay addons, agregarlos
        if (data.addons && data.addons.length > 0) {
          this.addBookingAddons(booking.id, data.addons);
        } else {
          this.showSuccess('Reserva creada exitosamente');
          this.loadBookings();
        }
      },
      error: (error) => {
        this.showError(error.message || 'Error al crear la reserva');
        this.isLoading = false;
      }
    });
  }

  /**
   * Actualiza una reserva existente
   */
  private updateBooking(id: number, data: any): void {
    this.isLoading = true;
    
    this.bookingService.update(id, data.booking).subscribe({
      next: (booking) => {
        // Si hay addons, actualizarlos
        if (data.addons && data.addons.length > 0) {
          this.addBookingAddons(booking.id, data.addons);
        } else {
          this.showSuccess('Reserva actualizada exitosamente');
          this.loadBookings();
        }
      },
      error: (error) => {
        this.showError(error.message || 'Error al actualizar la reserva');
        this.isLoading = false;
      }
    });
  }

  /**
   * Agrega addons a una reserva
   */
  private addBookingAddons(bookingId: number, addons: any[]): void {
    const addonRequests = addons.map(addon => ({
      addonId: addon.addonId,
      quantity: addon.quantity
    }));

    this.bookingService.addAddons(bookingId, addonRequests).subscribe({
      next: () => {
        this.showSuccess('Reserva guardada con servicios adicionales');
        this.loadBookings();
      },
      error: (error) => {
        this.showError('Reserva creada, pero hubo un error al agregar los servicios adicionales');
        this.loadBookings();
      }
    });
  }

  /**
   * Cancela una reserva
   */
  cancelBooking(booking: Booking): void {
    const confirmed = confirm(
      `¿Está seguro de cancelar la reserva #${booking.id}?\n` +
      `Huésped: ${booking.guestName}\n` +
      `Habitación: ${booking.roomNumber}`
    );

    if (!confirmed) {
      return;
    }

    this.isLoading = true;
    this.bookingService.cancel(booking.id).subscribe({
      next: () => {
        this.showSuccess('Reserva cancelada exitosamente');
        this.loadBookings();
      },
      error: (error) => {
        this.showError(error.message || 'Error al cancelar la reserva');
        this.isLoading = false;
      }
    });
  }

  /**
   * Elimina una reserva
   */
  deleteBooking(booking: Booking): void {
    const confirmed = confirm(
      `¿Está seguro de eliminar la reserva #${booking.id}?\n` +
      `Esta acción no se puede deshacer.\n\n` +
      `Huésped: ${booking.guestName}\n` +
      `Habitación: ${booking.roomNumber}`
    );

    if (!confirmed) {
      return;
    }

    this.isLoading = true;
    this.bookingService.delete(booking.id).subscribe({
      next: () => {
        this.showSuccess('Reserva eliminada exitosamente');
        this.loadBookings();
      },
      error: (error) => {
        this.showError(error.message || 'Error al eliminar la reserva');
        this.isLoading = false;
      }
    });
  }

  /**
   * Navega a la página de detalle de una reserva
   */
  viewDetails(booking: Booking): void {
    this.router.navigate(['/bookings', booking.id]);
  }

  /**
   * Obtiene el color del chip según el estado
   */
  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PENDING': 'accent',
      'CONFIRMED': 'primary',
      'CHECKED_IN': 'primary',
      'CHECKED_OUT': '',
      'CANCELLED': 'warn'
    };
    return colors[status] || '';
  }

  /**
   * Obtiene la etiqueta del estado
   */
  getStatusLabel(status: string): string {
    const option = this.statusOptions.find(s => s.value === status);
    return option?.label || status;
  }

  /**
   * Muestra un mensaje de éxito
   */
  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  /**
   * Muestra un mensaje de error
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
