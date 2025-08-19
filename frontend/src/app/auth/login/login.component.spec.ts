import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../auth.service';
import { AuthResponse } from '../interfaces/auth-response.interface';
import { RouterTestingModule } from '@angular/router/testing';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router; // 🔹 router real de RouterTestingModule

  const mockResponse: AuthResponse = {
    token: 'mock-toker',
    username: 'admin',
    hotelName: 'Hotel Test',
    authorities: ['ADMIN'],
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent, // standalone component
        ReactiveFormsModule,
        RouterTestingModule.withRoutes([]) // 🔹 provee ActivatedRoute
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    router = TestBed.inject(Router);
    spyOn(router, 'navigate'); // 🔹 espía el router real

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to /dashboard on successful login', () => {
    authServiceSpy.login.and.returnValue(of(mockResponse));
    component.form.setValue({ username: 'testuser', password: 'testpass' });

    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({ username: 'testuser', password: 'testpass' });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']); // ✅ funciona ahora
  });

  it('should set authError on login failure', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new Error('Invalid credentials')));
    component.form.setValue({ username: 'wronguser', password: 'wrongpass' });

    component.onSubmit();

    expect(component.authError).toBe('Invalid username or password');
  });
});
