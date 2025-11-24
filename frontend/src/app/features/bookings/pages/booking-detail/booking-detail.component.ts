import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Booking, BOOKING_STATUS_OPTIONS } from '../../models/booking.interface';
import { RoomService } from '../../../rooms/rooms.service';
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
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './booking-detail.component.html',
  styleUrls: ['./booking-detail.component.scss']
})
export class BookingDetailComponent implements OnInit {
  booking: Booking | null = null;
  isLoading = false;
  bookingId: number = 0;
  @ViewChild('cancelDialog') cancelDialogTpl!: TemplateRef<any>;
  @ViewChild('deleteDialog') deleteDialogTpl!: TemplateRef<any>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private roomService: RoomService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    
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

            // If backend did not return an accommodation subtotal, try to derive it from the room's pricePerNight
            if ((this.booking.accommodationSubtotal === undefined || this.booking.accommodationSubtotal === null) && this.booking.roomId) {
              this.roomService.getRoomById(this.booking.roomId).subscribe({
                next: (room) => {
                  const nights = this.calculateNights();
                  if (nights > 0 && room?.pricePerNight) {
                    this.booking!.accommodationSubtotal = Math.round((room.pricePerNight * nights) * 100) / 100;
                  }
                },
                error: (err) => {
                  // If room lookup fails, just continue; UI will show '-' for missing subtotal
                  console.warn('No se pudo obtener habitación para derivar subtotal:', err);
                }
              });
            }

            // Load addons and keep the loading spinner until addons (and fallback totals) are processed
            this.loadBookingAddons();
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
          this.booking.addons = addons.map(addon => {
            const a: any = addon;
            return {
              ...a,
              addonName: a.addonName ?? a.name ?? a.addon?.name ?? '',
              price: a.price ?? a.addon?.price ?? 0,
              quantity: a.quantity ?? 1,
              subtotal: a.subtotal ?? ((a.price ?? a.addon?.price ?? 0) * (a.quantity ?? 1))
            };
          });
          // If backend hasn't returned a totalAmount yet, compute a client-side fallback
          const addonsTotal = this.booking.addons.reduce((s: number, it: any) => s + (it.subtotal || 0), 0);
          if (this.booking && (this.booking.totalAmount === undefined || this.booking.totalAmount === null || this.booking.totalAmount === 0)) {
            this.booking.totalAmount = (this.booking.accommodationSubtotal ?? 0) + addonsTotal;
          }
        }
        // Done loading booking details and addons
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar addons:', error);
        // Even on error, stop the loading spinner so the UI is usable
        this.isLoading = false;
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
    // El backend espera el payload con la lista completa de addons para reemplazar la actual.
    const payload = { ...data.booking } as any;
    if (data.addons) {
      payload.addons = data.addons.map((a: any) => ({ addonId: a.addonId, quantity: a.quantity ?? 1 }));
    } else {
      payload.addons = [];
    }

    this.bookingService.update(this.booking.id, payload).subscribe({
      next: (bookingResp) => {
        this.showSuccess('Reserva actualizada exitosamente');
        this.loadBookingDetail();
      },
      error: (error) => {
        // Manejo de errores específicos según status
        if (error?.status === 409) {
          this.showError('La habitación no está disponible para las fechas solicitadas. Por favor, cambie las fechas o la habitación.');
        } else if (error?.status === 400) {
          // Mostrar errores por campo si el backend los devuelve en details
          const details = error?.details;
          if (details && typeof details === 'object') {
            // Intentar mapear a mensajes por campo
            const fieldMessages = Object.entries(details).map(([k, v]) => `${k}: ${JSON.stringify(v)}`).join('\n');
            this.showError(`Error en los datos: \n${fieldMessages}`);
          } else {
            this.showError(error.message || 'Datos inválidos');
          }
        } else if (error?.status === 404) {
          this.showError('Reserva o recurso no encontrado');
          this.router.navigate(['/bookings']);
        } else {
          this.showError(error?.message || 'Error al actualizar la reserva');
        }

        this.isLoading = false;
      }
    });
  }

  /**
   * Reemplaza completamente los addons de una reserva (método simplificado)
   */
  private replaceAddons(bookingId: number, addons: any[]): void {
    // Preparar payload con solo addonId y quantity
    const addonRequests = addons.map(addon => ({
      addonId: addon.addonId,
      quantity: addon.quantity
    }));

    this.bookingService.replaceBookingAddons(bookingId, addonRequests).subscribe({
      next: () => {
        this.showSuccess('Reserva actualizada exitosamente');
        this.loadBookingDetail();
      },
      error: (error) => {
        this.showError('Error al actualizar los servicios adicionales');
        this.loadBookingDetail();
      }
    });
  }

  /**
   * Sincroniza los addons de la reserva - LEGACY
   * @deprecated Reemplazado por replaceAddons
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
    // Open confirmation dialog (template)
    this.dialog.open(this.cancelDialogTpl, { width: '480px' });
  }

  /**
   * Elimina la reserva
   */
  deleteBooking(): void {
    if (!this.booking) return;
    this.dialog.open(this.deleteDialogTpl, { width: '480px' });
  }

  /** Close any open dialog (used by template buttons) */
  closeDialog(): void {
    this.dialog.closeAll();
  }

  /** Called from the cancel confirmation dialog */
  confirmCancel(): void {
    if (!this.booking) return;
    this.dialog.closeAll();
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

  /** Called from the delete confirmation dialog */
  confirmDelete(): void {
    if (!this.booking) return;
    this.dialog.closeAll();
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
   * Obtiene el precio por noche derivado del `accommodationSubtotal` si el backend
   * no proporciona explícitamente el `pricePerNight` en la reserva.
   * Retorna null si no es posible determinarlo.
   */
  getAccommodationPricePerNight(): number | null {
    if (!this.booking) return null;
    const nights = this.calculateNights();
    if (!nights || nights <= 0) return null;

    const acc = this.booking.accommodationSubtotal;
    if (acc === undefined || acc === null) return null;

    // Derivar precio por noche a partir del subtotal (round a 2 decimales)
    const price = acc / nights;
    return Math.round(price * 100) / 100;
  }

  /**
   * Calcula el subtotal del hospedaje (sin addons)
   */
  calculateAccommodationSubtotal(): number {
    return this.booking?.accommodationSubtotal ?? 0;
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
    return this.booking?.totalAmount ?? 0;
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
