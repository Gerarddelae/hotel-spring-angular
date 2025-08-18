import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthRequest } from './interfaces/auth-request.interface';
import { RegisterRequest } from './interfaces/register-request.interface';
import { AuthResponse } from './interfaces/auth-response.interface';
import { JwtPayload } from './interfaces/jwt-payload.interface';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;
  const apiUrl = 'http://localhost:8080/api/auth';

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy },
        provideHttpClient(withInterceptorsFromDi()), // Nuevo sistema
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería crearse el servicio', () => {
    expect(service).toBeTruthy();
  });

  describe('#login', () => {
    it('debería guardar el token en localStorage y actualizar el userSubject', () => {
      const mockRequest: AuthRequest = { username: 'test', password: '1234' };
      const mockResponse: AuthResponse = { token: 'mock-jwt-token' };
      const decodedPayload: JwtPayload = {
        sub: 'test',
        authorities: [{ authority: 'ROLE_USER' }],
        exp: Date.now() / 1000
      };

      spyOn(service, 'decodeToken').and.returnValue(decodedPayload);

      service.login(mockRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(localStorage.getItem('token')).toBe(mockResponse.token);
        expect(service.getCurrentUser()).toEqual(decodedPayload);
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRequest);
      req.flush(mockResponse);
    });
  });

  describe('#register', () => {
    it('debería realizar el registro y guardar el token', () => {
      const mockRegister: RegisterRequest = {
        username: 'user',
        email: 'user@test.com',
        password: 'password123',
        role: 'USER'
      };
      const mockResponse: AuthResponse = { token: 'mock-register-token' };

      service.register(mockRegister).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(localStorage.getItem('token')).toBe(mockResponse.token);
      });

      const req = httpMock.expectOne(`${apiUrl}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRegister);
      req.flush(mockResponse);
    });
  });

  describe('#logout', () => {
    it('debería eliminar el token y redirigir al login', () => {
      localStorage.setItem('token', 'fake-token');

      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
    });
  });

  describe('#isLoggedIn', () => {
    it('debería devolver true si hay token', () => {
      localStorage.setItem('token', 'fake-token');
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('debería devolver false si no hay token', () => {
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  describe('#decodeToken', () => {
    it('debería devolver el payload decodificado', () => {
      const mockToken = 'fake-token';
      const mockPayload: JwtPayload = {
        sub: 'test',
        authorities: [{ authority: 'ROLE_USER' }],
        exp: Date.now() / 1000
      };

      spyOn<any>(service, 'decodeToken').and.returnValue(mockPayload);

      const result = service.decodeToken(mockToken);
      expect(result).toEqual(mockPayload);
    });

    it('debería devolver null si el token no es válido', () => {
      const result = service.decodeToken('invalid-token');
      expect(result).toBeNull();
    });
  });
});
