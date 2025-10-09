import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { passwordsMatchValidator } from '../../validators/password-match.validator';

@Component({
  selector: 'app-user-modal-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './user-modal-form.component.html',
  styleUrls: ['./user-modal-form.component.css']
})
export class UserModalFormComponent {
  userForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<UserModalFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    const isEditMode = !!data?.user;

    this.userForm = this.fb.group(
      {
        username: [data?.user?.username || '', Validators.required],
        email: [data?.user?.email || '', [Validators.required, Validators.email]],
        password: ['', isEditMode ? [] : Validators.required],
        confirmPassword: ['', isEditMode ? [] : Validators.required]
      },
      { validators: passwordsMatchValidator }
    );
  }

  onSubmit() {
    if (this.userForm.valid) {
      const formValue = this.userForm.value;

      // Si está en modo edición y no se ingresó contraseña, no la incluimos
      if (this.data?.user && !formValue.password) {
        delete formValue.password;
        delete formValue.confirmPassword;
      }

      this.dialogRef.close(formValue);
    }
  }

  close() {
    this.dialogRef.close();
  }
}
