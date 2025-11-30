import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { Observable, debounceTime, distinctUntilChanged, switchMap, startWith, map, of } from 'rxjs';
import { Booking, BookingRequest, BookingAddon, BOOKING_STATUS_OPTIONS } from '../../../features/bookings/models/booking.interface';
import { BookingService } from '../../../features/bookings/services/booking.service';
import { RoomService } from '../../../features/rooms/rooms.service';
import { GuestsService } from '../../../features/guests/guests.service';
import { AuthService } from '../../../auth/auth.service';
import { dateRangeValidator } from '../../validators/date.validators';
import { AddonSelectorComponent } from '../addon-selector/addon-selector.component';

interface BookingModalData {
  booking?: Booking;
  preselectedRoomId?: number;
  preselectedCheckIn?: Date;
  preselectedCheckOut?: Date;
}

@Component({
  selector: 'app-booking-modal-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    AddonSelectorComponent
  ],
  templateUrl: './booking-modal-form.component.html',
  styleUrls: ['./booking-modal-form.component.scss']
})
export class BookingModalFormComponent implements OnInit {
  bookingForm: FormGroup;
  statusOptions = BOOKING_STATUS_OPTIONS;
  showSummaryModal = false;
  summaryGuestName = '';
  
  filteredGuests$!: Observable<any[]>;
  availableRooms: any[] = [];
  availableAddons: any[] = [];
  selectedAddons: BookingAddon[] = [];
  selectedRoom: any = null;
  
  isLoadingGuests = false;
  isLoadingRooms = false;
  isCheckingAvailability = false;
  isRoomAvailable = true;
  
  currentUser: any;
  minDate: Date | null = null;
  isEditMode: boolean;
  // Minimum allowed date for check-out (based on selected check-in)
  checkOutMin: Date | null = null;
  // Start date shown when opening the check-out datepicker (not used when not auto-opening)
  // kept only for potential future use
  // startAtCheckOut: Date | null = null;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<BookingModalFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: BookingModalData,
    private bookingService: BookingService,
    private roomService: RoomService,
    private guestService: GuestsService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.currentUser = this.authService.getCurrentUser();
    this.isEditMode = !!data.booking;
    
    // Only apply minDate restriction in create mode
    this.minDate = this.isEditMode ? null : new Date();
    
    // Convert date strings to Date objects for Angular Material Datepicker
    // Use local date to avoid timezone conversion issues
    const checkInDate = data.booking?.checkInDate 
      ? this.parseLocalDate(data.booking.checkInDate) 
      : (data.preselectedCheckIn || '');
    const checkOutDate = data.booking?.checkOutDate 
      ? this.parseLocalDate(data.booking.checkOutDate) 
      : (data.preselectedCheckOut || '');
    const leadTime = data.booking?.bookingLeadTime ? this.parseLocalDate(data.booking.bookingLeadTime) : '';
    
    // Use preselected roomId if provided (from dashboard filter)
    const roomId = data.booking?.roomId || data.preselectedRoomId || null;
    
    this.bookingForm = this.fb.group({
      guestId: [data.booking?.guestId || null, Validators.required],
      guestSearch: [''],
      roomId: [roomId, Validators.required],
      checkInDate: [checkInDate, Validators.required],
      checkOutDate: [checkOutDate, Validators.required],
      status: [data.booking?.status || 'PENDING', Validators.required],
      bookingLeadTime: [leadTime, Validators.required],
      notes: [data.booking?.notes || '']
    }, {
      validators: [dateRangeValidator('checkInDate', 'checkOutDate')]
    });

