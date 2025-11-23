import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { GuestRequest } from './models/guest-request.interface';
import { GuestResponse } from './models/guest-response.interface';

interface GuestModalData {
  guest?: GuestResponse;
}

@Component({
  selector: 'app-guests-form-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './guests-form-modal.component.html',
  styleUrls: ['./guests-form-modal.component.css']
})
export class GuestsFormModalComponent {
  guestForm: FormGroup;
  documentTypes = ['DNI', 'Pasaporte', 'Cédula', 'Otro'];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<GuestsFormModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: GuestModalData
  ) {
    const g = data?.guest;
    this.guestForm = this.fb.group({
      fullName: [g?.fullName || '', Validators.required],
      documentType: [g?.documentType || '', Validators.required],
      documentNumber: [g?.documentNumber || '', Validators.required],
      email: [g?.email || '', [Validators.required, Validators.email]],
      phone: [g?.phone || ''],
      address: [g?.address || ''],
      // metrics are not editable from the modal; initialized server-side or in service
    });
  }

  onSubmit() {
    if (this.guestForm.valid) {
      this.dialogRef.close(this.guestForm.value as GuestRequest);
    }
  }

  close() {
    this.dialogRef.close();
  }
}
