import { TestBed } from '@angular/core/testing';
import { GuestsComponent } from './guests.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatDialogModule } from '@angular/material/dialog';

describe('GuestsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GuestsComponent, HttpClientTestingModule, MatDialogModule]
    }).compileComponents();
  });

  it('should create the guests component', () => {
    const fixture = TestBed.createComponent(GuestsComponent);
    const comp = fixture.componentInstance;
    expect(comp).toBeTruthy();
  });
});
