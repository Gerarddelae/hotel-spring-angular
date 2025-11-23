import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { GuestsService } from './guests.service';
import { GuestResponse } from './models/guest-response.interface';

describe('GuestsService', () => {
  let service: GuestsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule], providers: [GuestsService] });
    service = TestBed.inject(GuestsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created and load guests', () => {
    const mock: GuestResponse[] = [];
    service.loadGuests();
    const req = httpMock.expectOne('http://127.0.0.1:8080/guests');
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });
});
