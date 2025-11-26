import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { RoomDashboardSummary } from '../../models';

@Component({
  selector: 'app-room-grid',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './room-grid.component.html'
})
export class RoomGridComponent {
  @Input() rooms: RoomDashboardSummary[] = [];
  @Input() isLoading = false;
  @Output() createBooking = new EventEmitter<RoomDashboardSummary>();

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
