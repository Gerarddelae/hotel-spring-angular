import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CalendarEntriesService } from './calendar-entries.service';
import { CalendarEntry } from './models/calendar-entry.interface';

describe('CalendarEntriesService', () => {
  let service: CalendarEntriesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CalendarEntriesService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(CalendarEntriesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch calendar entries with correct params', () => {
    const mockEntries: CalendarEntry[] = [
      {
        bookingId: 1,
        guestName: 'John Doe',
        roomNumber: '101',
        checkInDate: '2024-01-15',
        checkOutDate: '2024-01-18'
      },
      {
        bookingId: 2,
        guestName: 'Jane Smith',
        roomNumber: '203',
        checkInDate: '2024-01-16',
        checkOutDate: '2024-01-20'
      }
    ];

    const start = '2024-01-01';
    const end = '2024-01-31';

    service.getEntries(start, end).subscribe(entries => {
      expect(entries).toEqual(mockEntries);
      expect(entries.length).toBe(2);
    });

    const req = httpMock.expectOne(
      `http://localhost:8080/api/calendar/entries?start=${start}&end=${end}`
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockEntries);
  });
});
