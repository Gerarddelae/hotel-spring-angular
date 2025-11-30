import { Component, OnInit } from '@angular/core';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';
import { RegisterRequest } from '../interfaces/register-request.interface';
import { AuthResponse } from '../interfaces/auth-response.interface';
import { passwordsMatchValidator } from '../../shared/validators/password-match.validator';
import { CountryService, Country } from '../../shared/services/country.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './register.component.html',
})
export class RegisterComponent implements OnInit {
  form!: FormGroup;
  isDarkMode = false;
  showPassword = false;
  showConfirmPassword = false;
  authError: string | null = null;

  countries: Country[] = [];
  filteredCountries: Country[] = [];
  showDropdown = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private countryService: CountryService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group(
      {
        username: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],

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

    // Carga de países
    this.countryService.getCountries().subscribe({
      next: (countries) => {
        this.countries = countries;
        this.filteredCountries = countries;
      },
      error: (err) => console.error('Error cargando países', err),
    });

    // Dark mode
    const savedTheme = localStorage.getItem('theme');
    const systemPrefersDark = window.matchMedia(
      '(prefers-color-scheme: dark)'
    ).matches;
    this.isDarkMode =
      savedTheme === 'dark' || (!savedTheme && systemPrefersDark);
    this.applyTheme();
  }

  // Getters
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

  // Country dropdown
  filterCountries(value: string) {
    const filter = (value || '').toLowerCase();
    this.filteredCountries = this.countries.filter((c) =>
      c.name.toLowerCase().includes(filter)
    );
  }

  selectCountry(name: string) {
    this.form.get('country')?.setValue(name);
    this.showDropdown = false;
  }

  hideDropdown() {
    setTimeout(() => (this.showDropdown = false), 150);
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
        localStorage.setItem('token', res.token);
        localStorage.setItem('username', res.username);
        localStorage.setItem('hotelName', String(res.hotelName));
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.authError = 'Error en el registro. Inténtalo de nuevo.';
      },
    });
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    this.applyTheme();
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
  }

  private applyTheme(): void {
    document.documentElement.classList.toggle('dark', this.isDarkMode);
  }
}
