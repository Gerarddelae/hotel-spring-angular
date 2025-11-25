import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { BillDetailComponent } from './bill-detail.component';

describe('BillDetailComponent', () => {
  let component: BillDetailComponent;
  let fixture: ComponentFixture<BillDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BillDetailComponent,
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BillDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