    // Do not copy addons here; we'll enrich them once availableAddons are loaded
  }

  ngOnInit(): void {
    this.loadActiveAddons();
    this.setupGuestAutocomplete();
    this.setupRoomAvailabilityCheck();
    
    // Si hay un booking con guestId, cargar el guest completo
    if (this.data.booking?.guestId) {
      this.guestService.get(this.data.booking.guestId).subscribe({
        next: (guest) => {
          this.bookingForm.patchValue({ guestSearch: guest });
        },
        error: () => {
          // Fallback: usar el nombre si está disponible
          if (this.data.booking?.guestName) {
            this.bookingForm.patchValue({ guestSearch: this.data.booking.guestName });
          }
        }
      });
    }

  }

  /**
   * Parse date string to local Date object without timezone conversion
   * Input: "2024-11-25" -> Output: Date object for Nov 25 in local timezone
   */
  private parseLocalDate(dateString: string): Date {
    const [year, month, day] = dateString.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  /**
   * Configura el autocomplete de huéspedes
   */
  private setupGuestAutocomplete(): void {
    this.filteredGuests$ = this.bookingForm.get('guestSearch')!.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(value => {
        // Si es un objeto (guest seleccionado), no buscar
        if (value && typeof value === 'object') {
          this.isLoadingGuests = false;
          return of([]);
        }
        // Si es string y tiene al menos 2 caracteres, buscar
        if (typeof value === 'string' && value.length >= 2) {
          this.isLoadingGuests = true;
          return this.guestService.search(value).pipe(
            map(guests => {
              this.isLoadingGuests = false;
              return guests;
            })
          );
        }
        this.isLoadingGuests = false;
        return of([]);
      })
    );
  }

  /**
   * Configura la verificación de disponibilidad de habitación
   */
  private setupRoomAvailabilityCheck(): void {
    // Monitorear cambios en fechas
    this.bookingForm.get('checkInDate')?.valueChanges.subscribe((value: any) => {
      this.loadAvailableRooms();
      this.checkRoomAvailability();

      // Cuando se selecciona check-in en modo creación, fijar la mínima fecha
      // para check-out y preparar el datepicker para abrir mostrando la fecha seleccionada.
      if (value) {
        const selected = new Date(value);
        this.checkOutMin = selected;
        // Si la fecha de check-out actual es anterior a la mínima, limpiarla
        const currentOut = this.bookingForm.get('checkOutDate')?.value;
        if (currentOut && new Date(currentOut) <= selected) {
          this.bookingForm.get('checkOutDate')?.setValue('');
        }
      }
    });

    this.bookingForm.get('checkOutDate')?.valueChanges.subscribe(() => {
      this.loadAvailableRooms();
      this.checkRoomAvailability();
    });

    this.bookingForm.get('roomId')?.valueChanges.subscribe((roomId) => {
      this.selectedRoom = this.availableRooms.find(r => r.id === roomId);
      this.checkRoomAvailability();
    });

    // Cargar habitaciones disponibles si hay fechas iniciales (edición o pre-selección desde dashboard)
    const hasInitialDates = this.bookingForm.get('checkInDate')?.value && this.bookingForm.get('checkOutDate')?.value;
    if (hasInitialDates) {
      this.loadAvailableRooms();
    }
  }

  /**
   * Carga habitaciones disponibles según las fechas
   */
  private loadAvailableRooms(): void {
    const checkIn = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;

    if (!checkIn || !checkOut || new Date(checkOut) <= new Date(checkIn)) {
      this.availableRooms = [];
      return;
    }

    this.isLoadingRooms = true;
    const checkInStr = this.formatDate(checkIn);
    const checkOutStr = this.formatDate(checkOut);

    this.bookingService.getAvailableRooms(checkInStr, checkOutStr).subscribe({
      next: (rooms) => {
        this.availableRooms = rooms;
        this.isLoadingRooms = false;
        
        // Si estamos editando, incluir la habitación actual
        if (this.data.booking && this.data.booking.roomId) {
          const currentRoomExists = rooms.some(r => r.id === this.data.booking!.roomId);
          if (!currentRoomExists) {
            // Intentar obtener la habitación completa (con pricePerNight) y añadirla
            this.roomService.getRoomById(this.data.booking.roomId).subscribe({
              next: (room) => {
                // Añadir la habitación completa al listado
                this.availableRooms.push(room);
                // Si el formulario ya tiene roomId seleccionado, actualizar selectedRoom
                const selectedId = this.bookingForm.get('roomId')?.value;
                if (selectedId === room.id) {
                  this.selectedRoom = room;
                }
              },
              error: () => {
                // Fallback: push minimal info si no se puede obtener
                this.availableRooms.push({
                  id: this.data.booking!.roomId,
                  number: this.data.booking!.roomNumber,
                  type: 'CURRENT'
                });
              }
            });
          } else {
            // si ya existe en la lista, asegurarse de setear selectedRoom si aplica
            const selectedId = this.bookingForm.get('roomId')?.value;
            if (selectedId) {
              this.selectedRoom = this.availableRooms.find(r => r.id === selectedId) ?? null;
            }
          }
        } 
        // Si hay roomId pre-seleccionado (desde dashboard), actualizar selectedRoom
        else if (this.data.preselectedRoomId) {
          const preselectedRoom = this.availableRooms.find(r => r.id === this.data.preselectedRoomId);
          if (preselectedRoom) {
            this.selectedRoom = preselectedRoom;
          }
        }
      },
      error: (error) => {
        this.isLoadingRooms = false;
        this.showError('Error al cargar habitaciones disponibles');
      }
    });
  }

  /**
   * Verifica disponibilidad de la habitación seleccionada
   */
  private checkRoomAvailability(): void {
    const roomId = this.bookingForm.get('roomId')?.value;
    const checkIn = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;

    if (!roomId || !checkIn || !checkOut) {
      this.isRoomAvailable = true;
      return;
    }

    this.isCheckingAvailability = true;
    const checkInStr = this.formatDate(checkIn);
    const checkOutStr = this.formatDate(checkOut);

    this.bookingService.checkRoomAvailability({
      roomId,
      checkInDate: checkInStr,
      checkOutDate: checkOutStr,
      excludeBookingId: this.data.booking?.id
    }).subscribe({
      next: (available) => {
        this.isRoomAvailable = available;
        this.isCheckingAvailability = false;
        
        if (!available) {
          this.showError('La habitación no está disponible en las fechas seleccionadas');
        }
      },
      error: (error) => {
        this.isCheckingAvailability = false;
        this.isRoomAvailable = false;
        this.showError('Error al verificar disponibilidad');
      }
    });
  }

  /**
   * Carga los addons activos disponibles
   */
  private loadActiveAddons(): void {
    this.bookingService.getActiveAddons().subscribe({
      next: (addons) => {
        this.availableAddons = addons;

        // Si la ventana de edición recibió addons desde el backend, enriquecerlos
        // con los datos completos (nombre, precio) usando la lista de addons activos.
        if (this.data.booking?.addons) {
          this.selectedAddons = this.data.booking.addons.map((a: any) => {
            const addonId = a.addonId ?? a.id ?? (a.addon && a.addon.id);
            const found = this.availableAddons.find((x: any) => x.id === addonId || x.addonId === addonId);
            const price = found?.price ?? a.price ?? (a.addon && a.addon.price) ?? 0;
            const name = found?.name ?? a.addonName ?? (a.addon && a.addon.name) ?? '';
            const quantity = a.quantity ?? 1;

            return {
              addonId,
              addonName: name,
              price,
              quantity,
              subtotal: price * quantity
            } as BookingAddon;
          });
        }
      },
      error: (error) => {
        console.error('Error al cargar addons:', error);
      }
    });
  }

  /**
   * Selecciona un huésped del autocomplete
   */
  onGuestSelected(guest: any): void {
    this.bookingForm.patchValue({
      guestId: guest.id,
      guestSearch: guest
    });
  }

  /**
   * Muestra el nombre del huésped en el autocomplete
   */
  displayGuest(guest: any): string {
    return guest?.fullName || '';
  }

  /**
   * Maneja cambios en los addons
   */
  onAddonsChange(addons: BookingAddon[]): void {
    this.selectedAddons = addons;
  }

  /**
   * Calcula el número de noches
   */
  calculateNights(): number {
    const checkIn = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;
    
    if (!checkIn || !checkOut) return 0;
    
    const checkInDate = new Date(checkIn);
    const checkOutDate = new Date(checkOut);
    const diffTime = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  /**
   * Calcula el total del hospedaje
   */
  calculateAccommodationTotal(): number {
    if (!this.selectedRoom?.pricePerNight) return 0;
    const nights = this.calculateNights();
    return nights * this.selectedRoom.pricePerNight;
  }

  /**
   * Calcula el total de los addons
   */
  calculateAddonsTotal(): number {
    return this.selectedAddons.reduce((sum, addon) => 
      sum + (addon.subtotal || 0), 0
    );
  }

  /**
   * Calcula el total de la reserva
   */
  calculateTotal(): number {
    const accommodationTotal = this.calculateAccommodationTotal();
    const addonsTotal = this.calculateAddonsTotal();
    return accommodationTotal + addonsTotal;
  }

  /**
   * Muestra modal de confirmación y guarda
   */
  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.markFormGroupTouched(this.bookingForm);
      this.showError('Por favor, complete todos los campos requeridos');
      return;
    }

    if (!this.isRoomAvailable) {
      this.showError('La habitación no está disponible en las fechas seleccionadas');
      return;
    }

    // Prepare summary and show confirmation modal inside the dialog
    this.summaryGuestName = typeof this.bookingForm.get('guestSearch')?.value === 'object' 
      ? this.bookingForm.get('guestSearch')?.value?.fullName 
      : this.bookingForm.get('guestSearch')?.value;

    // Validate addon quantities before showing summary
    const invalidAddon = this.selectedAddons.find(a => !a.quantity || a.quantity < 1);
    if (invalidAddon) {
      this.showError('La cantidad de los servicios adicionales debe ser al menos 1');
      return;
    }

    this.showSummaryModal = true;
  }

  /** Confirma el resumen y cierra el diálogo retornando los datos */
  confirmSummary(): void {
    // Build payload and close dialog (same as original confirmed path)
    const bookingData: BookingRequest = {
      guestId: this.bookingForm.get('guestId')?.value,
      roomId: this.bookingForm.get('roomId')?.value,
      checkInDate: this.formatDate(this.bookingForm.get('checkInDate')?.value),
      checkOutDate: this.formatDate(this.bookingForm.get('checkOutDate')?.value),
      status: this.bookingForm.get('status')?.value,
      createdBy: this.currentUser?.email || 'system',
      bookingLeadTime: this.formatDate(this.bookingForm.get('bookingLeadTime')?.value),
      notes: this.bookingForm.get('notes')?.value
    };

    const addonRequests = this.selectedAddons.map(a => ({
      addonId: a.addonId,
      quantity: a.quantity ?? 1
    }));

    (bookingData as any).addons = addonRequests;

    this.showSummaryModal = false;
    this.dialogRef.close({ booking: bookingData, addons: addonRequests });
  }

  cancelSummary(): void {
    this.showSummaryModal = false;
  }

  /**
   * Cierra el modal
   */
  close(): void {
    this.dialogRef.close();
  }

  /**
   * Formatea una fecha a YYYY-MM-DD
   */
  formatDate(date: any): string {
    if (!date) return '';
    const d = new Date(date);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  /**
   * Marca todos los campos del formulario como touched
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
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

  /**
   * Obtiene el mensaje de error para un campo
   */
  getErrorMessage(fieldName: string): string {
    const control = this.bookingForm.get(fieldName);
    
    if (control?.hasError('required')) {
      return 'Este campo es requerido';
    }
    
    if (control?.hasError('min')) {
      return `El valor mínimo es ${control.errors?.['min'].min}`;
    }
    
    return '';
  }
}
