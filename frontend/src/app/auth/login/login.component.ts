import { Component, OnInit } from '@angular/core';
import {
  FormControl,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { AuthService } from '../auth.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule
  ],
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
  form!: FormGroup<{
    username: FormControl<string>;
    password: FormControl<string>;
  }>;

  isDarkMode = false;
  showPassword = false;

  authError: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.form = new FormGroup({
      username: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      password: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
    });

    // Restaurar tema guardado o detectar preferencia del sistema
    const savedTheme = localStorage.getItem('theme');
    const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    
    this.isDarkMode = savedTheme === 'dark' || (!savedTheme && systemPrefersDark);
    this.applyTheme();
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.authError = null;

    const { username, password } = this.form.getRawValue();
    this.authService.login({ username, password }).subscribe({
      next: (response) => {
        console.log('Login exitoso');
        // Redirección basada en el rol
        const authorities = response.authorities;
        if (authorities.includes('USER')) {
          this.router.navigate(['/bookings']);
        } else if (authorities.includes('ADMIN')) {
          this.router.navigate(['/dashboard']);
        } else {
          this.router.navigate(['/bookings']); // ruta por defecto
        }
      },
      error: (err) => {
        console.error('Error en login:', err)
        this.authError = 'Usuario o contraseña incorrectos';
      },
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