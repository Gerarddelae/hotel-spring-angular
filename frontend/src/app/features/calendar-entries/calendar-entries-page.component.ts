import { Component, OnInit, inject, signal, computed, LOCALE_ID } from '@angular/core';
import { CommonModule, registerLocaleData } from '@angular/common';
import { Router } from '@angular/router';
import {
  CalendarEvent,
  CalendarModule,
  CalendarView,
} from 'angular-calendar';
import { Subject } from 'rxjs';
import {
  startOfMonth,
  endOfMonth,
  format,
  parseISO,
  addMonths,
  subMonths,
  isSameMonth,
  isSameDay,
} from 'date-fns';
import { es } from 'date-fns/locale';
import localeEs from '@angular/common/locales/es';

// Register Spanish locale for Angular pipes
registerLocaleData(localeEs);

import { CalendarEntriesService } from './calendar-entries.service';
import { CalendarEntry } from './models/calendar-entry.interface';

// Color definitions for events - soft and professional palette
const colors = {
  checkIn: {
    primary: '#10b981',    // emerald-500 - softer green
    secondary: '#d1fae5',  // emerald-100
  },
  checkOut: {
    primary: '#f59e0b',    // amber-500 - warm orange instead of harsh red
    secondary: '#fef3c7',  // amber-100
  },
};

@Component({
  selector: 'app-calendar-entries-page',
  standalone: true,
  imports: [CommonModule, CalendarModule],
  providers: [{ provide: LOCALE_ID, useValue: 'es-ES' }],
  templateUrl: './calendar-entries-page.component.html',
  styleUrl: './calendar-entries-page.component.scss',
})
export class CalendarEntriesPageComponent implements OnInit {
  private readonly calendarEntriesService = inject(CalendarEntriesService);
  private readonly router = inject(Router);

  // Calendar configuration
  view: CalendarView = CalendarView.Month;
  CalendarView = CalendarView;
  viewDate = signal<Date>(new Date());
  refresh = new Subject<void>();

  // Events and state
  events = signal<CalendarEvent[]>([]);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  activeDayIsOpen = signal<boolean>(false);

  // Computed properties
  currentMonthLabel = computed(() => {
    const date = this.viewDate();
    return format(date, 'MMMM yyyy', { locale: es });
  });

  ngOnInit(): void {
    this.loadEntries();
  }

  /**
   * Navigate to previous month
   */
  previousMonth(): void {
    this.viewDate.set(subMonths(this.viewDate(), 1));
    this.activeDayIsOpen.set(false);
    this.loadEntries();
  }

  /**
   * Navigate to next month
   */
  nextMonth(): void {
    this.viewDate.set(addMonths(this.viewDate(), 1));
    this.activeDayIsOpen.set(false);
    this.loadEntries();
  }

  /**
   * Navigate to today
   */
  goToToday(): void {
    this.viewDate.set(new Date());
    this.loadEntries();
  }

  /**
   * Load calendar entries from the backend
   */
  loadEntries(): void {
    this.loading.set(true);
    this.error.set(null);

    const currentDate = this.viewDate();
    const start = format(startOfMonth(currentDate), 'yyyy-MM-dd');
    const end = format(endOfMonth(currentDate), 'yyyy-MM-dd');

    this.calendarEntriesService.getEntries(start, end).subscribe({
      next: (entries) => {
        this.events.set(this.mapEntriesToEvents(entries));
        this.loading.set(false);
        this.refresh.next();
      },
      error: (err) => {
        console.error('Error loading calendar entries:', err);
        this.error.set('Error al cargar las entradas del calendario');
        this.loading.set(false);
      },
    });
  }

  /**
   * Maps backend entries to calendar events
   * Creates two events per entry: one for check-in (green) and one for check-out (red)
   */
  private mapEntriesToEvents(entries: CalendarEntry[]): CalendarEvent[] {
    const events: CalendarEvent[] = [];

    entries.forEach((entry) => {
      // Check-in event (green)
      events.push({
        id: `checkin-${entry.bookingId}`,
        start: parseISO(entry.checkInDate),
        title: `✓ ${entry.guestName} - ${entry.roomNumber}`,
        color: colors.checkIn,
        meta: {
          type: 'checkin',
          bookingId: entry.bookingId,
          guestName: entry.guestName,
          roomNumber: entry.roomNumber,
        },
      });

      // Check-out event (red)
      events.push({
        id: `checkout-${entry.bookingId}`,
        start: parseISO(entry.checkOutDate),
        title: `✗ ${entry.guestName} - ${entry.roomNumber}`,
        color: colors.checkOut,
        meta: {
          type: 'checkout',
          bookingId: entry.bookingId,
          guestName: entry.guestName,
          roomNumber: entry.roomNumber,
        },
      });
    });

    return events;
  }

  /**
   * Handle event click - navigate to booking details
   */
  onEventClicked(event: CalendarEvent): void {
    if (event.meta?.bookingId) {
      this.router.navigate(['/bookings', event.meta.bookingId]);
    }
  }

  /**
   * Handle day click - toggle open day with events (angular-calendar pattern)
   */
  onDayClicked({ day, sourceEvent }: { day: any; sourceEvent: MouseEvent | KeyboardEvent }): void {
    // Only handle clicks in the current month
    if (isSameMonth(day.date, this.viewDate())) {
      // If clicking the same day that's already open, close it
      if (
        (isSameDay(this.viewDate(), day.date) && this.activeDayIsOpen()) ||
        day.events.length === 0
      ) {
        this.activeDayIsOpen.set(false);
      } else {
        // Open the day if it has events
        this.activeDayIsOpen.set(true);
      }
      // Update viewDate to the clicked day
      this.viewDate.set(day.date);
    }
  }

  /**
   * Get tooltip text for an event
   */
  getEventTooltip(event: CalendarEvent): string {
    if (event.meta) {
      return `Huésped: ${event.meta.guestName}\nHabitación: ${event.meta.roomNumber}`;
    }
    return event.title;
  }

  /**
   * Track events by id for ngFor optimization
   */
  trackByEventId(index: number, event: CalendarEvent): string | number | undefined {
    return event.id;
  }
}
