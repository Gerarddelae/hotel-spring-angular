import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin, of, catchError } from 'rxjs';
import { KpiCardsComponent } from './components/kpi-cards/kpi-cards.component';
import { RoomGridComponent } from './components/room-grid/room-grid.component';
import { BookingService } from '../bookings/services/booking.service';
import { RoomService } from '../rooms/rooms.service';
import { BillService } from '../billing/services/bill.service';
import { BookingModalFormComponent } from '../../shared/components/booking-modal-form/booking-modal-form.component';
import { 
  KpiData, 
  RoomDashboardSummary,
  BookingStatusCountResponse,
  ActiveGuestsCountResponse,
  OccupiedRoomsCountResponse,
  RevenueResponse
} from './models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatSnackBarModule,
    KpiCardsComponent,
    RoomGridComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  kpis: KpiData[] = [];
  rooms: RoomDashboardSummary[] = [];
  isLoadingKpis = true;
  isLoadingRooms = true;

  constructor(
    private bookingService: BookingService,
    private roomService: RoomService,
    private billService: BillService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadKpis();
    this.loadRooms();
  }

  /**
   * Load all KPI data in parallel using forkJoin
   * Each observable has its own catchError to prevent one failure from blocking all KPIs
   */
  loadKpis(): void {
    this.isLoadingKpis = true;

    const defaultBookingStatus: BookingStatusCountResponse = { total: 0, pending: 0, confirmed: 0, checkedIn: 0 };
    const defaultActiveGuests: ActiveGuestsCountResponse = { count: 0 };
    const defaultOccupiedRooms: OccupiedRoomsCountResponse = { count: 0 };
    const defaultRevenue: RevenueResponse = { total: 0, currency: 'USD' };

    forkJoin({
      bookingStatus: this.bookingService.getBookingStatusCount().pipe(
        catchError(err => {
          console.error('Error loading booking status:', err);
          return of(defaultBookingStatus);
        })
      ),
      activeGuests: this.bookingService.getActiveGuestsCount().pipe(
        catchError(err => {
          console.error('Error loading active guests:', err);
          return of(defaultActiveGuests);
        })
      ),
      occupiedRooms: this.roomService.getOccupiedCount().pipe(
        catchError(err => {
          console.error('Error loading occupied rooms:', err);
          return of(defaultOccupiedRooms);
        })
      ),
      revenue: this.billService.getMonthlyRevenue().pipe(
        catchError(err => {
          console.error('Error loading revenue:', err);
          return of(defaultRevenue);
        })
      )
    }).subscribe({
      next: (data) => {
        this.kpis = this.mapToKpiData(
          data.bookingStatus,
          data.activeGuests,
          data.occupiedRooms,
          data.revenue
        );
        this.isLoadingKpis = false;
      },
      error: (err) => {
        console.error('Error loading KPIs:', err);
        this.snackBar.open('Error al cargar los indicadores', 'Cerrar', { duration: 3000 });
        this.isLoadingKpis = false;
      }
    });
  }

  /**
   * Load room dashboard summary
   */
  loadRooms(): void {
    this.isLoadingRooms = true;

    this.roomService.getDashboardSummary().subscribe({
      next: (rooms) => {
        this.rooms = rooms;
        this.isLoadingRooms = false;
      },
      error: (err) => {
        console.error('Error loading rooms:', err);
        this.snackBar.open('Error al cargar las habitaciones', 'Cerrar', { duration: 3000 });
        this.isLoadingRooms = false;
      }
    });
  }

  /**
   * Map API responses to KPI data array
   */
  private mapToKpiData(
    bookingStatus: BookingStatusCountResponse,
    activeGuests: ActiveGuestsCountResponse,
    occupiedRooms: OccupiedRoomsCountResponse,
    revenue: RevenueResponse
  ): KpiData[] {
    return [
      {
        label: 'Reservas Pendientes',
        value: bookingStatus.pending,
        icon: 'pi pi-clock'
      },
      {
        label: 'Reservas Confirmadas',
        value: bookingStatus.confirmed,
        icon: 'pi pi-check-circle'
      },
      {
        label: 'Check-In Hoy',
        value: bookingStatus.checkedIn,
        icon: 'pi pi-sign-in'
      },
      {
        label: 'Total Reservas',
        value: bookingStatus.total,
        icon: 'pi pi-calendar'
      },
      {
        label: 'Habitaciones Ocupadas',
        value: occupiedRooms.count,
        icon: 'pi pi-home'
      },
      {
        label: 'Huéspedes Activos',
        value: activeGuests.count,
        icon: 'pi pi-users'
      },
      {
        label: 'Ingresos del Mes',
        value: this.formatCurrency(revenue.total, revenue.currency),
        icon: 'pi pi-dollar'
      }
    ];
  }

  /**
   * Format currency value
   */
  private formatCurrency(value: number, currency: string): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: currency || 'USD'
    }).format(value);
  }

  /**
   * Open booking creation modal for a specific room
   */
  openBookingModal(room: RoomDashboardSummary): void {
    const dialogRef = this.dialog.open(BookingModalFormComponent, {
      width: '800px',
      maxWidth: '95vw',
      maxHeight: '90vh',
      disableClose: true,
      autoFocus: 'first-tabbable',
      panelClass: 'booking-modal-panel',
      data: {
        preselectedRoomId: room.roomId
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.booking) {
        this.createBookingFromModal(result);
      }
    });
  }

  /**
   * Create booking from modal result and refresh dashboard
   */
  private createBookingFromModal(data: any): void {
    const payload = data.booking;
    
    this.bookingService.create(payload).subscribe({
      next: (booking) => {
        // If there are addons, add them to the booking
        if (data.addons && data.addons.length > 0) {
          this.bookingService.replaceBookingAddons(booking.id, data.addons).subscribe({
            next: () => {
              this.snackBar.open('Reserva creada exitosamente', 'Cerrar', { duration: 3000 });
              this.loadKpis();
              this.loadRooms();
            },
            error: () => {
              this.snackBar.open('Reserva creada, pero hubo un error con los servicios adicionales', 'Cerrar', { duration: 3000 });
              this.loadKpis();
              this.loadRooms();
            }
          });
        } else {
          this.snackBar.open('Reserva creada exitosamente', 'Cerrar', { duration: 3000 });
          this.loadKpis();
          this.loadRooms();
        }
      },
      error: (err) => {
        console.error('Error creating booking:', err);
        this.snackBar.open('Error al crear la reserva: ' + (err.message || 'Error desconocido'), 'Cerrar', { duration: 5000 });
      }
    });
  }
}
