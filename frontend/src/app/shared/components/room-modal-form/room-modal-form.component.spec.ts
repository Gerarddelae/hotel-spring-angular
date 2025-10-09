import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomModalFormComponent } from './room-modal-form.component';

describe('RoomModalFormComponent', () => {
  let component: RoomModalFormComponent;
  let fixture: ComponentFixture<RoomModalFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomModalFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomModalFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
