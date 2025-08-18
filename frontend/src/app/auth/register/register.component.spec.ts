import { TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('RegisterComponent (solo lógica)', () => {
  let component: RegisterComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    component = new RegisterComponent(
      TestBed.inject(FormBuilder),
      TestBed.inject(AuthService),
      TestBed.inject(Router)
    );

    component.ngOnInit(); // Inicializa el formulario y modo oscuro
  });

  it('debería crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('debería inicializar el formulario con valores vacíos y rol USER', () => {
    expect(component.form).toBeDefined();
    expect(component.username?.value).toBe('');
    expect(component.email?.value).toBe('');
    expect(component.password?.value).toBe('');
    expect(component.confirmPassword?.value).toBe('');
    expect(component.role?.value).toBe('USER');
  });

  it('debería marcar el formulario como inválido si faltan campos', () => {
    component.form.setValue({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'USER'
    });

    expect(component.form.invalid).toBeTrue();
  });

  it('debería validar que las contraseñas coincidan', () => {
    component.password?.setValue('123456');
    component.confirmPassword?.setValue('654321');

    expect(component.form.errors?.['mismatch']).toBeTrue();

    component.confirmPassword?.setValue('123456');
    expect(component.form.errors).toBeNull();
  });

  it('debería llamar a AuthService.register y navegar al login en éxito', () => {
    const mockFormValue = {
      username: 'testuser',
      email: 'test@example.com',
      password: '123456',
      confirmPassword: '123456',
      role: 'USER'
    };

    component.form.setValue(mockFormValue);
    authServiceSpy.register.and.returnValue(of({ token: 'fake-token' }));

    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalledWith({
      username: 'testuser',
      email: 'test@example.com',
      password: '123456',
      role: 'USER'
    });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('no debería llamar a AuthService.register si el formulario es inválido', () => {
    component.form.setValue({
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'USER'
    });

    component.onSubmit();

    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('debería manejar error en AuthService.register', () => {
    const mockFormValue = {
      username: 'testuser',
      email: 'test@example.com',
      password: '123456',
      confirmPassword: '123456',
      role: 'USER'
    };

    component.form.setValue(mockFormValue);
    authServiceSpy.register.and.returnValue(throwError(() => new Error('Error')));

    spyOn(console, 'error');

    component.onSubmit();

    expect(console.error).toHaveBeenCalledWith('❌ Error en registro:', jasmine.any(Error));
  });

  it('debería alternar el modo oscuro y guardar en localStorage', () => {
    spyOn(localStorage, 'setItem');
    component.isDarkMode = false;

    component.toggleDarkMode();

    expect(component.isDarkMode).toBeTrue();
    expect(localStorage.setItem).toHaveBeenCalledWith('theme', 'dark');
  });
});
