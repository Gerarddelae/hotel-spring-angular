import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';

import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {

  // Usamos un FormGroup tipado con FormControls
  form!: FormGroup<{
    username: FormControl<string>;
    password: FormControl<string>;
  }>;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.form = new FormGroup({
      username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      password: new FormControl('', { nonNullable: true, validators: [Validators.required] })
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    const { username, password } = this.form.getRawValue();
    this.authService.login({ username, password }).subscribe({
      next: () => {console.log('Login exitoso');
        this.router.navigate(['/dashboard']);
      },
      error: err => console.error('Error en login:', err)
    });
  }
}
