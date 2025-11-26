import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RoomDashboardSummary } from '../../models';

export interface DateFilterEvent {
  checkIn: string;
  checkOut: string;
}

@Component({
  selector: 'app-room-grid',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './room-grid.component.html',
  styleUrls: ['./room-grid.component.scss']
})
export class RoomGridComponent {
  @Input() rooms: RoomDashboardSummary[] = [];
  @Input() isLoading = false;
  @Input() isFiltered = false;
  @Output() createBooking = new EventEmitter<RoomDashboardSummary>();
  @Output() filterByDates = new EventEmitter<DateFilterEvent>();
  @Output() clearFilter = new EventEmitter<void>();

  // Date filter fields (Date objects for mat-datepicker)
  checkInDate: Date | null = null;
  checkOutDate: Date | null = null;

  // Get today's date for min attribute
  get minDate(): Date {
    return new Date();
  }

  /**
   * Apply date filter
   */
  applyFilter(): void {
    if (this.checkInDate && this.checkOutDate) {
      this.filterByDates.emit({
        checkIn: this.formatDate(this.checkInDate),
        checkOut: this.formatDate(this.checkOutDate)
      });
    }
  }

  /**
   * Format Date to YYYY-MM-DD string
   */
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  /**
   * Clear date filter and show all rooms
   */
  onClearFilter(): void {
    this.checkInDate = null;
    this.checkOutDate = null;
    this.clearFilter.emit();
  }

  constructor(private router: Router) {}

  /**
   * Navigate to booking details page
   */
  viewBooking(bookingId: number): void {
    this.router.navigate(['/bookings', bookingId]);
  }

  /**
   * Emit event to open booking creation modal
   */
  onCreateBooking(room: RoomDashboardSummary): void {
    this.createBooking.emit(room);
  }

  /**
   * Get status badge classes based on room status
   */
  getStatusClasses(status: string): string {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'OCCUPIED':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400';
      case 'MAINTENANCE':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
    }
  }

  /**
   * Get status label in Spanish
   */
  getStatusLabel(status: string): string {
    switch (status) {
      case 'AVAILABLE':
        return 'Disponible';
      case 'OCCUPIED':
        return 'Ocupada';
      case 'MAINTENANCE':
        return 'Mantenimiento';
      default:
        return status;
    }
  }

  /**
   * Get card border color based on status
   */
  getCardBorderClass(status: string): string {
    switch (status) {
      case 'AVAILABLE':
        return 'border-l-4 border-l-green-500';
      case 'OCCUPIED':
        return 'border-l-4 border-l-blue-500';
      case 'MAINTENANCE':
        return 'border-l-4 border-l-red-500';
      default:
        return '';
    }
  }
}
