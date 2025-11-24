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
import { Booking, BOOKING_STATUS_OPTIONS, BookingStatus } from '../../models/booking.interface';
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
  showDeleteModal = false;
  bookingToDelete: Booking | null = null;
  showCancelModal = false;
  bookingToCancel: Booking | null = null;
  
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
    // Use cached load to avoid flashing the list when returning from detail view
    this.loadBookings();
    this.setupFilters();

    // Rely on backend `totalAmount`; no client-side room price cache required
  }

  /**
   * Carga todas las reservas
   */
  loadBookings(forceRefresh = false): void {
    this.isLoading = true;
    this.bookingService.getCachedAll(forceRefresh).subscribe({
      next: (bookings) => {
        this.bookings = bookings || [];
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        // If cache read fails, try a full refresh
        this.bookingService.refreshAll().subscribe({
          next: (bookings) => {
            this.bookings = bookings || [];
            this.applyFilters();
            this.isLoading = false;
          },
          error: () => {
            this.showError('Error al cargar las reservas');
            this.isLoading = false;
          }
        });
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
    // Cargar los addons antes de abrir el modal
    // Use a local loading flow so the global `isLoading` spinner doesn't flash the table
    this.bookingService.getAddons(booking.id).subscribe({
      next: (addons) => {
        // Asegurar que cada addon tenga el subtotal calculado
        const addonsWithSubtotal = addons.map(addon => ({
          ...addon,
          subtotal: addon.price * addon.quantity
        }));
        const bookingWithAddons = { ...booking, addons: addonsWithSubtotal };
        
        const dialogRef = this.dialog.open(BookingModalFormComponent, {
          width: '800px',
          maxWidth: '90vw',
          data: { booking: bookingWithAddons },
          disableClose: true
        });

        dialogRef.afterClosed().subscribe(result => {
          if (result) {
            this.updateBooking(booking.id, result);
          }
        });
      },
      error: (error) => {
        console.error('Error al cargar addons:', error);
        // Abrir el modal sin addons si hay error
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
    });
  }

  /**
   * Crea una nueva reserva
   */
  private createBooking(data: any): void {
    // Optimistic UI: insert a temporary booking so the list updates immediately
    const tempId = -Date.now();
    const payload = data.booking as any;
    const tempBooking: Booking = {
      id: tempId,
      guestId: payload.guestId,
      guestName: (data && data.booking && data.booking.guestName) || 'Creando...',
      roomId: payload.roomId,
      roomNumber: '',
      checkInDate: payload.checkInDate,
      checkOutDate: payload.checkOutDate,
      status: payload.status,
      createdBy: payload.createdBy || 'system',
      bookingLeadTime: payload.bookingLeadTime || '',
      notes: payload.notes || '',
      hotelId: payload.hotelId || 0,
      totalAmount: undefined
    } as Booking;

    // Insert at top for immediate feedback
    this.bookings.unshift(tempBooking);
    this.applyFilters();

    // Call backend to create; on success replace temp entry, on error rollback
    this.bookingService.create(payload).subscribe({
      next: (booking) => {
        // Replace temp booking with actual server response
        const idx = this.bookings.findIndex(b => b.id === tempId);
        if (idx !== -1) {
          this.bookings[idx] = { ...this.bookings[idx], ...booking } as Booking;
        } else {
          this.bookings.unshift(booking);
        }

        // If there are addons, call replaceBookingAddons and then refresh booking
        if (data.addons && data.addons.length > 0) {
          const addonRequests = data.addons.map((addon: any) => ({ addonId: addon.addonId, quantity: addon.quantity ?? 1 }));
          this.bookingService.replaceBookingAddons(booking.id, addonRequests).subscribe({
            next: (updatedBooking) => {
              // Normalize and set
              if (updatedBooking?.addons && Array.isArray(updatedBooking.addons)) {
                updatedBooking.addons = updatedBooking.addons.map((a: any) => ({
                  ...a,
                  addonName: a.addonName ?? a.name ?? a.addon?.name ?? '',
                  subtotal: a.subtotal ?? ((a.price ?? a.addon?.price ?? 0) * (a.quantity ?? 1))
                }));
              }

              const i = this.bookings.findIndex(b => b.id === updatedBooking.id);
              if (i !== -1) this.bookings[i] = { ...this.bookings[i], ...updatedBooking };

              // Final authoritative fetch
              this.bookingService.getById(updatedBooking.id).subscribe({
                next: (fullBooking) => {
                  const j = this.bookings.findIndex(b => b.id === fullBooking.id);
                  if (j !== -1) this.bookings[j] = { ...this.bookings[j], ...fullBooking };
                  else this.bookings.unshift(fullBooking);
                  this.applyFilters();
                  this.showSuccess('Reserva creada exitosamente');
                },
                error: () => {
                  this.applyFilters();
                  this.showSuccess('Reserva creada exitosamente');
                }
              });
            },
            error: () => {
              this.showError('Reserva creada, pero no se pudieron asignar los servicios adicionales');
              // Refresh full list to reconcile
              this.loadBookings(true);
            }
          });
        } else {
          // No addons: fetch authoritative booking
          this.bookingService.getById(booking.id).subscribe({
            next: (fullBooking) => {
              const i = this.bookings.findIndex(b => b.id === fullBooking.id);
              if (i !== -1) this.bookings[i] = { ...this.bookings[i], ...fullBooking };
              else this.bookings.unshift(fullBooking);
              this.applyFilters();
              this.showSuccess('Reserva creada exitosamente');
            },
            error: () => {
              this.showSuccess('Reserva creada exitosamente');
              this.loadBookings(true);
            }
          });
        }
      },
      error: (error) => {
        // Rollback optimistic insert
        const idx = this.bookings.findIndex(b => b.id === tempId);
        if (idx !== -1) {
          this.bookings.splice(idx, 1);
        }
        this.applyFilters();
        this.showError(error.message || 'Error al crear la reserva');
      }
    });
  }

  /**
   * Actualiza una reserva existente
   */
  private updateBooking(id: number, data: any): void {
    // Optimistic UI update: apply local changes immediately and rollback on error
    const idx = this.bookings.findIndex(b => b.id === id);
    const originalBooking = idx !== -1 ? { ...this.bookings[idx] } : null;

    // Apply optimistic update locally
    if (idx !== -1) {
      const payload = { ...data.booking } as any;
      const optimistic = {
        ...this.bookings[idx],
        ...payload,
        addons: (data.addons || []).map((a: any) => ({ addonId: a.addonId, quantity: a.quantity ?? 1 }))
      } as Booking;

      this.bookings[idx] = optimistic;
      this.applyFilters();
    }

    // Call backend
    const payload = { ...data.booking } as any;
    payload.addons = (data.addons || []).map((a: any) => ({ addonId: a.addonId, quantity: a.quantity ?? 1 }));

    this.bookingService.update(id, payload).subscribe({
      next: (updatedBooking: any) => {
        if (updatedBooking && updatedBooking.id) {
          // Normalize addons subtotal if present
          if (updatedBooking.addons && Array.isArray(updatedBooking.addons)) {
            updatedBooking.addons = updatedBooking.addons.map((a: any) => ({
              ...a,
              subtotal: a.subtotal ?? ((a.price ?? a.addon?.price ?? 0) * (a.quantity ?? 1))
            }));
          }

          const index = this.bookings.findIndex(b => b.id === updatedBooking.id);
          if (index !== -1) {
            this.bookings[index] = { ...this.bookings[index], ...updatedBooking };
          } else {
            this.bookings.push(updatedBooking);
          }

          // Refresh authoritative booking
          this.bookingService.getById(updatedBooking.id).subscribe({
            next: (fullBooking) => {
              const i = this.bookings.findIndex(b => b.id === fullBooking.id);
              if (i !== -1) this.bookings[i] = { ...this.bookings[i], ...fullBooking };
              this.applyFilters();
              this.showSuccess('Reserva actualizada exitosamente');
            },
            error: () => {
              this.applyFilters();
              this.showSuccess('Reserva actualizada exitosamente');
            }
          });
          return;
        }

        // Fallback: reload all
        this.showSuccess('Reserva actualizada exitosamente');
        this.loadBookings(true);
      },
      error: (error) => {
        // Rollback optimistic update if present
        if (originalBooking && idx !== -1) {
          this.bookings[idx] = originalBooking;
          this.applyFilters();
        }

        if (error?.status === 409) {
          this.showError('La habitación no está disponible para las fechas solicitadas. Cambie fechas o habitación.');
        } else if (error?.status === 400 && error?.details) {
          const details = error.details;
          const fieldMessages = Object.entries(details).map(([k, v]) => `${k}: ${JSON.stringify(v)}`).join('\n');
          this.showError(`Error en los datos:\n${fieldMessages}`);
        } else {
          this.showError(error.message || 'Error al actualizar la reserva');
        }
      }
    });
  }

  /**
   * Agrega addons a una reserva nueva
   */
  private addBookingAddons(bookingId: number, addons: any[]): void {
    const addonRequests = addons.map(addon => ({
      addonId: addon.addonId,
      quantity: addon.quantity
    }));

    this.bookingService.addAddons(bookingId, addonRequests).subscribe({
      next: () => {
        this.showSuccess('Reserva creada con servicios adicionales');
        this.loadBookings();
      },
      error: (error) => {
        this.showError('Reserva creada, pero hubo un error al agregar los servicios adicionales');
        this.loadBookings();
      }
    });
  }

  /**
   * Compute booking total for display when backend doesn't provide `totalAmount`.
   * Returns null when not possible to compute.
   */
  

  /**
   * Reemplaza completamente los addons de una reserva (método simplificado con nuevo endpoint)
   */
  private replaceBookingAddons(bookingId: number, addons: any[]): void {
    // Preparar payload con solo addonId y quantity
    const addonRequests = addons.map(addon => ({
      addonId: addon.addonId,
      quantity: addon.quantity
    }));

    this.bookingService.replaceBookingAddons(bookingId, addonRequests).subscribe({
      next: () => {
        this.showSuccess('Reserva actualizada exitosamente');
        this.loadBookings();
      },
      error: (error) => {
        this.showError('Error al actualizar los servicios adicionales');
        this.loadBookings();
      }
    });
  }

  /**
   * Sincroniza los addons de una reserva (para actualizaciones) - LEGACY
   * @deprecated Reemplazado por replaceBookingAddons
   */
  private syncBookingAddons(bookingId: number, newAddons: any[]): void {
    this.bookingService.getAddons(bookingId).subscribe({
      next: (existingAddons) => {
        let operationsCompleted = 0;
        let hasError = false;
        const errors: string[] = [];
        
        console.log('Addons existentes:', existingAddons);
        console.log('Addons nuevos:', newAddons);
        
        // Calcular operaciones necesarias - usar addonId para comparar
        const addonsToDelete = existingAddons.filter(existing => 
          !newAddons.some(newAddon => newAddon.addonId === existing.addonId)
        );
        const addonsToUpdate = newAddons.filter(newAddon => 
          existingAddons.some(existing => existing.addonId === newAddon.addonId)
        );
        const addonsToCreate = newAddons.filter(newAddon => 
          !existingAddons.some(existing => existing.addonId === newAddon.addonId)
        );
        
        console.log('Para eliminar:', addonsToDelete);
        console.log('Para actualizar:', addonsToUpdate);
        console.log('Para crear:', addonsToCreate);
        
        const totalOperations = addonsToDelete.length + addonsToUpdate.length + addonsToCreate.length;
        
        // Si no hay operaciones, terminar
        if (totalOperations === 0) {
          this.showSuccess('Reserva actualizada exitosamente');
          this.loadBookings();
          return;
        }

        const checkCompletion = () => {
          operationsCompleted++;
          if (operationsCompleted >= totalOperations) {
            if (hasError) {
              console.error('Errores en sincronización de addons:', errors);
              this.showError('Reserva actualizada pero algunos servicios adicionales no se pudieron sincronizar');
            } else {
              this.showSuccess('Reserva actualizada exitosamente');
            }
            this.loadBookings();
          }
        };

        // Eliminar addons que ya no están
        addonsToDelete.forEach(addon => {
          const addonIdToDelete = addon.addonId;
          console.log('Eliminando addon con addonId:', addonIdToDelete);
          this.bookingService.removeAddon(bookingId, addonIdToDelete).subscribe({
            next: () => {
              console.log('Addon eliminado exitosamente:', addonIdToDelete);
              checkCompletion();
            },
            error: (err) => {
              console.error('Error eliminando addon:', addonIdToDelete, err);
              hasError = true;
              errors.push(`Error eliminando addon ${addonIdToDelete}`);
              checkCompletion();
            }
          });
        });

        // Actualizar cantidades
        addonsToUpdate.forEach(newAddon => {
          const existing = existingAddons.find(e => e.addonId === newAddon.addonId);
          const addonIdToUpdate = newAddon.addonId;
          
          if (!addonIdToUpdate) {
            console.error('addonId undefined en newAddon:', newAddon);
            hasError = true;
            errors.push('Addon sin ID válido');
            checkCompletion();
            return;
          }
          
          if (existing && existing.quantity !== newAddon.quantity) {
            console.log('Actualizando cantidad addon:', addonIdToUpdate, 'de', existing.quantity, 'a', newAddon.quantity);
            this.bookingService.updateAddonQuantity(bookingId, addonIdToUpdate, newAddon.quantity).subscribe({
              next: () => {
                console.log('Cantidad actualizada exitosamente:', addonIdToUpdate);
                checkCompletion();
              },
              error: (err) => {
                console.error('Error actualizando cantidad:', addonIdToUpdate, err);
                hasError = true;
                errors.push(`Error actualizando addon ${addonIdToUpdate}`);
                checkCompletion();
              }
            });
          } else {
            console.log('Sin cambios en addon:', addonIdToUpdate);
            checkCompletion();
          }
        });

        // Crear nuevos addons
        addonsToCreate.forEach(newAddon => {
          const addonIdToCreate = newAddon.addonId;
          console.log('Creando nuevo addon con addonId:', addonIdToCreate);
          this.bookingService.addAddons(bookingId, [{
            addonId: addonIdToCreate,
            quantity: newAddon.quantity
          }]).subscribe({
            next: () => {
              console.log('Addon creado exitosamente:', addonIdToCreate);
              checkCompletion();
            },
            error: (err) => {
              console.error('Error creando addon:', addonIdToCreate, err);
              hasError = true;
              errors.push(`Error creando addon ${addonIdToCreate}`);
              checkCompletion();
            }
          });
        });
      },
      error: (err) => {
        console.error('Error cargando addons existentes:', err);
        this.showError('Error al sincronizar servicios adicionales');
        this.loadBookings();
      }
    });
  }

  /**
   * Cancela una reserva
   */
  cancelBooking(booking: Booking): void {
    // Open confirmation modal instead of using native confirm()
    this.bookingToCancel = booking;
    this.showCancelModal = true;
  }

  /** Confirmación desde el modal: realiza la cancelación */
  confirmCancel(): void {
    if (!this.bookingToCancel) return;

    const id = this.bookingToCancel.id;

    // Optimistic UI update: mark the booking as CANCELLED locally so the table updates immediately
    const idx = this.bookings.findIndex(b => b.id === id);
    let originalStatus: BookingStatus | null = null;
    let localBookingRef: Booking | null = null;
    if (idx !== -1) {
      localBookingRef = this.bookings[idx];
      originalStatus = localBookingRef.status;
      localBookingRef.status = 'CANCELLED';
      this.applyFilters();
    }

    // Close modal immediately
    this.cancelCancel();

    // Call backend; refresh cache in background to keep authoritative state
    this.bookingService.cancel(id).subscribe({
      next: () => {
        this.showSuccess('Reserva cancelada exitosamente');
        // update cache in background without showing global loading spinner
        this.bookingService.refreshAll().subscribe({
          next: (bookings) => {
            this.bookings = bookings || [];
            this.applyFilters();
          },
          error: () => {
            // silent: keep optimistic UI, will be reconciled later
          }
        });
      },
      error: (error) => {
        // Rollback optimistic update
        if (localBookingRef && originalStatus !== null) {
          localBookingRef.status = originalStatus;
          this.applyFilters();
        }
        this.showError(error.message || 'Error al cancelar la reserva');
      }
    });
  }

  /** Cierra el modal de cancelación sin realizar acción */
  cancelCancel(): void {
    this.showCancelModal = false;
    this.bookingToCancel = null;
  }

  /**
   * Elimina una reserva
   */
  deleteBooking(booking: Booking): void {
    // Open confirmation modal instead of using native confirm()
    this.bookingToDelete = booking;
    this.showDeleteModal = true;
  }

  /** Confirmación desde el modal: realiza la eliminación */
  confirmDelete(): void {
    if (!this.bookingToDelete) return;

    const id = this.bookingToDelete.id;

    // Optimistic UI update: remove the booking from local array immediately
    const idx = this.bookings.findIndex(b => b.id === id);
    let removed: Booking | null = null;
    if (idx !== -1) {
      removed = this.bookings.splice(idx, 1)[0];
      this.applyFilters();
    }

    // Close modal immediately
    this.cancelDelete();

    // Call backend; refresh cache in background to keep authoritative state
    this.bookingService.delete(id).subscribe({
      next: () => {
        this.showSuccess('Reserva eliminada exitosamente');
        this.bookingService.refreshAll().subscribe({
          next: (bookings) => {
            this.bookings = bookings || [];
            this.applyFilters();
          },
          error: () => {
            // silent: keep optimistic UI
          }
        });
      },
      error: (error) => {
        // Rollback optimistic deletion
        if (removed) {
          // insert back at original index if possible
          const insertAt = Math.min(idx, this.bookings.length);
          this.bookings.splice(insertAt, 0, removed);
          this.applyFilters();
        }
        this.showError(error.message || 'Error al eliminar la reserva');
      }
    });
  }

  /** Cierra el modal de eliminación sin realizar acción */
  cancelDelete(): void {
    this.showDeleteModal = false;
    this.bookingToDelete = null;
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
