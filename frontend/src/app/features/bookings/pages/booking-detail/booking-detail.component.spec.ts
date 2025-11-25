import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BookingDetailComponent } from './booking-detail.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('BookingDetailComponent', () => {
  let component: BookingDetailComponent;
  let fixture: ComponentFixture<BookingDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BookingDetailComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: 1 })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookingDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate nights correctly', () => {
    component.booking = {
      id: 1,
      guestId: 1,
      roomId: 1,
      checkInDate: '2025-06-01',
      checkOutDate: '2025-06-05',
      status: 'CONFIRMED',
      createdBy: 'test',
      bookingLeadTime: '2025-05-01',
      hotelId: 1
    };

    expect(component.calculateNights()).toBe(4);
  });
});
