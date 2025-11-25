import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddonSelectorComponent } from './addon-selector.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('AddonSelectorComponent', () => {
  let component: AddonSelectorComponent;
  let fixture: ComponentFixture<AddonSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddonSelectorComponent, NoopAnimationsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AddonSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add addon to list', () => {
    component.availableAddons = [
      { id: 1, name: 'WiFi', price: 10 }
    ];
    component.selectedAddonId = 1;
    component.addAddon();

    expect(component.selectedAddons.length).toBe(1);
    expect(component.selectedAddons[0].addonId).toBe(1);
    expect(component.selectedAddons[0].quantity).toBe(1);
  });

  it('should increase quantity', () => {
    const addon = {
      addonId: 1,
      addonName: 'WiFi',
      price: 10,
      quantity: 1,
      subtotal: 10
    };
    component.selectedAddons = [addon];
    component.increaseQuantity(addon);

    expect(addon.quantity).toBe(2);
    expect(addon.subtotal).toBe(20);
  });

  it('should calculate total correctly', () => {
    component.selectedAddons = [
      { addonId: 1, price: 10, quantity: 2, subtotal: 20 },
      { addonId: 2, price: 15, quantity: 1, subtotal: 15 }
    ];

    expect(component.calculateTotal()).toBe(35);
  });
});
