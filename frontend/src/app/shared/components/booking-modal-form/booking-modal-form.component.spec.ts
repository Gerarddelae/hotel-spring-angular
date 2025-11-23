import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BookingModalFormComponent } from './booking-modal-form.component';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('BookingModalFormComponent', () => {
  let component: BookingModalFormComponent;
  let fixture: ComponentFixture<BookingModalFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BookingModalFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookingModalFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with validators', () => {
    expect(component.bookingForm).toBeDefined();
    expect(component.bookingForm.get('guestId')).toBeTruthy();
    expect(component.bookingForm.get('roomId')).toBeTruthy();
    expect(component.bookingForm.get('checkInDate')).toBeTruthy();
    expect(component.bookingForm.get('checkOutDate')).toBeTruthy();
  });

  it('should invalidate form when dates are incorrect', () => {
    component.bookingForm.patchValue({
      checkInDate: '2025-06-05',
      checkOutDate: '2025-06-01'
    });

    expect(component.bookingForm.hasError('dateRange')).toBeTruthy();
  });

  it('should calculate total with addons', () => {
    component.selectedAddons = [
      { addonId: 1, price: 50, quantity: 2, subtotal: 100 },
      { addonId: 2, price: 30, quantity: 1, subtotal: 30 }
    ];

    const total = component.calculateTotal();
    expect(total).toBeGreaterThanOrEqual(130);
  });
});
