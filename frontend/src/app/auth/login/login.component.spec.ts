import { LoginComponent } from './login.component';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthResponse } from '../interfaces/auth-response.interface';
import { FormControl, FormGroup, Validators } from '@angular/forms';

describe('LoginComponent (solo lógica)', () => {
  let component: LoginComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    component = new LoginComponent(authServiceSpy, routerSpy);
    
    // Simular ngOnInit manualmente
    component.ngOnInit();
  });

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('form debería ser inválido al iniciar', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('form debería ser válido cuando se completan username y password', () => {
    component.form.setValue({ username: 'usuario', password: 'clave' });
    expect(component.form.valid).toBeTrue();
  });

  it('onSubmit no debería llamar a login si el formulario es inválido', () => {
    component.form.setValue({ username: '', password: '' });
    component.onSubmit();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('onSubmit debería llamar a AuthService.login si el formulario es válido', () => {
    const mockResponse: AuthResponse = { token: 'fake-token' };
    authServiceSpy.login.and.returnValue(of(mockResponse));

    component.form.setValue({ username: 'usuario', password: 'clave' });
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      username: 'usuario',
      password: 'clave'
    });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('debería establecer authError si login falla', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new Error('Invalid credentials')));

    component.form.setValue({ username: 'usuario', password: 'clave' });
    component.onSubmit();

    expect(component.authError).toBe('Invalid username or password');
  });

  it('toggleDarkMode debería alternar isDarkMode y actualizar localStorage', () => {
    component.isDarkMode = false;
    component.toggleDarkMode();
    expect(component.isDarkMode).toBeTrue();
    expect(localStorage.getItem('theme')).toBe('dark');

    component.toggleDarkMode();
    expect(component.isDarkMode).toBeFalse();
    expect(localStorage.getItem('theme')).toBe('light');
  });
});
