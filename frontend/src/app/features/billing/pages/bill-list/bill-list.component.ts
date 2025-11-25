import { Component, OnInit, ViewChild, AfterViewInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { BillService } from '../../services/bill.service';
import { Bill, BillStatus, BILL_STATUS_OPTIONS } from '../../models';

@Component({
  selector: 'app-bill-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule
  ],
  templateUrl: './bill-list.component.html',
  styleUrls: ['./bill-list.component.scss']
})
export class BillListComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = [
    'id',
    'bookingId',
    'guestName',
    'status',
    'totalAmount',
    'createdAt',
    'actions'
  ];

  bills = signal<Bill[]>([]);
  isLoading = signal(false);
  searchQuery = signal('');
  statusFilter = signal<BillStatus | ''>('');
  dateFrom = signal<Date | null>(null);
  dateTo = signal<Date | null>(null);

  statusOptions = BILL_STATUS_OPTIONS;

  dataSource = new MatTableDataSource<Bill>([]);
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  filteredBills = computed(() => {
    let result = this.bills();
    const query = this.searchQuery().toLowerCase();
    const status = this.statusFilter();
    const fromDate = this.dateFrom();
    const toDate = this.dateTo();

    if (query) {
      result = result.filter(bill => {
        const guestName = bill.guestName || bill.booking?.guestName || '';
        return bill.id.toString().includes(query) ||
          bill.bookingId.toString().includes(query) ||
          guestName.toLowerCase().includes(query);
      });
    }

    if (status) {
      result = result.filter(bill => bill.status === status);
    }

    if (fromDate) {
      result = result.filter(bill => {
        const billDate = new Date(bill.createdAt);
        return billDate >= fromDate;
      });
    }

    if (toDate) {
      const endOfDay = new Date(toDate);
      endOfDay.setHours(23, 59, 59, 999);
      result = result.filter(bill => {
        const billDate = new Date(bill.createdAt);
        return billDate <= endOfDay;
      });
    }

    return result;
  });

  constructor(
    private billService: BillService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    // Sync dataSource with filteredBills signal
    effect(() => {
      this.dataSource.data = this.filteredBills();
    });
  }

  ngOnInit(): void {
    this.loadBills();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
  }

  loadBills(): void {
    this.isLoading.set(true);
    this.billService.getAll().subscribe({
      next: (bills) => {
        this.bills.set(bills || []);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.showError('Error al cargar las facturas');
        this.isLoading.set(false);
      }
    });
  }

  viewDetails(bill: Bill): void {
    this.router.navigate(['/bills', bill.id]);
  }

  getStatusLabel(status: BillStatus): string {
    const option = BILL_STATUS_OPTIONS.find(s => s.value === status);
    return option?.label || status;
  }

  getStatusColor(status: BillStatus): string {
    const colors: Record<BillStatus, string> = {
      'PAID': 'primary',
      'UNPAID': 'accent',
      'CANCELED': 'warn'
    };
    return colors[status] || '';
  }

  getGuestName(bill: Bill): string {
    return bill.guestName || bill.booking?.guestName || 'N/A';
  }

  /**
   * Calcula el total real de la factura (hospedaje + addons)
   * El backend puede enviar solo el total de addons en totalAmount
   */
  calculateGrandTotal(bill: Bill): number {
    // Calcular subtotal de hospedaje
    let accommodationSubtotal = 0;
    const billAny = bill as any;
    const bookingAny = bill.booking as any;
    const pricePerNight = billAny.roomPricePerNight || bookingAny?.roomPricePerNight || 0;
    
    if (billAny.accommodationSubtotal) {
      accommodationSubtotal = billAny.accommodationSubtotal;
    } else if (pricePerNight) {
      const checkIn = billAny.checkInDate || bill.booking?.checkInDate;
      const checkOut = billAny.checkOutDate || bill.booking?.checkOutDate;
      if (checkIn && checkOut) {
        const nights = Math.ceil(
          (new Date(checkOut).getTime() - new Date(checkIn).getTime()) / (1000 * 60 * 60 * 24)
        );
        accommodationSubtotal = pricePerNight * Math.max(nights, 1);
      }
    }

    // Calcular subtotal de addons
    let addonsTotal = 0;
    if (bill.addons && bill.addons.length > 0) {
      addonsTotal = bill.addons.reduce((sum, addon) => {
        const addonAny = addon as any;
        const price = addonAny.totalPrice || addonAny.subtotal || 
                     (addonAny.unitPrice || addonAny.price || 0) * (addonAny.quantity || 1);
        return sum + price;
      }, 0);
    }

    // Si el backend totalAmount es menor que la suma, usar la suma calculada
    const calculatedTotal = accommodationSubtotal + addonsTotal;
    const backendTotal = bill.totalAmount || 0;
    
    return Math.max(backendTotal, calculatedTotal);
  }

  onSearchChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchQuery.set(value);
  }

  onStatusFilterChange(value: BillStatus | ''): void {
    this.statusFilter.set(value);
  }

  onDateFromChange(event: any): void {
    this.dateFrom.set(event.value);
  }

  onDateToChange(event: any): void {
    this.dateTo.set(event.value);
  }

  clearFilters(): void {
    this.searchQuery.set('');
    this.statusFilter.set('');
    this.dateFrom.set(null);
    this.dateTo.set(null);
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
