import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BookingService } from './booking.service';
import { Booking, BookingRequest } from '../models/booking.interface';

describe('BookingService', () => {
  let service: BookingService;
  let httpMock: HttpTestingController;
  const API_URL = 'http://localhost:8080/api/bookings';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BookingService]
    });
    service = TestBed.inject(BookingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all bookings', () => {
    const mockBookings: Booking[] = [
      {
        id: 1,
        guestId: 1,
        roomId: 1,
        checkInDate: '2025-06-01',
        checkOutDate: '2025-06-05',
        status: 'CONFIRMED',
        createdBy: 'test@test.com',
        bookingLeadTime: '2025-05-01',
        hotelId: 1
      }
    ];

    service.getAll().subscribe(bookings => {
      expect(bookings).toEqual(mockBookings);
      expect(bookings.length).toBe(1);
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush(mockBookings);
  });

  it('should create a booking', () => {
    const newBooking: BookingRequest = {
      guestId: 1,
      roomId: 1,
      checkInDate: '2025-06-01',
      checkOutDate: '2025-06-05',
      status: 'PENDING',
      createdBy: 'test@test.com',
      bookingLeadTime: '2025-05-01'
    };

    service.create(newBooking).subscribe(booking => {
      expect(booking).toBeTruthy();
      expect(booking.id).toBeDefined();
    });

    const req = httpMock.expectOne(API_URL);
    expect(req.request.method).toBe('POST');
    req.flush({ ...newBooking, id: 1, hotelId: 1 });
  });

  it('should validate dates correctly', () => {
    const today = new Date().toISOString().split('T')[0];
    const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
    const nextWeek = new Date(Date.now() + 604800000).toISOString().split('T')[0];

    expect(service.validateDates(tomorrow, nextWeek)).toBeTruthy();
    expect(service.validateDates(nextWeek, tomorrow)).toBeFalsy();
  });

  it('should calculate total with addons', () => {
    const booking: Booking = {
      id: 1,
      guestId: 1,
      roomId: 1,
      checkInDate: '2025-06-01',
      checkOutDate: '2025-06-05',
      status: 'CONFIRMED',
      createdBy: 'test@test.com',
      bookingLeadTime: '2025-05-01',
      hotelId: 1,
      totalAmount: 500,
      addons: [
        { addonId: 1, price: 50, quantity: 2 },
        { addonId: 2, price: 30, quantity: 1 }
      ]
    };

    const total = service.calculateTotal(booking);
    expect(total).toBe(630); // 500 + (50*2) + (30*1)
  });
});
