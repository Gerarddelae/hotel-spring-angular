import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthResponse } from './interfaces/auth-response.interface';
import { RegisterRequest } from './interfaces/register-request.interface';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockResponse: AuthResponse = {
    token: 'mock-jwt-token',
    username: 'mockuser',
    hotelId: 1
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('#login', () => {
    it('should return AuthResponse on successful login', () => {
      const credentials = { username: 'user', password: 'password' };

      service.login(credentials).subscribe((res) => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${service['apiUrl']}/login`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });

  describe('#register', () => {
    it('should return AuthResponse on successful registration', () => {
      const registerData: RegisterRequest = {
        user: {
          username: 'user',
          email: 'email@example.com',
          password: 'pass123'
        },
        hotel: {
          name: 'Hotel Test',
          address: '123 Street',
          city: 'City',
          country: 'Country',
          phone: '123456789',
          email: 'hotel@example.com',
          description: 'Hotel description'
        }
      };

      service.register(registerData).subscribe((res) => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${service['apiUrl']}/register`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });
});
