import { Component, OnInit } from '@angular/core';

import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { AuthService } from '../auth.service';
import { RegisterForm } from '../interfaces/register-form.interface';
import { CommonModule } from '@angular/common'; 
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './register.component.html',
})
export class RegisterComponent implements OnInit {
  form!: FormGroup;
  isDarkMode = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group(
      {
        username: this.fb.control('', Validators.required),
        email: this.fb.control('', [Validators.required, Validators.email]),
        password: this.fb.control('', [
          Validators.required,
          Validators.minLength(6),
        ]),
        confirmPassword: this.fb.control(''),
        role: this.fb.control<'USER' | 'ADMIN'>('USER'),
      },
      { validators: this.passwordsMatchValidator }
    );

    const savedTheme = localStorage.getItem('theme');
    const systemPrefersDark = window.matchMedia(
      '(prefers-color-scheme: dark)'
    ).matches;

    this.isDarkMode =
      savedTheme === 'dark' || (!savedTheme && systemPrefersDark);
    this.applyTheme();
  }

  // ✅ Validador de contraseñas
  passwordsMatchValidator: ValidatorFn = (
    group: AbstractControl
  ): ValidationErrors | null => {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  };

  // ✅ Getters para facilitar el acceso desde el template
  get username() {
    return this.form.get('username');
  }
  get email() {
    return this.form.get('email');
  }
  get password() {
    return this.form.get('password');
  }
  get confirmPassword() {
    return this.form.get('confirmPassword');
  }
  get role() {
    return this.form.get('role');
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const { username, email, password, role } = this.form.value;
    console.log(this.form.value);
    this.authService
      .register({
        username,
        email,
        password,
        role,
      })
      .subscribe({
        next: () => {
          console.log('✅ Registro exitoso');
          this.router.navigate(['/auth/login']);
        },
        error: (err) => console.error('❌ Error en registro:', err),
      });
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    this.applyTheme();
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
  }

  private applyTheme(): void {
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }
}
