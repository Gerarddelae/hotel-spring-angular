import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../auth.service';
import { AuthResponse } from '../interfaces/auth-response.interface';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);

    await TestBed.configureTestingModule({
      imports: [
        RegisterComponent, // ✅ standalone
        RouterTestingModule, // ✅ proporciona ActivatedRoute, RouterLink, etc.
      ],
      providers: [{ provide: AuthService, useValue: authServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;

    router = TestBed.inject(Router);
    spyOn(router, 'navigate'); // 🔹 espiamos navigate

    fixture.detectChanges();
  });

  function fillValidForm() {
    component.form.patchValue({
      username: 'usuario',
      email: 'user@example.com',
      password: 'clave123',
      confirmPassword: 'clave123',
      name: 'Hotel Test',
      address: 'Calle 123',
      city: 'Bogotá',
      country: 'Colombia',
      phone: '123456789',
      hotelEmail: 'hotel@example.com',
      description: 'Hotel de prueba',
    });
  }

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('el formulario debería ser inválido si faltan campos obligatorios', () => {
    component.form.patchValue({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      name: '',
      address: '',
      city: '',
      country: '',
      phone: '',
      hotelEmail: '',
    });
    expect(component.form.invalid).toBeTrue();
  });

  it('debería llamar a AuthService.register con la estructura correcta', () => {
    const mockResponse: AuthResponse = {
      token: 'fake-token',
      username: 'usuario',
      hotelId: 1,
    };
    authServiceSpy.register.and.returnValue(of(mockResponse));

    fillValidForm();
    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalledWith({
      user: {
        username: 'usuario',
        email: 'user@example.com',
        password: 'clave123',
      },
      hotel: {
        name: 'Hotel Test',
        address: 'Calle 123',
        city: 'Bogotá',
        country: 'Colombia',
        phone: '123456789',
        email: 'hotel@example.com',
        description: 'Hotel de prueba',
      },
    });
  });

  it('debería guardar token, username y hotelId en localStorage tras registro exitoso', () => {
    const mockResponse: AuthResponse = {
      token: 'fake-token',
      username: 'usuario',
      hotelId: 1,
    };
    authServiceSpy.register.and.returnValue(of(mockResponse));
    spyOn(localStorage, 'setItem');

    fillValidForm();
    component.onSubmit();

    expect(localStorage.setItem).toHaveBeenCalledWith('token', 'fake-token');
    expect(localStorage.setItem).toHaveBeenCalledWith('username', 'usuario');
    expect(localStorage.setItem).toHaveBeenCalledWith('hotelId', '1');
  });

  it('debería navegar a /dashboard después de registro exitoso', () => {
    const mockResponse: AuthResponse = {
      token: 'fake-token',
      username: 'usuario',
      hotelId: 1,
    };
    authServiceSpy.register.and.returnValue(of(mockResponse));

    fillValidForm();
    component.onSubmit();

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('debería establecer authError cuando el registro falla', () => {
    authServiceSpy.register.and.returnValue(
      throwError(() => new Error('Error'))
    );
    fillValidForm();
    component.onSubmit();

    expect(component.authError).toBe(
      'Error en el registro. Inténtalo de nuevo.'
    );
  });
});
