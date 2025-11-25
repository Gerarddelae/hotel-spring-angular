import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BookingListComponent } from './booking-list.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';

describe('BookingListComponent', () => {
  let component: BookingListComponent;
  let fixture: ComponentFixture<BookingListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BookingListComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load bookings on init', () => {
    expect(component.bookings).toBeDefined();
    expect(component.filteredBookings).toBeDefined();
  });

  it('should apply filters correctly', () => {
    component.bookings = [
      {
        id: 1,
        guestId: 1,
        guestName: 'John Doe',
        roomId: 1,
        roomNumber: '101',
        checkInDate: '2025-06-01',
        checkOutDate: '2025-06-05',
        status: 'CONFIRMED',
        createdBy: 'test',
        bookingLeadTime: '2025-05-01',
        hotelId: 1
      }
    ];

    component.filterForm.patchValue({ status: 'CONFIRMED' });
    component.applyFilters();

    expect(component.filteredBookings.length).toBe(1);
  });
});
