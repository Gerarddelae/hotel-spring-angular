import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HotelService } from './hotel.service';
import { AuthService } from '../../../auth/auth.service';
import { HotelResponse, HotelUpdateRequest } from '../models';

describe('HotelService', () => {
  let service: HotelService;
  let httpMock: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const authSpy = jasmine.createSpyObj('AuthService', ['getHotelId']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HotelService, { provide: AuthService, useValue: authSpy }],
    });

    service = TestBed.inject(HotelService);
    httpMock = TestBed.inject(HttpTestingController);
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get hotel by id', () => {
    const mockHotel: HotelResponse = {
      id: 1,
      name: 'Test Hotel',
      city: 'Bogotá',
      country: 'Colombia',
    };

    service.getHotelById(1).subscribe((hotel) => {
      expect(hotel).toEqual(mockHotel);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/hotels/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockHotel);
  });

  it('should update hotel', () => {
    const updateData: HotelUpdateRequest = {
      name: 'Updated Hotel',
      city: 'Medellín',
    };

    const mockResponse: HotelResponse = {
      id: 1,
      ...updateData,
    };

    service.updateHotel(1, updateData).subscribe((hotel) => {
      expect(hotel).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/hotels/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateData);
    req.flush(mockResponse);
  });

  it('should get current hotel', () => {
    authServiceSpy.getHotelId.and.returnValue(1);

    const mockHotel: HotelResponse = {
      id: 1,
      name: 'Test Hotel',
    };

    service.getCurrentHotel().subscribe((hotel) => {
      expect(hotel).toEqual(mockHotel);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/hotels/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockHotel);
  });
});
