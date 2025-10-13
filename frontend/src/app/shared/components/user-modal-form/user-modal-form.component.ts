import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { passwordsMatchValidator } from '../../validators/password-match.validator';
import { User } from '../../../features/users/models/user.interface';

interface UserModalData {
  user?: User; // usuario opcional para edición
}

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
    @Inject(MAT_DIALOG_DATA) public data: UserModalData
  ) {
    this.userForm = this.fb.group(
      {
        username: [data.user?.username || '', Validators.required],
        email: [data.user?.email || '', [Validators.required, Validators.email]],
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required]
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
