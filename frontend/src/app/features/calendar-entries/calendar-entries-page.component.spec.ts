import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { CalendarEntriesPageComponent } from './calendar-entries-page.component';
import { CalendarEntriesService } from './calendar-entries.service';
import { of } from 'rxjs';

describe('CalendarEntriesPageComponent', () => {
  let component: CalendarEntriesPageComponent;
  let fixture: ComponentFixture<CalendarEntriesPageComponent>;
  let calendarEntriesService: jasmine.SpyObj<CalendarEntriesService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CalendarEntriesService', ['getEntries']);
    spy.getEntries.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [CalendarEntriesPageComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: CalendarEntriesService, useValue: spy }
      ]
    }).compileComponents();

    calendarEntriesService = TestBed.inject(CalendarEntriesService) as jasmine.SpyObj<CalendarEntriesService>;
    fixture = TestBed.createComponent(CalendarEntriesPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load entries on init', () => {
    expect(calendarEntriesService.getEntries).toHaveBeenCalled();
  });

  it('should navigate months correctly', () => {
    const initialDate = new Date(component.viewDate());
    
    component.nextMonth();
    expect(component.viewDate().getMonth()).toBe((initialDate.getMonth() + 1) % 12);

    component.previousMonth();
    expect(component.viewDate().getMonth()).toBe(initialDate.getMonth());
  });

  it('should map entries to events correctly', () => {
    const mockEntries = [
      {
        bookingId: 1,
        guestName: 'John Doe',
        roomNumber: '101',
        checkInDate: '2024-01-15',
        checkOutDate: '2024-01-18'
      }
    ];

    calendarEntriesService.getEntries.and.returnValue(of(mockEntries));
    component.loadEntries();

    expect(component.events().length).toBe(2); // One check-in, one check-out
    expect(component.events()[0].title).toContain('John Doe');
    expect(component.events()[0].title).toContain('101');
  });
});
