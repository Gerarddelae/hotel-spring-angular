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
import { RegisterRequest } from '../interfaces/register-request.interface';
import { AuthResponse } from '../interfaces/auth-response.interface';
import { passwordsMatchValidator } from '../../shared/validators/password-match.validator';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './register.component.html',
})
export class RegisterComponent implements OnInit {
  form!: FormGroup;
  isDarkMode = false;
  authError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group(
      {
        username: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],

        // Campos de hotel
        name: ['', Validators.required],
        address: ['', Validators.required],
        city: ['', Validators.required],
        country: ['', Validators.required],
        phone: ['', Validators.required],
        hotelEmail: ['', [Validators.required, Validators.email]],
        description: [''],
      },
      { validators: passwordsMatchValidator }
    );

    const savedTheme = localStorage.getItem('theme');
    const systemPrefersDark = window.matchMedia(
      '(prefers-color-scheme: dark)'
    ).matches;

    this.isDarkMode =
      savedTheme === 'dark' || (!savedTheme && systemPrefersDark);
    this.applyTheme();
  }

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

  onSubmit(): void {
    if (this.form.invalid) return;

    const {
      username,
      email,
      password,
      name,
      address,
      city,
      country,
      phone,
      hotelEmail,
      description,
    } = this.form.value;

    const payload: RegisterRequest = {
      user: { username, email, password },
      hotel: {
        name,
        address,
        city,
        country,
        phone,
        email: hotelEmail,
        description,
      },
    };


    this.authService.register(payload).subscribe({
      next: (res: AuthResponse) => {
        // Guardamos token, username y hotelId
        localStorage.setItem('token', res.token);
        localStorage.setItem('username', res.username);
        localStorage.setItem('hotelId', String(res.hotelId));

        this.router.navigate(['/dashboard']); // ✅ igual que login
      },
      error: (err) => {
        console.error('❌ Error en registro:', err);
        this.authError = 'Error en el registro. Inténtalo de nuevo.';
      }
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
