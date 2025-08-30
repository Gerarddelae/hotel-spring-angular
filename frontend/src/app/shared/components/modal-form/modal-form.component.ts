import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { passwordsMatchValidator } from '../../validators/password-match.validator';

@Component({
  selector: 'app-modal-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule
  ],
  templateUrl: './modal-form.component.html',
  styles: [`
    :host {
      display: block;
    }
    
    ::ng-deep .mat-mdc-form-field-outline {
      @apply !border-gray-600;
    }
    
    ::ng-deep .mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-notched-outline__leading,
    ::ng-deep .mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-notched-outline__notch,
    ::ng-deep .mdc-text-field--outlined:not(.mdc-text-field--disabled) .mdc-notched-outline__trailing {
      @apply !border-gray-600;
    }
    
    ::ng-deep .mat-mdc-form-field-subscript-wrapper {
      height: auto !important;
    }
  `]
})
export class ModalFormComponent {
  userForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ModalFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.userForm = this.fb.group(
      {
        username: [data?.user?.username || '', Validators.required],
        email: [data?.user?.email || '', [Validators.required, Validators.email]],
        password: ['', data?.user ? [] : Validators.required],
        confirmPassword: ['', data?.user ? [] : Validators.required]
      },
      { validators: passwordsMatchValidator }
    );
  }

  onSubmit() {
    if (this.userForm.valid) {
      this.dialogRef.close(this.userForm.value);
    }
  }

  close() {
    this.dialogRef.close();
  }
}
