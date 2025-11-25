import { Component, OnInit, signal, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BillService } from '../../services/bill.service';
import { Bill, BillStatus, BILL_STATUS_OPTIONS, PAYMENT_METHOD_OPTIONS } from '../../models';

@Component({
  selector: 'app-bill-detail',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatListModule,
    MatSelectModule,
    MatFormFieldModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './bill-detail.component.html',
  styleUrls: ['./bill-detail.component.scss']
})
export class BillDetailComponent implements OnInit {
  bill = signal<Bill | null>(null);
  isLoading = signal(false);
  billId = 0;

  statusOptions = BILL_STATUS_OPTIONS;
  paymentMethodOptions = PAYMENT_METHOD_OPTIONS;

  @ViewChild('deleteDialog') deleteDialogTpl!: TemplateRef<any>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private billService: BillService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.billId = +params['id'];
      if (this.billId) {
        this.loadBillDetail();
      }
    });
  }

  loadBillDetail(): void {
    this.isLoading.set(true);
    this.billService.getBill(this.billId).subscribe({
      next: (bill) => {
        this.bill.set(bill);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.showError('Error al cargar los detalles de la factura');
        this.isLoading.set(false);
        this.router.navigate(['/bills']);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/bills']);
  }

  goToBooking(): void {
    const billData = this.bill();
    if (billData?.bookingId) {
      this.router.navigate(['/bookings', billData.bookingId]);
    }
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

  getPaymentMethodLabel(method: string): string {
    const option = PAYMENT_METHOD_OPTIONS.find(m => m.value === method);
    return option?.label || method || 'No especificado';
  }

  onStatusChange(status: BillStatus): void {
    this.isLoading.set(true);
    this.billService.updateStatus(this.billId, status).subscribe({
      next: (updatedBill) => {
        this.bill.set(updatedBill);
        this.showSuccess('Estado actualizado correctamente');
        this.isLoading.set(false);
      },
      error: (error) => {
        this.showError(error.message || 'Error al actualizar el estado');
        this.isLoading.set(false);
      }
    });
  }

  onPaymentMethodChange(paymentMethod: string): void {
    this.isLoading.set(true);
    this.billService.updatePaymentMethod(this.billId, paymentMethod as any).subscribe({
      next: (updatedBill) => {
        this.bill.set(updatedBill);
        this.showSuccess('Método de pago actualizado correctamente');
        this.isLoading.set(false);
      },
      error: (error) => {
        this.showError(error.message || 'Error al actualizar el método de pago');
        this.isLoading.set(false);
      }
    });
  }

  deleteBill(): void {
    this.dialog.open(this.deleteDialogTpl, { width: '480px' });
  }

  closeDialog(): void {
    this.dialog.closeAll();
  }

  confirmDelete(): void {
    this.dialog.closeAll();
    this.isLoading.set(true);
    this.billService.delete(this.billId).subscribe({
      next: () => {
        this.showSuccess('Factura eliminada exitosamente');
        this.router.navigate(['/bills']);
      },
      error: (error) => {
        this.showError(error.message || 'Error al eliminar la factura');
        this.isLoading.set(false);
      }
    });
  }

  // Métodos para obtener datos del huésped (pueden venir directos o del booking embebido)
  getGuestName(): string {
    const billData = this.bill();
    return billData?.guestName || billData?.booking?.guestName || 'N/A';
  }

  getGuestId(): number | null {
    const billData = this.bill();
    return billData?.guestId || billData?.booking?.guestId || null;
  }

  getRoomNumber(): string {
    const billData = this.bill();
    return billData?.roomNumber || billData?.booking?.roomNumber || 'N/A';
  }

  getRoomId(): number | null {
    const billData = this.bill();
    return billData?.roomId || billData?.booking?.roomId || null;
  }

  getCheckInDate(): string | null {
    const billData = this.bill();
    return billData?.checkInDate || billData?.booking?.checkInDate || null;
  }

  getCheckOutDate(): string | null {
    const billData = this.bill();
    return billData?.checkOutDate || billData?.booking?.checkOutDate || null;
  }

  // Métodos helper para addons con diferentes estructuras
  getAddonName(addon: any): string {
    return addon.addonName || addon.name || addon.addon?.name || 'Servicio';
  }

  getAddonPrice(addon: any): number {
    return addon.unitPrice ?? addon.price ?? addon.addon?.price ?? 0;
  }

  getAddonQuantity(addon: any): number {
    return addon.quantity ?? 1;
  }

  getAddonSubtotal(addon: any): number {
    if (addon.totalPrice !== undefined && addon.totalPrice !== null) {
      return addon.totalPrice;
    }
    if (addon.subtotal !== undefined && addon.subtotal !== null) {
      return addon.subtotal;
    }
    if (addon.total !== undefined && addon.total !== null) {
      return addon.total;
    }
    return this.getAddonPrice(addon) * this.getAddonQuantity(addon);
  }

  calculateAddonsTotal(): number {
    const billData = this.bill();
    if (!billData?.addons) return 0;
    return billData.addons.reduce((sum, addon) => sum + this.getAddonSubtotal(addon), 0);
  }

  calculateNights(): number {
    const checkIn = this.getCheckInDate();
    const checkOut = this.getCheckOutDate();
    if (!checkIn || !checkOut) return 0;
    
    const checkInDate = new Date(checkIn);
    const checkOutDate = new Date(checkOut);
    const diffTime = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  /**
   * Calcula el total real sumando hospedaje + addons
   * Usa el totalAmount del backend si incluye todo, sino calcula manualmente
   */
  calculateGrandTotal(): number {
    const billData = this.bill();
    const accommodationSubtotal = this.calculateAccommodationSubtotal();
    const addonsTotal = this.calculateAddonsTotal();
    
    // Si el backend ya devuelve el total correcto (hospedaje + addons), usarlo
    const backendTotal = billData?.totalAmount || 0;
    const calculatedTotal = accommodationSubtotal + addonsTotal;
    
    // Usar el mayor de los dos (para manejar casos donde backend no incluye hospedaje)
    return Math.max(backendTotal, calculatedTotal);
  }

  calculateAccommodationSubtotal(): number {
    const billData = this.bill();
    // Si viene directo del backend
    if (billData?.accommodationSubtotal !== undefined && billData.accommodationSubtotal !== null) {
      return billData.accommodationSubtotal;
    }
    // Intentar calcular con noches y precio por noche
    if (billData?.roomPricePerNight) {
      const nights = this.calculateNights();
      return nights * billData.roomPricePerNight;
    }
    // Derivar del total menos addons
    const totalAmount = billData?.totalAmount || 0;
    const addonsTotal = this.calculateAddonsTotal();
    return totalAmount - addonsTotal;
  }

  getRoomPricePerNight(): number | null {
    const billData = this.bill();
    if (billData?.roomPricePerNight) {
      return billData.roomPricePerNight;
    }
    // Derivar del subtotal de hospedaje dividido por noches
    const nights = this.calculateNights();
    if (nights > 0) {
      const accSubtotal = this.calculateAccommodationSubtotalDirect();
      if (accSubtotal > 0) {
        return Math.round((accSubtotal / nights) * 100) / 100;
      }
    }
    return null;
  }

  // Método auxiliar para evitar recursión infinita
  private calculateAccommodationSubtotalDirect(): number {
    const billData = this.bill();
    if (billData?.accommodationSubtotal !== undefined && billData.accommodationSubtotal !== null) {
      return billData.accommodationSubtotal;
    }
    // Derivar del total menos addons
    const totalAmount = billData?.totalAmount || 0;
    const addonsTotal = billData?.addons?.reduce((sum, addon) => sum + (addon.subtotal || 0), 0) || 0;
    return totalAmount - addonsTotal;
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
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
