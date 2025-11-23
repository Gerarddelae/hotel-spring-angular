import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AddonRequest } from './models/addon-request.interface';
import { AddonResponse } from './models/addon-response.interface';

interface AddonModalData {
  addon?: AddonResponse;
}

@Component({
  selector: 'app-addons-form-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './addons-form-modal.component.html',
  styleUrls: ['./addons-form-modal.component.css']
})
export class AddonsFormModalComponent {
  addonForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddonsFormModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AddonModalData
  ) {
    const addon = data?.addon;
    this.addonForm = this.fb.group({
      name: [addon?.name || '', [Validators.required, Validators.maxLength(100)]],
      description: [addon?.description || '', Validators.maxLength(255)],
      price: [addon?.price || 0, [Validators.required, Validators.min(0)]],
    });
  }

  onSubmit() {
    if (this.addonForm.valid) {
      this.dialogRef.close(this.addonForm.value as AddonRequest);
    }
  }

  close() {
    this.dialogRef.close();
  }
}
