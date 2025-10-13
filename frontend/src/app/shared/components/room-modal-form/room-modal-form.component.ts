import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-room-modal-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule
  ],
  templateUrl: './room-modal-form.component.html',
  styleUrl: './room-modal-form.component.css'
})
export class RoomModalFormComponent {
  roomForm: FormGroup;
  roomTypes = ['SINGLE', 'DOUBLE', 'SUITE', 'DELUXE'];
  roomStatuses = ['AVAILABLE', 'OCCUPIED', 'MAINTENANCE'];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<RoomModalFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    const room = data?.formFields || {};

    const isEditMode = !!data?.room;

    this.roomForm = this.fb.group({
      number: [data?.room?.number || '', Validators.required],
      type: [data?.room?.type || '', Validators.required],
      floor: [data?.room?.floor || '', [Validators.required, Validators.min(0)]],
      capacity: [data?.room?.capacity || '', [Validators.required, Validators.min(1)]],
      pricePerNight: [data?.room?.pricePerNight || '', [Validators.required, Validators.min(0.01)]],
      status: [data?.room?.status || '', Validators.required]
    });
  }

  onSubmit() {
    if (this.roomForm.valid) {
      this.dialogRef.close(this.roomForm.value);
    }
  }

  close() {
    this.dialogRef.close();
  }
}
