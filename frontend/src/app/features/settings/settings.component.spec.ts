import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SettingsComponent } from './settings.component';
import { HotelService } from './services/hotel.service';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('SettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SettingsComponent, ReactiveFormsModule, HttpClientTestingModule],
      providers: [HotelService],
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with empty values', () => {
    expect(component.hotelForm).toBeDefined();
    expect(component.hotelForm.get('name')?.value).toBe('');
  });

  it('should validate required name field', () => {
    const nameControl = component.hotelForm.get('name');
    nameControl?.setValue('');
    expect(nameControl?.invalid).toBeTruthy();
    expect(nameControl?.errors?.['required']).toBeTruthy();
  });

  it('should validate phone pattern', () => {
    const phoneControl = component.hotelForm.get('phone');
    phoneControl?.setValue('invalid');
    expect(phoneControl?.invalid).toBeTruthy();
    expect(phoneControl?.errors?.['pattern']).toBeTruthy();
  });

  it('should accept valid phone number', () => {
    const phoneControl = component.hotelForm.get('phone');
    phoneControl?.setValue('+573001234567');
    expect(phoneControl?.valid).toBeTruthy();
  });
});
