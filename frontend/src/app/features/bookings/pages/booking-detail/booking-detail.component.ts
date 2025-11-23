import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { Booking, BOOKING_STATUS_OPTIONS } from '../../models/booking.interface';
import { BookingService } from '../../services/booking.service';
import { BookingModalFormComponent } from '../../../../shared/components/booking-modal-form/booking-modal-form.component';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatListModule,
    MatSnackBarModule
  ],
  templateUrl: './booking-detail.component.html',
  styleUrls: ['./booking-detail.component.scss']
})
export class BookingDetailComponent implements OnInit {
  booking: Booking | null = null;
  isLoading = false;
  bookingId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.bookingId = +params['id'];
      if (this.bookingId) {
        this.loadBookingDetail();
      }
    });
  }

  /**
   * Carga los detalles de la reserva
   */
  loadBookingDetail(): void {
    this.isLoading = true;
    this.bookingService.getById(this.bookingId).subscribe({
      next: (booking) => {
        this.booking = booking;
        this.loadBookingAddons();
        this.isLoading = false;
      },
      error: (error) => {
        this.showError('Error al cargar los detalles de la reserva');
        this.isLoading = false;
        this.router.navigate(['/bookings']);
      }
    });
  }

  /**
   * Carga los addons de la reserva
   */
  loadBookingAddons(): void {
    if (!this.booking) return;

    this.bookingService.getAddons(this.booking.id).subscribe({
      next: (addons) => {
        if (this.booking) {
          // Calcular subtotal para cada addon
          this.booking.addons = addons.map(addon => ({
            ...addon,
            subtotal: addon.price * addon.quantity
          }));
        }
      },
      error: (error) => {
        console.error('Error al cargar addons:', error);
      }
    });
  }

  /**
   * Abre el modal para editar la reserva
   */
  editBooking(): void {
    if (!this.booking) return;

    // Asegurar que los addons tengan subtotal calculado
    const bookingWithSubtotals = {
      ...this.booking,
      addons: this.booking.addons?.map(addon => ({
        ...addon,
        subtotal: addon.subtotal || (addon.price * addon.quantity)
      }))
    };

    const dialogRef = this.dialog.open(BookingModalFormComponent, {
      width: '800px',
      maxWidth: '90vw',
      data: { booking: bookingWithSubtotals },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateBooking(result);
      }
    });
  }

  /**
   * Actualiza la reserva
   */
  private updateBooking(data: any): void {
    if (!this.booking) return;

    this.isLoading = true;
    this.bookingService.update(this.booking.id, data.booking).subscribe({
      next: (booking) => {
        // Si hay addons, sincronizarlos
        if (data.addons) {
          this.syncAddons(this.booking!.id, data.addons);
        } else {
          this.showSuccess('Reserva actualizada exitosamente');
          this.loadBookingDetail();
        }
      },
      error: (error) => {
        this.showError(error.message || 'Error al actualizar la reserva');
        this.isLoading = false;
      }
    });
  }

  /**
   * Sincroniza los addons de la reserva
   */
  private syncAddons(bookingId: number, newAddons: any[]): void {
    this.bookingService.getAddons(bookingId).subscribe({
      next: (existingAddons) => {
        let operationsCompleted = 0;
        let hasError = false;
        
        // Calcular operaciones necesarias
        const addonsToDelete = existingAddons.filter(existing => 
          !newAddons.some(newAddon => newAddon.addonId === existing.addonId)
        );
        const addonsToUpdate = newAddons.filter(newAddon => 
          existingAddons.some(existing => existing.addonId === newAddon.addonId)
        );
        const addonsToCreate = newAddons.filter(newAddon => 
          !existingAddons.some(existing => existing.addonId === newAddon.addonId)
        );
        
        const totalOperations = addonsToDelete.length + addonsToUpdate.length + addonsToCreate.length;
        
        if (totalOperations === 0) {
          this.showSuccess('Reserva actualizada exitosamente');
          this.loadBookingDetail();
          return;
        }

        const checkCompletion = () => {
          operationsCompleted++;
          if (operationsCompleted >= totalOperations) {
            if (hasError) {
              this.showError('Reserva actualizada con algunos errores en servicios adicionales');
            } else {
              this.showSuccess('Reserva actualizada exitosamente');
            }
            this.loadBookingDetail();
          }
        };

        // Eliminar addons
        addonsToDelete.forEach(addon => {
          this.bookingService.removeAddon(bookingId, addon.addonId).subscribe({
            next: () => checkCompletion(),
            error: () => { hasError = true; checkCompletion(); }
          });
        });

        // Actualizar cantidades
        addonsToUpdate.forEach(newAddon => {
          const existing = existingAddons.find(e => e.addonId === newAddon.addonId);
          if (existing && existing.quantity !== newAddon.quantity) {
            this.bookingService.updateAddonQuantity(bookingId, newAddon.addonId, newAddon.quantity).subscribe({
              next: () => checkCompletion(),
              error: () => { hasError = true; checkCompletion(); }
            });
          } else {
            checkCompletion();
          }
        });

        // Crear nuevos
        addonsToCreate.forEach(newAddon => {
          this.bookingService.addAddons(bookingId, [{
            addonId: newAddon.addonId,
            quantity: newAddon.quantity
          }]).subscribe({
            next: () => checkCompletion(),
            error: () => { hasError = true; checkCompletion(); }
          });
        });
      },
      error: () => {
        this.showError('Error al sincronizar servicios adicionales');
        this.loadBookingDetail();
      }
    });
  }

  /**
   * Cancela la reserva
   */
  cancelBooking(): void {
    if (!this.booking) return;

    const confirmed = confirm(
      `¿Está seguro de cancelar la reserva #${this.booking.id}?\n` +
      `Esta acción no se puede deshacer.`
    );

    if (!confirmed) return;

    this.isLoading = true;
    this.bookingService.cancel(this.booking.id).subscribe({
      next: () => {
        this.showSuccess('Reserva cancelada exitosamente');
        this.loadBookingDetail();
      },
      error: (error) => {
        this.showError(error.message || 'Error al cancelar la reserva');
        this.isLoading = false;
      }
    });
  }

  /**
   * Elimina la reserva
   */
  deleteBooking(): void {
    if (!this.booking) return;

    const confirmed = confirm(
      `¿Está seguro de eliminar la reserva #${this.booking.id}?\n` +
      `Esta acción no se puede deshacer y eliminará todos los datos asociados.`
    );

    if (!confirmed) return;

    this.isLoading = true;
    this.bookingService.delete(this.booking.id).subscribe({
      next: () => {
        this.showSuccess('Reserva eliminada exitosamente');
        this.router.navigate(['/bookings']);
      },
      error: (error) => {
        this.showError(error.message || 'Error al eliminar la reserva');
        this.isLoading = false;
      }
    });
  }

  /**
   * Vuelve al listado de reservas
   */
  goBack(): void {
    this.router.navigate(['/bookings']);
  }

  /**
   * Obtiene la etiqueta del estado
   */
  getStatusLabel(status: string): string {
    const option = BOOKING_STATUS_OPTIONS.find(s => s.value === status);
    return option?.label || status;
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
   * Calcula el número de noches
   */
  calculateNights(): number {
    if (!this.booking) return 0;
    
    const checkIn = new Date(this.booking.checkInDate);
    const checkOut = new Date(this.booking.checkOutDate);
    const diffTime = Math.abs(checkOut.getTime() - checkIn.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  /**
   * Calcula el subtotal del hospedaje (sin addons)
   */
  calculateAccommodationSubtotal(): number {
    return this.booking?.totalAmount || 0;
  }

  /**
   * Calcula el total de addons
   */
  calculateAddonsTotal(): number {
    if (!this.booking?.addons) return 0;
    return this.booking.addons.reduce((sum, addon) => sum + (addon.subtotal || 0), 0);
  }

  /**
   * Calcula el total de la reserva
   */
  calculateTotal(): number {
    if (!this.booking) return 0;
    return (this.booking.totalAmount || 0) + this.calculateAddonsTotal();
  }

  /**
   * Verifica si la reserva puede ser editada
   */
  canEdit(): boolean {
    if (!this.booking) return false;
    return this.booking.status !== 'CANCELLED' && this.booking.status !== 'CHECKED_OUT';
  }

  /**
   * Verifica si la reserva puede ser cancelada
   */
  canCancel(): boolean {
    if (!this.booking) return false;
    return this.booking.status !== 'CANCELLED' && this.booking.status !== 'CHECKED_OUT';
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
